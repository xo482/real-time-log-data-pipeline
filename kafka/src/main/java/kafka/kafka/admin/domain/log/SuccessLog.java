package kafka.kafka.admin.domain.log;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class SuccessLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "success_log_id")
    private Long id;

    private Long scenario_id;

    @Column(name = "message", nullable = false)
    private String message;

    public SuccessLog() {}

    public SuccessLog(Long scenario_id, String message) {
        this.scenario_id = scenario_id;
        this.message = message;
    }
}

