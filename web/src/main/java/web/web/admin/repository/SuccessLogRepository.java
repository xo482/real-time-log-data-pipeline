package web.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.web.admin.domain.log.SuccessLog;

public interface SuccessLogRepository extends JpaRepository<SuccessLog, Long> {

    @Query("SELECT COUNT(s) FROM SuccessLog s WHERE s.scenario_id = :scenarioId")
    Long countByScenarioId(@Param("scenarioId") Long scenarioId);


    @Query(value = "SELECT COUNT(*) FROM success_log sl WHERE JSON_UNQUOTE(JSON_EXTRACT(sl.message, '$.date')) BETWEEN :startTime AND :endTime", nativeQuery = true)
    Long countLogsInTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Query(value = "SELECT COUNT(*) FROM success_log sl WHERE sl.scenario_id = :scenarioId AND JSON_UNQUOTE(JSON_EXTRACT(sl.message, '$.date')) BETWEEN :startTime AND :endTime", nativeQuery = true)
    Long countLogsInTimeRangeByScenarioId(@Param("scenarioId") Long scenarioId, @Param("startTime") String startTime, @Param("endTime") String endTime);

}
