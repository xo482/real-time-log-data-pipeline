package web.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.web.admin.domain.log.SuccessLog;

public interface SuccessLogRepository extends JpaRepository<SuccessLog, Long> {

    @Query("SELECT COUNT(s) FROM SuccessLog s WHERE s.scenario_id = :scenarioId")
    Long countByScenarioId(@Param("scenarioId") Long scenarioId);
}
