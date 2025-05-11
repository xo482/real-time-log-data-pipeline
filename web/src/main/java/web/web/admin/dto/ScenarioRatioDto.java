package web.web.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ScenarioRatioDto {

    @JsonProperty("scenario_id")
    private Long scenarioId;

    @JsonProperty("success_ratio")
    private double successRatio;

    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("success_count")
    private int successCount;

    @JsonProperty("updated_at")
    private String updatedAt; // Spark 기준 수집 시각 (갱신 여부 판단용)

    private ZonedDateTime collectedAt; // Spring 기준 보정 시각 (그래프 시각 기준)
}

