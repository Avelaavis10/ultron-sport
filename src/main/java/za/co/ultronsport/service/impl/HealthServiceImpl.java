package za.co.ultronsport.service.impl;

import java.sql.Connection;
import java.time.Instant;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;
import za.co.ultronsport.service.HealthService;
import za.co.ultronsport.web.dto.HealthReadinessResponse;

@Service
public class HealthServiceImpl implements HealthService {

    private final DataSource dataSource;

    public HealthServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public HealthReadinessResponse readiness() {
        String databaseStatus = databaseStatus();
        String status = "UP".equals(databaseStatus) ? "READY" : "NOT_READY";
        return new HealthReadinessResponse(status, databaseStatus, Instant.now());
    }

    private String databaseStatus() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? "UP" : "DOWN";
        } catch (Exception ex) {
            return "DOWN";
        }
    }
}
