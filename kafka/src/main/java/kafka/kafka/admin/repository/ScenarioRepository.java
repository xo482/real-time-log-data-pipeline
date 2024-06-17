package kafka.kafka.admin.repository;

import kafka.kafka.admin.domain.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    @Query("SELECT s.id FROM Scenario s")
    List<Long> findAllIds();
}
