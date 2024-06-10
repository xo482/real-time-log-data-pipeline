package kafka.kafka.admin.repository;

import kafka.kafka.admin.domain.log.FailureLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailureLogRepository extends JpaRepository<FailureLog, Long> {
}
