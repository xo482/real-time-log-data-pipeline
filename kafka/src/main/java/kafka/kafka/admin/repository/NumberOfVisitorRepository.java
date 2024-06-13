package kafka.kafka.admin.repository;

import kafka.kafka.admin.domain.NumberOfVisitors;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NumberOfVisitorRepository extends JpaRepository<NumberOfVisitors,Long> {
}
