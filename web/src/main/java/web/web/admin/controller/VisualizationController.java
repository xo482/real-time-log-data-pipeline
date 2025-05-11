package web.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.web.admin.dto.ScenarioRatioDto;
import web.web.admin.service.VisualizationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scenario")
@RequiredArgsConstructor
public class VisualizationController {

    private final VisualizationService visualizationService;

    @GetMapping("/scenario_ratios")
    public Map<Long, List<ScenarioRatioDto>> getAllScenarioHistories() {
        return visualizationService.getAllScenarioHistories();
    }

}
