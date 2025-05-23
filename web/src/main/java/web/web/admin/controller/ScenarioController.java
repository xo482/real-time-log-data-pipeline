package web.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import web.web.admin.domain.LogFormat;
import web.web.admin.domain.Scenario;
import web.web.admin.domain.Status;
import web.web.admin.repository.LogFormatRepository;
import web.web.admin.repository.ScenarioRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioRepository scenarioRepository;
    private final LogFormatRepository logFormatRepository;

    @GetMapping("/managerCenter")
    public String getScenarios(Model model) {
        List<Scenario> scenarios = scenarioRepository.findAll();
        model.addAttribute("scenarios", scenarios);
        return "AdminLTE-3.2.0-rc/src/_my/widgets";
    }

    @GetMapping("/managerCenter/settings")
    public String getLogFormats(Model model) {
        List<LogFormat> logFormats = logFormatRepository.findAll();
        model.addAttribute("logFormats", logFormats);
        model.addAttribute("scenario", new Scenario()); // 폼에 바인딩할 객체 추가
        return "AdminLTE-3.2.0-rc/src/_my/settings";
    }

    @PostMapping("/managerCenter/createScenario")
    @Transactional
    public String createScenario(@ModelAttribute Scenario scenario) {
        // 유효한 필터만 포함
        scenario.setFilters(
                scenario.getFilters().stream()
                        .filter(filter -> filter.getLeft() != null && filter.getOperator() != null && filter.getRight() != null)
                        .collect(Collectors.toList())
        );
        scenario.setStatus(Status.RUN);
        scenarioRepository.save(scenario);
        return "redirect:/managerCenter";
    }


    @PostMapping("/managerCenter/scenarios/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        Scenario scenario = scenarioRepository.findById(id).orElseThrow(() -> new ExpressionException("Scenario not found"));
        if (status != null) {
            scenario.setStatus(Status.valueOf(status));
            scenarioRepository.save(scenario);
        }
        return "redirect:/managerCenter";
    }

    @GetMapping("/managerCenter/scenarios/{id}/delete")
    public String deleteScenario(@PathVariable Long id) {
        Scenario scenario = scenarioRepository.findById(id).orElseThrow(() -> new ExpressionException("Scenario not found"));
        scenarioRepository.delete(scenario);
        return "redirect:/managerCenter";
    }

    @GetMapping("/managerCenter/api/logFormat/{id}/filters")
    @ResponseBody
    public List<String> getLogFormatFilters(@PathVariable Long id) {
        LogFormat logFormat = logFormatRepository.findById(id).orElseThrow(() -> new RuntimeException("LogFormat not found"));
        List<String> filters = new ArrayList<>();

        Field[] fields = LogFormat.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(logFormat);
                if (value instanceof Integer && (Integer) value == 1) {
                    if ("date".equals(field.getName())) {continue;}
                    if ("memberId".equals(field.getName())) {
                        filters.add("age");
                        filters.add("gender");
                    } else {
                        filters.add(field.getName());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return filters.stream().distinct().collect(Collectors.toList());
    }
}
