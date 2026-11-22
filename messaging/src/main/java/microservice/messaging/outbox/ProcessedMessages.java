package microservice.messaging.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ProcessedMessages {
    private final JdbcTemplate jdbc;

    public ProcessedMessages(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean firstTime(UUID messageId) {
        return jdbc.update("INSERT INTO processed_message(id) VALUES (?) ON CONFLICT (id) DO NOTHING", messageId) == 1;
    }
}
