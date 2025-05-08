package web.web.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
    private String updatedAt;
}

