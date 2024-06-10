package web.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.web.admin.domain.log.SuccessLog;

public interface SuccessLogRepository extends JpaRepository<SuccessLog, Long> {
}
