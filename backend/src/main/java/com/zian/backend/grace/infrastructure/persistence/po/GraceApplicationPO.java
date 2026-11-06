package com.zian.backend.grace.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 表 {@code grace_application}（宽限申请与计算快照（含可解释依据，24h 后清空敏感字段））的 MyBatis-Plus 持久化对象。
 *
 * <p>仅允许出现在 infrastructure 层；domain / application 不得直接依赖本类。
 */
@TableName("grace_application")
@Data
public class GraceApplicationPO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String applyNo;

    private Long borrowOrderId;

    private Long readerId;

    /** 读者自声明场景 枚举：GRADUATION/EXAM/THESIS/NORMAL */
    private String sceneCode;

    /** 场景判定来源 枚举：CALENDAR/DECLARED/BOTH */
    private String sceneSource;

    /** 校历是否覆盖当前日期 */
    private Integer calendarHit;

    /** Bs 场景基准天数 */
    private BigDecimal baseDays;

    /** Kt 守信系数 */
    private BigDecimal kt;

    /** Kq 供需系数 */
    private BigDecimal kq;

    /** Dmax 封顶 */
    private Integer dmax;

    /** 公式结果 D */
    private BigDecimal computedDays;

    /** 实际批准天数（申诉/复核后可能不同） */
    private Integer grantedDays;

    /** 一句话依据说明（NFR-ETH-3） */
    private String evidenceText;

    /** 规则版本，用于复现 */
    private String formulaVersion;

    /** 计算时队列长度 */
    private Integer queueLen;

    /** 快照 TTL 到期（默认 24h） */
    private LocalDateTime snapshotExpiresAt;

    /** 枚举：PENDING/APPROVED/REJECTED/EXPIRED/APPEALING/CLOSED */
    private String status;

    private Long decidedBy;

    private LocalDateTime decidedAt;

    private String rejectCode;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
