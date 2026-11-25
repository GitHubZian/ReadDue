package com.zian.backend.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

class ProdAdminBootstrapTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    private ProdAdminBootstrap bootstrap(boolean enabled, String password) {
        return new ProdAdminBootstrap(jdbc, encoder, enabled, "admin", password);
    }

    @Test
    void skipsWhenAdminAlreadyExists() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);

        bootstrap(true, "a-strong-password-123").run(null);

        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void createsAdminWhenNoneExists() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
        when(encoder.encode("a-strong-password-123")).thenReturn("$2a$bcrypt-hash");

        bootstrap(true, "a-strong-password-123").run(null);

        ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).update(contains("INSERT INTO sys_user"), args.capture());
        Object[] values = args.getValue();
        assertEquals("admin", values[1]);
        assertEquals("$2a$bcrypt-hash", values[2]);
    }

    @Test
    void warnsButDoesNotFailWhenPasswordMissing() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

        bootstrap(true, "").run(null);

        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }
}
