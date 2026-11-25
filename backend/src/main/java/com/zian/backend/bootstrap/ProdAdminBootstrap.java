package com.zian.backend.bootstrap;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 生产环境初始管理员引导。
 *
 * <p>生产库只有表结构与角色/权限字典，没有任何账号；演示种子又只在 local 生效。
 * 因此首次以 prod 启动且库中不存在管理员时，用环境变量创建唯一一个管理员账号，
 * 让运维可以登录管理端，再在界面里建馆员/读者。</p>
 *
 * <p>安全约束：只在 prod profile 生效；只在「库中无 ADMIN」时执行一次；
 * 密码必须由环境变量提供且不短于 12 位；创建后日志只提示用户名，不打印密码。</p>
 */
@Component
@Profile("prod")
public class ProdAdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdAdminBootstrap.class);
    private static final int MIN_PASSWORD_LENGTH = 12;

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String username;
    private final String password;

    public ProdAdminBootstrap(
            JdbcTemplate jdbc,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.enabled:true}") boolean enabled,
            @Value("${app.bootstrap.admin.username:admin}") String username,
            @Value("${app.bootstrap.admin.password:}") String password) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.username = username == null || username.isBlank() ? "admin" : username.trim();
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("初始管理员引导已关闭（app.bootstrap.admin.enabled=false）");
            return;
        }
        Integer admins = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE role = 'ADMIN' AND deleted = 0", Integer.class);
        if (admins != null && admins > 0) {
            log.info("库中已有 {} 个管理员，跳过初始管理员引导", admins);
            return;
        }
        if (password == null || password.isBlank()) {
            log.warn("库中没有任何管理员，但未设置 APP_BOOTSTRAP_ADMIN_PASSWORD；管理端将无法登录。"
                    + "请设置该环境变量后重启一次。");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "APP_BOOTSTRAP_ADMIN_PASSWORD 至少 " + MIN_PASSWORD_LENGTH + " 位");
        }
        long id = IdWorker.getId();
        jdbc.update("""
                INSERT INTO sys_user
                    (id, user_no, role, name, password_hash, status, version, deleted)
                VALUES (?, ?, 'ADMIN', '系统管理员', ?, 'NORMAL', 0, 0)
                """, id, username, passwordEncoder.encode(password));
        log.info("已创建初始管理员账号 {}，请首次登录后立即修改密码", username);
    }
}
