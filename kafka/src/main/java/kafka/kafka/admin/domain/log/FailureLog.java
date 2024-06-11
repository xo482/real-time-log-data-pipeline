package kafka.kafka.admin.domain.log;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class FailureLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "failure_log_id")
    private Long id;

    private Long scenario_id;

    @Column(name = "message", nullable = false)
    private String message;

    public FailureLog() {}

    public FailureLog(String message) {
        this.message = message;
    }
}

