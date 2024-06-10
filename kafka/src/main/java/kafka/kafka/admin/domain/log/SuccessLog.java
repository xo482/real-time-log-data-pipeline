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

    @Column(name = "message", nullable = false)
    private String message;

    public SuccessLog() {}

    public SuccessLog(String message) {
        this.message = message;
    }
}

