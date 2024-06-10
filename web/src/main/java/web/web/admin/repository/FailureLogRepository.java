package web.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.web.admin.domain.log.FailureLog;

public interface FailureLogRepository extends JpaRepository<FailureLog, Long> {
}
