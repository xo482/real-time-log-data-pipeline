package kafka.kafka.repository;

import kafka.kafka.domain.LogFormat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogFormatRepository extends JpaRepository<LogFormat, Long> {
}
