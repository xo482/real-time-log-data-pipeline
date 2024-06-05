package web.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.web.admin.domain.LogFormat;

public interface LogFormatRepository extends JpaRepository<LogFormat, Integer> {
}
