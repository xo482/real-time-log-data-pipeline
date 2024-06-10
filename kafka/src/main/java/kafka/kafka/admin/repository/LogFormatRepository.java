package kafka.kafka.admin.repository;

import kafka.kafka.admin.domain.LogFormat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogFormatRepository extends JpaRepository<LogFormat, Long> {
}
