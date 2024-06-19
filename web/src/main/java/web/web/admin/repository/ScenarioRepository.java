package web.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import web.web.admin.domain.Scenario;

import java.util.List;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    @Query("SELECT s.id FROM Scenario s")
    List<Long> findAllIds();
}
