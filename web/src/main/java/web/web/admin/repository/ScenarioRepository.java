package web.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.web.admin.domain.Scenario;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
}
