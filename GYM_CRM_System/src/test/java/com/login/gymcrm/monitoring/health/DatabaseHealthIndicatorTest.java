package com.login.gymcrm.monitoring.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void healthIsUpWhenDatabaseReturnsOne() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        DatabaseHealthIndicator indicator = new DatabaseHealthIndicator(jdbcTemplate);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.health().getDetails()).containsEntry("database", "reachable");
    }

    @Test
    void healthIsDownWhenDatabaseCheckThrows() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenThrow(new RuntimeException("db down"));
        DatabaseHealthIndicator indicator = new DatabaseHealthIndicator(jdbcTemplate);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getDetails()).containsEntry("database", "unreachable");
    }
}
