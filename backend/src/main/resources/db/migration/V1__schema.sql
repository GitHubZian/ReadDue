-- =============================================================
-- V1：阅有期基线表结构（来源 doc/project/spec/schema.sql V1.1，逐字复制）
-- 说明：已存在的库通过 spring.flyway.baseline-on-migrate 打基线，本迁移不会重放；
--      全新库直接执行本脚本建表。不要手工修改本文件，结构演进一律新增 Vn 迁移。
-- =============================================================
-- =============================================================
-- 阅有期 · 图书馆催还与借阅管理平台
-- MySQL 8.0 / InnoDB / utf8mb4
-- 版本 V1.1（2026-11-04：补充权限分级、账号状态、逻辑删除；对应 doc/design/借阅与归还说明书.md V1.1）
-- 设计原则：
--   1. 规则可解释：所有判定参数落库（rule_param），公式输出快照留痕（grace_application）。
--   2. 数据最小化：不存阅读内容、检索词、阅读时长；审计日志 90 天滚动清理。
--   3. 状态可回溯：借阅单、提醒、申诉均为显式状态机，禁止物理删除业务行。
--   4. 可靠投递：通知走 transactional outbox + RabbitMQ，投递结果逐渠道记录。
--   5. 逻辑删除：主数据用 deleted（0 或本行主键）标记，业务事实用状态机收口，业务行永不物理删除。
-- =============================================================



SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------------------------
-- 一、身份与读者档案
-- -------------------------------------------------------------

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_no               VARCHAR(32)  NOT NULL COMMENT '账号：读者学号 / 馆员工号',
  role                  ENUM('READER','LIBRARIAN','ADMIN') NOT NULL DEFAULT 'READER' COMMENT '主角色（单角色，多角色需另建关联表）',
  name                  VARCHAR(64)  NOT NULL COMMENT '姓名',
  phone                 VARCHAR(20)  DEFAULT NULL COMMENT '手机号（仅用于通知渠道，不参与画像）',
  email                 VARCHAR(128) DEFAULT NULL COMMENT '邮箱（L2/L3 提醒渠道）',
  password_hash         VARCHAR(100) NOT NULL COMMENT 'BCrypt',
  status                ENUM('NORMAL','BLACKLISTED') NOT NULL DEFAULT 'NORMAL' COMMENT '账号状态：NORMAL 正常 / BLACKLISTED 拉黑',
  blacklist_reason      VARCHAR(255) DEFAULT NULL COMMENT '拉黑原因（读者可见）',
  blacklisted_at        DATETIME     DEFAULT NULL COMMENT '拉黑时间',
  blacklisted_by        BIGINT UNSIGNED DEFAULT NULL COMMENT '拉黑操作人（仅管理员）',
  blacklist_expires_at  DATETIME     DEFAULT NULL COMMENT '到期自动解除；NULL 表示需人工恢复',
  last_login_at         DATETIME     DEFAULT NULL,
  version               INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  deleted               BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=未删除；删除时写入本行 id，保证唯一键可复用',
  deleted_at            DATETIME     DEFAULT NULL COMMENT '逻辑删除时间',
  deleted_by            BIGINT UNSIGNED DEFAULT NULL COMMENT '删除操作人',
  created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_no (user_no, deleted),
  KEY idx_role_status (role, status),
  KEY idx_blacklist_expire (status, blacklist_expires_at)
) ENGINE=InnoDB COMMENT='账号（读者/馆员/管理员）';

-- -------------------------------------------------------------
-- 一之二、权限分级（RBAC，见技术架构 §10）
-- -------------------------------------------------------------

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_code   VARCHAR(32)  NOT NULL COMMENT 'ADMIN / LIBRARIAN / READER',
  role_name   VARCHAR(32)  NOT NULL COMMENT '管理员 / 馆员 / 用户',
  data_scope  ENUM('ALL','LIBRARY','SELF') NOT NULL DEFAULT 'SELF' COMMENT '数据范围',
  description VARCHAR(255) DEFAULT NULL,
  enabled     TINYINT NOT NULL DEFAULT 1,
  deleted     BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=未删除；删除时写入本行 id',
  deleted_at  DATETIME DEFAULT NULL,
  deleted_by  BIGINT UNSIGNED DEFAULT NULL,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code, deleted)
) ENGINE=InnoDB COMMENT='角色（三级：管理员 / 馆员 / 用户）';

DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  perm_code   VARCHAR(64)  NOT NULL COMMENT '如 borrow:return / user:blacklist',
  perm_name   VARCHAR(64)  NOT NULL,
  perm_type   ENUM('MENU','BUTTON','API','DATA') NOT NULL DEFAULT 'API',
  parent_id   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0 为根节点',
  path        VARCHAR(128) DEFAULT NULL COMMENT '前端路由（MENU）或接口路径（API）',
  method      VARCHAR(8)   DEFAULT NULL COMMENT 'HTTP 方法（API）',
  sort        INT NOT NULL DEFAULT 0,
  enabled     TINYINT NOT NULL DEFAULT 1,
  deleted     BIGINT UNSIGNED NOT NULL DEFAULT 0,
  deleted_at  DATETIME DEFAULT NULL,
  deleted_by  BIGINT UNSIGNED DEFAULT NULL,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_code (perm_code, deleted),
  KEY idx_parent (parent_id, sort)
) ENGINE=InnoDB COMMENT='权限码（菜单 / 按钮 / 接口 / 数据范围）';

DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_id       BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_perm (role_id, permission_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
  CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE=InnoDB COMMENT='角色-权限关联';

-- -------------------------------------------------------------
-- 一之三、账号状态日志（拉黑 / 恢复 / 注销全程留痕）
-- -------------------------------------------------------------

DROP TABLE IF EXISTS user_status_log;
CREATE TABLE user_status_log (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id        BIGINT UNSIGNED NOT NULL,
  from_status    VARCHAR(16) NOT NULL COMMENT 'NORMAL / BLACKLISTED / DELETED',
  to_status      VARCHAR(16) NOT NULL,
  reason         VARCHAR(255) DEFAULT NULL,
  expires_at     DATETIME DEFAULT NULL COMMENT '拉黑期限（仅转 BLACKLISTED 时）',
  operator_id    BIGINT UNSIGNED DEFAULT NULL,
  operator_name  VARCHAR(64) DEFAULT NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_created (user_id, created_at),
  CONSTRAINT fk_status_log_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='账号状态变更日志（只增，不修改）';

DROP TABLE IF EXISTS reader_profile;
CREATE TABLE reader_profile (
  user_id                BIGINT UNSIGNED NOT NULL COMMENT '读者账号',
  department             VARCHAR(64)  DEFAULT NULL COMMENT '院系',
  major                  VARCHAR(64)  DEFAULT NULL COMMENT '专业',
  grade                  VARCHAR(16)  DEFAULT NULL COMMENT '年级',
  scene_consent          TINYINT      NOT NULL DEFAULT 0 COMMENT '场景声明授权：0 未授权（默认最小授权）/ 1 已授权',
  cache_consent          TINYINT      NOT NULL DEFAULT 0 COMMENT '宽限计算缓存授权（TTL 24h）：0/1',
  consent_revoked_at     DATETIME     DEFAULT NULL COMMENT '最近一次关闭授权时间，触发立即遗忘',
  forget_requested_at    DATETIME     DEFAULT NULL COMMENT '读者点「立即遗忘」的时间',
  created_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  CONSTRAINT fk_reader_profile_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='读者档案与隐私授权状态';

DROP TABLE IF EXISTS reader_channel_pref;
CREATE TABLE reader_channel_pref (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  channel    ENUM('INBOX','EMAIL','SMS','APP_PUSH') NOT NULL COMMENT '站内 / 邮件 / 短信 / App 推送',
  enabled    TINYINT NOT NULL DEFAULT 1 COMMENT '渠道开关',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_channel (user_id, channel),
  CONSTRAINT fk_channel_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='提醒渠道偏好（FR-01）';

DROP TABLE IF EXISTS reader_quiet_hours;
CREATE TABLE reader_quiet_hours (
  user_id    BIGINT UNSIGNED NOT NULL,
  enabled    TINYINT NOT NULL DEFAULT 1 COMMENT '免打扰开关',
  start_time TIME NOT NULL DEFAULT '22:00:00' COMMENT '免打扰开始',
  end_time   TIME NOT NULL DEFAULT '08:00:00' COMMENT '免打扰结束（跨零点）',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  CONSTRAINT fk_quiet_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='免打扰时段，命中后顺延至结束时刻合并推送';

DROP TABLE IF EXISTS reader_credit;
CREATE TABLE reader_credit (
  user_id                BIGINT UNSIGNED NOT NULL,
  overdue_cnt_12m        SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '近 12 个月逾期次数',
  consecutive_good_months TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '连续守信月数（满 6 恢复一档）',
  kt_tier                TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '守信系数档位 0=1.00 / 1=0.85 / 2=0.70 / 3=0.55',
  last_evaluated_at      DATETIME DEFAULT NULL,
  updated_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  CONSTRAINT fk_credit_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='守信系数 Kt 的物化输入（不从借阅记录实时聚合）';

-- -------------------------------------------------------------
-- 二、书目与馆藏
-- -------------------------------------------------------------

DROP TABLE IF EXISTS book;
CREATE TABLE book (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  isbn13        VARCHAR(13)  DEFAULT NULL COMMENT 'ISBN-13',
  title         VARCHAR(200) NOT NULL COMMENT '书名',
  subtitle      VARCHAR(200) DEFAULT NULL,
  author        VARCHAR(200) DEFAULT NULL,
  publisher     VARCHAR(120) DEFAULT NULL,
  publish_date  DATE         DEFAULT NULL,
  category_code VARCHAR(32)  DEFAULT NULL COMMENT '中图法分类号',
  language      VARCHAR(16)  NOT NULL DEFAULT 'zh' COMMENT '语种',
  price         DECIMAL(10,2) DEFAULT NULL,
  summary       TEXT         DEFAULT NULL COMMENT '简介',
  cover_url     VARCHAR(500) DEFAULT NULL,
  tags          JSON         DEFAULT NULL COMMENT '标签数组，如 ["计算机","考试周高需"]',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1 上架 / 0 下架',
  deleted       BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=未删除；删除时写入本行 id',
  deleted_at    DATETIME     DEFAULT NULL,
  deleted_by    BIGINT UNSIGNED DEFAULT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_isbn13 (isbn13, deleted),
  KEY idx_title (title),
  KEY idx_category (category_code),
  FULLTEXT KEY ft_title_author (title, author) WITH PARSER ngram
) ENGINE=InnoDB COMMENT='书目';

DROP TABLE IF EXISTS book_copy;
CREATE TABLE book_copy (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  book_id     BIGINT UNSIGNED NOT NULL,
  barcode     VARCHAR(32) NOT NULL COMMENT '馆藏条码（册级唯一）',
  location    VARCHAR(64) DEFAULT NULL COMMENT '馆藏位置，如 A区3排2层',
  status      ENUM('AVAILABLE','ON_LOAN','READY_SHELF','LOST','DAMAGED','REPAIRING','WITHDRAWN')
              NOT NULL DEFAULT 'AVAILABLE' COMMENT '册状态',
  acquired_at DATE DEFAULT NULL,
  version     INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（借出/归还并发）',
  deleted     BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=未删除；删除时写入本行 id',
  deleted_at  DATETIME DEFAULT NULL,
  deleted_by  BIGINT UNSIGNED DEFAULT NULL,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_barcode (barcode, deleted),
  KEY idx_book_status (book_id, status),
  CONSTRAINT fk_copy_book FOREIGN KEY (book_id) REFERENCES book (id)
) ENGINE=InnoDB COMMENT='馆藏册';

-- -------------------------------------------------------------
-- 三、借阅 / 续借 / 预约
-- -------------------------------------------------------------

DROP TABLE IF EXISTS borrow_order;
CREATE TABLE borrow_order (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_no         VARCHAR(32) NOT NULL COMMENT '借阅单号',
  reader_id        BIGINT UNSIGNED NOT NULL,
  book_id          BIGINT UNSIGNED NOT NULL COMMENT '冗余书目，避免看板联表',
  copy_id          BIGINT UNSIGNED NOT NULL,
  borrowed_at      DATETIME NOT NULL,
  due_at           DATE NOT NULL COMMENT '应还日',
  effective_due_at DATE NOT NULL COMMENT '含宽限的最终应还日 = due_at + grace_days',
  grace_days       SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已批准宽限天数',
  renew_count      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已续借次数',
  returned_at      DATETIME DEFAULT NULL,
  final_overdue_days SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '归还/当前逾期天数快照',
  status           ENUM('BORROWED','OVERDUE','RETURNED','LOST','CLOSED')
                   NOT NULL DEFAULT 'BORROWED' COMMENT '借阅状态机',
  return_condition ENUM('GOOD','DAMAGED','LOST') DEFAULT NULL COMMENT '归还验收结论',
  version          INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（续借/归还/宽限并发）',
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_reader_status (reader_id, status),
  KEY idx_due (effective_due_at, status),
  KEY idx_copy_status (copy_id, status),
  CONSTRAINT fk_order_reader FOREIGN KEY (reader_id) REFERENCES sys_user (id),
  CONSTRAINT fk_order_book   FOREIGN KEY (book_id)   REFERENCES book (id),
  CONSTRAINT fk_order_copy   FOREIGN KEY (copy_id)   REFERENCES book_copy (id)
) ENGINE=InnoDB COMMENT='借阅单（核心表）';

DROP TABLE IF EXISTS renew_record;
CREATE TABLE renew_record (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  borrow_order_id BIGINT UNSIGNED NOT NULL,
  seq            TINYINT UNSIGNED NOT NULL COMMENT '第几次续借',
  old_due_at     DATE NOT NULL,
  new_due_at     DATE DEFAULT NULL,
  result         ENUM('APPROVED','REJECTED') NOT NULL,
  reject_code    VARCHAR(32) DEFAULT NULL COMMENT 'QUEUE_LIMIT / RENEW_LIMIT / OVERDUE / RESERVED / HOLDING',
  queue_len      SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '决策时预约队列长度（公平性证据）',
  operator_type  ENUM('READER','LIBRARIAN','SYSTEM') NOT NULL DEFAULT 'READER',
  operator_id    BIGINT UNSIGNED DEFAULT NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_seq (borrow_order_id, seq),
  KEY idx_result (result, created_at),
  CONSTRAINT fk_renew_order FOREIGN KEY (borrow_order_id) REFERENCES borrow_order (id)
) ENGINE=InnoDB COMMENT='续借记录（含拒绝理由，支撑「为什么不能续借」）';

DROP TABLE IF EXISTS reservation;
CREATE TABLE reservation (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  book_id       BIGINT UNSIGNED NOT NULL,
  reader_id     BIGINT UNSIGNED NOT NULL,
  queue_no      INT UNSIGNED NOT NULL COMMENT '排队序号，重排后整体递减',
  status        ENUM('WAITING','READY','FULFILLED','CANCELLED','EXPIRED')
                NOT NULL DEFAULT 'WAITING',
  ready_at      DATETIME DEFAULT NULL COMMENT '到馆可取时间',
  expires_at    DATETIME DEFAULT NULL COMMENT '保留截止（到馆后 48h）',
  fulfilled_at  DATETIME DEFAULT NULL,
  cancel_reason VARCHAR(64) DEFAULT NULL,
  version       INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（队列重排）',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_book_reader (book_id, reader_id, status),
  KEY idx_book_queue (book_id, status, queue_no),
  CONSTRAINT fk_res_book   FOREIGN KEY (book_id)   REFERENCES book (id),
  CONSTRAINT fk_res_reader FOREIGN KEY (reader_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='预约队列（Kq 供需系数来源）';

-- -------------------------------------------------------------
-- 四、场景与宽限（FR-02 核心，确定性规则引擎）
-- -------------------------------------------------------------

DROP TABLE IF EXISTS academic_calendar;
CREATE TABLE academic_calendar (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  term       VARCHAR(16) NOT NULL COMMENT '学期，如 2026-FALL',
  scene_code ENUM('GRADUATION','EXAM','THESIS','NORMAL') NOT NULL,
  start_date DATE NOT NULL,
  end_date   DATE NOT NULL,
  source     ENUM('CALENDAR','DECLARED') NOT NULL DEFAULT 'CALENDAR',
  note       VARCHAR(255) DEFAULT NULL,
  deleted    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=未删除；删除时写入本行 id',
  deleted_at DATETIME DEFAULT NULL,
  deleted_by BIGINT UNSIGNED DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_term_scene_start (term, scene_code, start_date, deleted),
  KEY idx_range (start_date, end_date)
) ENGINE=InnoDB COMMENT='校历场景区间（客观、零隐私成本）';

DROP TABLE IF EXISTS grace_application;
CREATE TABLE grace_application (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  apply_no       VARCHAR(32) NOT NULL,
  borrow_order_id BIGINT UNSIGNED NOT NULL,
  reader_id      BIGINT UNSIGNED NOT NULL,
  scene_code     ENUM('GRADUATION','EXAM','THESIS','NORMAL') NOT NULL COMMENT '读者自声明场景',
  scene_source   ENUM('CALENDAR','DECLARED','BOTH') NOT NULL COMMENT '场景判定来源',
  calendar_hit   TINYINT NOT NULL DEFAULT 0 COMMENT '校历是否覆盖当前日期',
  base_days      DECIMAL(4,1) NOT NULL COMMENT 'Bs 场景基准天数',
  kt             DECIMAL(3,2) NOT NULL COMMENT 'Kt 守信系数',
  kq             DECIMAL(3,2) NOT NULL COMMENT 'Kq 供需系数',
  dmax           TINYINT UNSIGNED NOT NULL COMMENT 'Dmax 封顶',
  computed_days  DECIMAL(4,1) NOT NULL COMMENT '公式结果 D',
  granted_days   SMALLINT UNSIGNED DEFAULT NULL COMMENT '实际批准天数（申诉/复核后可能不同）',
  evidence_text  VARCHAR(500) NOT NULL COMMENT '一句话依据说明（NFR-ETH-3）',
  formula_version VARCHAR(16) NOT NULL COMMENT '规则版本，用于复现',
  queue_len      SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '计算时队列长度',
  snapshot_expires_at DATETIME NOT NULL COMMENT '快照 TTL 到期（默认 24h）',
  status         ENUM('PENDING','APPROVED','REJECTED','EXPIRED','APPEALING','CLOSED')
                 NOT NULL DEFAULT 'PENDING',
  decided_by     BIGINT UNSIGNED DEFAULT NULL,
  decided_at     DATETIME DEFAULT NULL,
  reject_code    VARCHAR(32) DEFAULT NULL,
  version        INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_apply_no (apply_no),
  KEY idx_order (borrow_order_id),
  KEY idx_reader_status (reader_id, status),
  KEY idx_expire (snapshot_expires_at),
  CONSTRAINT fk_grace_order  FOREIGN KEY (borrow_order_id) REFERENCES borrow_order (id),
  CONSTRAINT fk_grace_reader FOREIGN KEY (reader_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='宽限申请与计算快照（含可解释依据，24h 后清空敏感字段）';

-- -------------------------------------------------------------
-- 五、规则参数与逾期分层（可配置、可公示）
-- -------------------------------------------------------------

DROP TABLE IF EXISTS rule_param;
CREATE TABLE rule_param (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  param_key      VARCHAR(64) NOT NULL COMMENT '如 BS.GRADUATION / KT_TIER_1 / QUIET.START',
  param_value    VARCHAR(64) NOT NULL,
  value_type     ENUM('INT','DECIMAL','STRING','TIME','JSON') NOT NULL DEFAULT 'STRING',
  version        VARCHAR(16) NOT NULL DEFAULT 'v1',
  effective_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  effective_to   DATETIME DEFAULT NULL COMMENT 'NULL 表示当前生效',
  description    VARCHAR(255) DEFAULT NULL COMMENT '对读者公示的参数说明',
  updated_by     BIGINT UNSIGNED DEFAULT NULL,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_key_version (param_key, version),
  KEY idx_key_effective (param_key, effective_to)
) ENGINE=InnoDB COMMENT='规则参数表（参数即产品，公开可核对）';

DROP TABLE IF EXISTS overdue_tier;
CREATE TABLE overdue_tier (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tier_code    VARCHAR(16) NOT NULL COMMENT 'T1 / T2 / T3 / T4 / T5',
  tier_name    VARCHAR(32) NOT NULL COMMENT '如「温和提醒期」',
  min_days     SMALLINT UNSIGNED NOT NULL,
  max_days     SMALLINT UNSIGNED DEFAULT NULL COMMENT 'NULL 表示无上限',
  action_code  VARCHAR(32) NOT NULL COMMENT 'REINFORCE / APPEAL_WINDOW / LIMIT_BORROW / LIBRARIAN_HANDLE',
  tone_code    VARCHAR(32) NOT NULL COMMENT 'message-soft / message-clear / message-formal，对应话术模板',
  allow_renew  TINYINT NOT NULL DEFAULT 0 COMMENT '该层是否仍可续借',
  channels     JSON DEFAULT NULL COMMENT '该层默认渠道',
  version      VARCHAR(16) NOT NULL DEFAULT 'v1',
  description  VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tier_version (tier_code, version),
  KEY idx_days (min_days, max_days)
) ENGINE=InnoDB COMMENT='逾期分级配置（FR-04，每层动作与话术不同）';

-- -------------------------------------------------------------
-- 六、通知与提醒（FR-01，outbox + MQ + 渠道投递）
-- -------------------------------------------------------------

DROP TABLE IF EXISTS notify_template;
CREATE TABLE notify_template (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  template_code VARCHAR(48) NOT NULL COMMENT '如 DUE_L1 / DUE_L2 / DUE_L3 / GRACE_RESULT / APPEAL_SLA',
  channel       ENUM('INBOX','EMAIL','SMS','APP_PUSH') NOT NULL,
  title_tpl     VARCHAR(200) NOT NULL COMMENT '支持 {{bookTitle}} 等占位符',
  body_tpl      TEXT NOT NULL,
  tone_code     VARCHAR(32) NOT NULL DEFAULT 'message-soft' COMMENT '非羞辱性措辞分级',
  version       VARCHAR(16) NOT NULL DEFAULT 'v1',
  enabled       TINYINT NOT NULL DEFAULT 1,
  deleted       BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=未删除；删除时写入本行 id',
  deleted_at    DATETIME DEFAULT NULL,
  deleted_by    BIGINT UNSIGNED DEFAULT NULL,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_code_channel_version (template_code, channel, version, deleted)
) ENGINE=InnoDB COMMENT='提醒文案模板（话术是交付物，需评审）';

DROP TABLE IF EXISTS notify_task;
CREATE TABLE notify_task (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  dedup_key      VARCHAR(96) NOT NULL COMMENT 'readerId:borrowOrderId:level:planDate，幂等键',
  reader_id      BIGINT UNSIGNED NOT NULL,
  borrow_order_id BIGINT UNSIGNED DEFAULT NULL,
  level          ENUM('L1','L2','L3','GRACE_RESULT','APPEAL_SLA','RESERVATION_READY','SYSTEM')
                 NOT NULL,
  plan_at        DATETIME NOT NULL COMMENT '计划发送时刻（已含免打扰顺延）',
  quiet_shifted  TINYINT NOT NULL DEFAULT 0 COMMENT '是否因免打扰顺延',
  status         ENUM('PLANNED','QUEUED','SENT','PARTIAL','FAILED','CANCELED','MERGED')
                 NOT NULL DEFAULT 'PLANNED',
  payload        JSON DEFAULT NULL COMMENT '渲染上下文（不含阅读内容）',
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_dedup (dedup_key),
  KEY idx_plan (plan_at, status),
  KEY idx_reader (reader_id, created_at),
  CONSTRAINT fk_task_order FOREIGN KEY (borrow_order_id) REFERENCES borrow_order (id)
) ENGINE=InnoDB COMMENT='提醒任务（调度产物，幂等）';

DROP TABLE IF EXISTS notify_delivery;
CREATE TABLE notify_delivery (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  task_id      BIGINT UNSIGNED NOT NULL,
  channel      ENUM('INBOX','EMAIL','SMS','APP_PUSH') NOT NULL,
  status       ENUM('PENDING','SENT','FAILED','SKIPPED') NOT NULL DEFAULT 'PENDING',
  attempt      TINYINT UNSIGNED NOT NULL DEFAULT 1,
  provider_msg_id VARCHAR(96) DEFAULT NULL,
  error_code   VARCHAR(32) DEFAULT NULL,
  error_msg    VARCHAR(500) DEFAULT NULL,
  sent_at      DATETIME DEFAULT NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_channel_attempt (task_id, channel, attempt),
  KEY idx_status (status, created_at),
  CONSTRAINT fk_delivery_task FOREIGN KEY (task_id) REFERENCES notify_task (id)
) ENGINE=InnoDB COMMENT='逐渠道投递结果（重试计数与失败归因）';

DROP TABLE IF EXISTS event_outbox;
CREATE TABLE event_outbox (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  aggregate_type VARCHAR(48) NOT NULL COMMENT 'BORROW_ORDER / GRACE / APPEAL / RESERVATION',
  aggregate_id   BIGINT UNSIGNED NOT NULL,
  event_type     VARCHAR(64) NOT NULL COMMENT 'BORROW_RETURNED / OVERDUE_MARKED / GRACE_APPROVED ...',
  payload        JSON NOT NULL,
  status         ENUM('NEW','PUBLISHED','FAILED','DEAD') NOT NULL DEFAULT 'NEW',
  retry_count    SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_at   DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_status_created (status, created_at),
  KEY idx_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB COMMENT='本地消息表：业务事务内落库，再由投递器发 RabbitMQ（至少一次）';

-- -------------------------------------------------------------
-- 七、申诉工单（FR-06，2h SLA 与人负责）
-- -------------------------------------------------------------

DROP TABLE IF EXISTS appeal_ticket;
CREATE TABLE appeal_ticket (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ticket_no        VARCHAR(32) NOT NULL,
  reader_id        BIGINT UNSIGNED NOT NULL,
  target_type      ENUM('GRACE','OVERDUE_TIER','RENEW','REMINDER','OTHER') NOT NULL,
  target_id        BIGINT UNSIGNED NOT NULL COMMENT '被申诉对象主键（grace_application / borrow_order / notify_task）',
  reason           VARCHAR(1000) NOT NULL COMMENT '读者陈述',
  status           ENUM('OPEN','ACKED','RESOLVED','REJECTED','AUTO_COMPENSATED','CLOSED')
                   NOT NULL DEFAULT 'OPEN',
  sla_due_at       DATETIME NOT NULL COMMENT '创建时间 + 2h',
  first_reply_at   DATETIME DEFAULT NULL,
  sla_breached     TINYINT NOT NULL DEFAULT 0 COMMENT '是否超时（超时自动补偿 3 天）',
  assignee_id      BIGINT UNSIGNED DEFAULT NULL,
  assignee_name    VARCHAR(64) DEFAULT NULL COMMENT '受理馆员真实署名',
  compensation_days TINYINT UNSIGNED NOT NULL DEFAULT 0,
  resolution       VARCHAR(1000) DEFAULT NULL,
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ticket_no (ticket_no),
  KEY idx_status_sla (status, sla_due_at),
  KEY idx_reader (reader_id, created_at),
  KEY idx_target (target_type, target_id),
  CONSTRAINT fk_appeal_reader FOREIGN KEY (reader_id) REFERENCES sys_user (id)
) ENGINE=InnoDB COMMENT='申诉工单';

DROP TABLE IF EXISTS appeal_event;
CREATE TABLE appeal_event (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ticket_id  BIGINT UNSIGNED NOT NULL,
  event_type ENUM('CREATED','ASSIGNED','REPLIED','RESOLVED','REJECTED','AUTO_COMPENSATED','CLOSED')
             NOT NULL,
  actor_id   BIGINT UNSIGNED DEFAULT NULL,
  actor_name VARCHAR(64) DEFAULT NULL,
  content    VARCHAR(1000) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_ticket_time (ticket_id, created_at),
  CONSTRAINT fk_event_ticket FOREIGN KEY (ticket_id) REFERENCES appeal_ticket (id)
) ENGINE=InnoDB COMMENT='工单流转时间线（读者可见全过程）';

-- -------------------------------------------------------------
-- 八、异常处理与审计
-- -------------------------------------------------------------

DROP TABLE IF EXISTS lost_book_report;
CREATE TABLE lost_book_report (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  borrow_order_id BIGINT UNSIGNED NOT NULL,
  reader_id      BIGINT UNSIGNED NOT NULL,
  declared_type  ENUM('LOST','DAMAGED') NOT NULL DEFAULT 'LOST',
  description    VARCHAR(500) DEFAULT NULL,
  status         ENUM('SUBMITTED','CONFIRMED','COMPENSATED','REJECTED') NOT NULL DEFAULT 'SUBMITTED',
  handling_note  VARCHAR(500) DEFAULT NULL,
  decided_by     BIGINT UNSIGNED DEFAULT NULL,
  decided_at     DATETIME DEFAULT NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order (borrow_order_id),
  CONSTRAINT fk_lost_order FOREIGN KEY (borrow_order_id) REFERENCES borrow_order (id)
) ENGINE=InnoDB COMMENT='丢书/损书申报（人工确认，不做自动扣款）';

DROP TABLE IF EXISTS audit_log;
CREATE TABLE audit_log (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  actor_id    BIGINT UNSIGNED DEFAULT NULL,
  actor_role  VARCHAR(16) DEFAULT NULL,
  action      VARCHAR(64) NOT NULL COMMENT 'GRACE_SNAPSHOT_READ / CONSENT_REVOKED / TIER_OVERRIDE ...',
  object_type VARCHAR(32) NOT NULL,
  object_id   BIGINT UNSIGNED NOT NULL,
  purpose     VARCHAR(64) NOT NULL COMMENT '追责用途标签，禁止用于画像',
  detail      JSON DEFAULT NULL,
  ip          VARCHAR(45) DEFAULT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_object (object_type, object_id),
  KEY idx_actor_time (actor_id, created_at),
  KEY idx_created (created_at)
) ENGINE=InnoDB COMMENT='操作审计，保留 90 天，仅供追责';

-- -------------------------------------------------------------
-- 九、视图（只读聚合，供接口与管理端看板使用）
-- -------------------------------------------------------------

-- 逻辑删除约定：视图与统计一律只取 deleted = 0 的行（联表时过滤条件写在 ON 上，保留左连接语义）

CREATE OR REPLACE VIEW v_book_stock AS
SELECT b.id                AS book_id,
       b.title,
       COUNT(c.id)                                             AS total_copies,
       SUM(c.status = 'AVAILABLE')                              AS available_copies,
       SUM(c.status = 'ON_LOAN')                                AS on_loan_copies,
       (SELECT COUNT(*) FROM reservation r
         WHERE r.book_id = b.id AND r.status = 'WAITING')       AS queue_len
FROM book b
LEFT JOIN book_copy c ON c.book_id = b.id AND c.deleted = 0
WHERE b.deleted = 0
GROUP BY b.id, b.title;

CREATE OR REPLACE VIEW v_overdue_board AS
SELECT o.id            AS order_id,
       o.order_no,
       o.reader_id,
       u.name          AS reader_name,
       u.user_no       AS student_no,
       o.book_id,
       b.title         AS book_title,
       o.due_at,
       o.effective_due_at,
       o.grace_days,
       DATEDIFF(CURDATE(), o.effective_due_at)  AS overdue_days,
       o.renew_count,
       (SELECT COUNT(*) FROM notify_task t
         WHERE t.borrow_order_id = o.id AND t.level = 'L3') AS l3_sent,
       (SELECT a.status FROM appeal_ticket a
         WHERE a.target_type = 'OVERDUE_TIER' AND a.target_id = o.id
         ORDER BY a.created_at DESC LIMIT 1)    AS appeal_status
FROM borrow_order o
JOIN sys_user u ON u.id = o.reader_id AND u.deleted = 0
JOIN book b     ON b.id = o.book_id AND b.deleted = 0
WHERE o.status IN ('BORROWED','OVERDUE');

-- -------------------------------------------------------------
-- 十、初始化参数与分层配置（与说明书 §5 对齐）
-- -------------------------------------------------------------

INSERT INTO rule_param (param_key, param_value, value_type, version, description) VALUES
  ('BS.GRADUATION', '14', 'INT',     'v1', '毕业季场景基准天数'),
  ('BS.THESIS',     '10', 'INT',     'v1', '论文季场景基准天数'),
  ('BS.EXAM',       '7',  'INT',     'v1', '考试周场景基准天数'),
  ('BS.NORMAL',     '3',  'INT',     'v1', '常规场景基准天数'),
  ('KT.TIER_0',     '1.00','DECIMAL','v1', '守信系数：近 12 个月逾期 0 次'),
  ('KT.TIER_1',     '0.85','DECIMAL','v1', '守信系数：逾期 1 次'),
  ('KT.TIER_2',     '0.70','DECIMAL','v1', '守信系数：逾期 2 次'),
  ('KT.TIER_3',     '0.55','DECIMAL','v1', '守信系数：逾期 ≥3 次（下限，永不归零）'),
  ('KT.RECOVER_MONTHS','6','INT',    'v1', '连续守信满 6 个月恢复一档'),
  ('KQ.LEVEL_0',    '1.00','DECIMAL','v1', '供需系数：预约队列 0 人'),
  ('KQ.LEVEL_1',    '0.85','DECIMAL','v1', '供需系数：队列 1–3 人'),
  ('KQ.LEVEL_2',    '0.70','DECIMAL','v1', '供需系数：队列 ≥4 人'),
  ('GRACE.DMAX',    '14', 'INT',     'v1', '宽限封顶天数'),
  ('GRACE.ROUND_STEP','0.5','DECIMAL','v1','计算结果按 0.5 天取整'),
  ('GRACE.SNAPSHOT_TTL_HOURS','24','INT','v1','计算快照缓存存活 24 小时'),
  ('RENEW.MAX_TIMES','1',  'INT',     'v1', '普通借阅可续借次数'),
  ('RENEW.QUEUE_BLOCK','4','INT',     'v1', '预约队列 ≥4 人时禁止续借'),
  ('RENEW.LOAN_DAYS','30','INT',      'v1', '单次借期天数'),
  ('RENEW.PERIOD_DAYS','15','INT',    'v1', '单次续借延长天数'),
  ('QUIET.START',   '22:00','TIME',   'v1', '免打扰开始时间'),
  ('QUIET.END',     '08:00','TIME',   'v1', '免打扰结束时间'),
  ('APPEAL.SLA_HOURS','2','INT',      'v1', '申诉首次人工回应时限'),
  ('APPEAL.AUTO_COMP_DAYS','3','INT', 'v1', 'SLA 超时自动顺延天数'),
  ('AUDIT.RETENTION_DAYS','90','INT', 'v1', '审计日志保留天数'),
  ('RESERVATION.READY_HOLD_HOURS','48','INT','v1','到馆保留时长'),
  ('BORROW.FINE_GATE_TIER','T4','STRING','v1','罚款金额仅在该层及以后出现');

INSERT INTO overdue_tier (tier_code, tier_name, min_days, max_days, action_code, tone_code, allow_renew, channels, version, description) VALUES
  ('T1','温和提醒期',   0, 1,    'REINFORCE',        'message-soft',   1, '["INBOX","EMAIL"]', 'v1', '仅提醒，附一键续借，不提罚款'),
  ('T2','可申诉保护期', 2, 3,    'APPEAL_WINDOW',    'message-clear',  1, '["INBOX","EMAIL"]', 'v1', '开放一键申诉与宽限补申请'),
  ('T3','限制借阅期',   4, 7,    'LIMIT_BORROW',     'message-clear',  0, '["INBOX","EMAIL"]', 'v1', '暂停新增借阅，归还后即时恢复'),
  ('T4','馆员介入期',   8, 30,   'LIBRARIAN_HANDLE', 'message-formal', 0, '["INBOX","EMAIL"]', 'v1', '转馆员人工跟进，出现费用建议'),
  ('T5','长期逾期',     31, NULL, 'LIBRARIAN_HANDLE','message-formal', 0, '["INBOX","EMAIL"]', 'v1', '纳入挂账名单，仅人工处理');

INSERT INTO notify_template (template_code, channel, title_tpl, body_tpl, tone_code, version) VALUES
  ('DUE_L1','INBOX','《{{bookTitle}}》还有 3 天到期','这本书 3 天后到期。时间充裕的话可以现在续借，也可以到期前还回。','message-soft','v1'),
  ('DUE_L2','INBOX','《{{bookTitle}}》明天到期','明天是应还日。点这里一键续借，或按自己的节奏还书。','message-soft','v1'),
  ('DUE_L2','EMAIL','《{{bookTitle}}》明天到期','明天是应还日。一键续借：{{renewLink}}','message-soft','v1'),
  ('DUE_L3','INBOX','《{{bookTitle}}》已到期 1 天','这本书已过应还日 1 天。你可以在「宽限申请」里说明情况，我们会按规则给出可解释的延期；也可以直接归还。','message-clear','v1'),
  ('DUE_L3','EMAIL','《{{bookTitle}}》已到期 1 天','已过应还日 1 天。宽限申请：{{graceLink}}；归还指引：{{returnGuide}}','message-clear','v1'),
  ('GRACE_RESULT','INBOX','宽限结果：{{days}} 天','{{evidenceText}}','message-clear','v1'),
  ('APPEAL_SLA','INBOX','工单 {{ticketNo}} 已受理','受理人：{{assigneeName}}。{{slaText}}','message-clear','v1'),
  ('RESERVATION_READY','INBOX','《{{bookTitle}}》已到馆','为你保留至 {{expiresAt}}，逾期自动释放给下一位。','message-soft','v1');

-- -------------------------------------------------------------
-- 十一、权限分级种子数据（与说明书 §2.1 / 技术架构 §10 对齐）
-- -------------------------------------------------------------

INSERT INTO sys_role (role_code, role_name, data_scope, description) VALUES
  ('ADMIN',     '管理员', 'ALL',     '规则参数、权限授权、拉黑与注销、审计查询'),
  ('LIBRARIAN', '馆员',   'LIBRARY', '归还验收、逾期跟进、宽限与申诉受理、看板导出'),
  ('READER',    '用户',   'SELF',    '借阅、归还、续借、预约、宽限申请、申诉');

INSERT INTO sys_permission (perm_code, perm_name, perm_type, path, method, sort) VALUES
  ('borrow:create',     '借出',         'API',  '/api/borrows',                        'POST',   10),
  ('borrow:return',     '归还',         'API',  '/api/borrows/{id}/return',            'POST',   11),
  ('borrow:renew',      '续借',         'API',  '/api/borrows/{id}/renew',             'POST',   12),
  ('borrow:read:self',  '查看本人借阅', 'DATA', NULL,                                  NULL,     20),
  ('borrow:read:all',   '查看全量借阅', 'DATA', NULL,                                  NULL,     21),
  ('reservation:create','预约',         'API',  '/api/reservations',                   'POST',   30),
  ('reservation:manage','管理预约',     'API',  '/api/admin/reservations',             'GET',    31),
  ('grace:apply',       '申请宽限',     'API',  '/api/graces',                         'POST',   40),
  ('grace:approve',     '审批宽限',     'API',  '/api/admin/graces/{id}/decide',       'POST',   41),
  ('appeal:create',     '发起申诉',     'API',  '/api/appeals',                        'POST',   50),
  ('appeal:handle',     '受理申诉',     'API',  '/api/admin/appeals/{id}',             'PUT',    51),
  ('overdue:board',     '逾期看板',     'API',  '/api/admin/overdue-board',            'GET',    60),
  ('user:blacklist',    '拉黑用户',     'API',  '/api/admin/users/{id}/blacklist',     'POST',   70),
  ('user:delete',       '注销用户',     'API',  '/api/admin/users/{id}',               'DELETE', 71),
  ('rule:update',       '规则参数变更', 'API',  '/api/admin/rules',                     'PUT',    80),
  ('audit:read',        '审计查询',     'API',  '/api/admin/audit',                     'GET',    90);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p
 WHERE r.role_code = 'READER'
   AND p.perm_code IN ('borrow:create','borrow:return','borrow:renew','borrow:read:self',
                       'reservation:create','grace:apply','appeal:create');

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p
 WHERE r.role_code = 'LIBRARIAN'
   AND p.perm_code IN ('borrow:return','borrow:read:all','reservation:manage',
                       'grace:approve','appeal:handle','overdue:board');

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p
 WHERE r.role_code = 'ADMIN';

SET FOREIGN_KEY_CHECKS = 1;
