package microservice.messaging.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPublisher(JdbcTemplate jdbc, KafkaTemplate<String, String> kafka) {
        this.jdbc = jdbc;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${outbox.delay:1000}")
    @Transactional
    public void publish() {
        var rows = jdbc.queryForList("SELECT * FROM outbox WHERE sent_at IS NULL ORDER BY created_at, id LIMIT 20 FOR UPDATE SKIP LOCKED");
        for (var row : rows) {
            try {
                kafka.send((String) row.get("topic"), (String) row.get("message_key"),
                        (String) row.get("payload")).get(10, java.util.concurrent.TimeUnit.SECONDS);
                jdbc.update("UPDATE outbox SET sent_at = CURRENT_TIMESTAMP WHERE id = ?", row.get("id"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("Outbox {} will be retried: {}", row.get("id"), e.getMessage());
                return;
            }
        }
    }
}
