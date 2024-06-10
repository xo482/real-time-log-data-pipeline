package web.web.admin.domain.log;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class SuccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "success_log_id")
    private Long id;

    @Column(name = "message", nullable = false)
    private String message;

    public SuccessLog() {}

    public SuccessLog(String message) {
        this.message = message;
    }
}

