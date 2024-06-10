package kafka.kafka.admin.repository;

import kafka.kafka.admin.domain.log.SuccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuccessLogRepository extends JpaRepository<SuccessLog, Long> {
}
