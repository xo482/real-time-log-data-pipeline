package web.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.web.admin.domain.log.FailureLog;

public interface FailureLogRepository extends JpaRepository<FailureLog, Long> {

    @Query("SELECT COUNT(f) FROM FailureLog f WHERE f.scenario_id = :scenarioId")
    Long countByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query(value = "SELECT COUNT(*) FROM failure_log fl WHERE JSON_UNQUOTE(JSON_EXTRACT(fl.message, '$.date')) BETWEEN :startTime AND :endTime", nativeQuery = true)
    Long countLogsInTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
