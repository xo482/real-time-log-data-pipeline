package web.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import web.web.admin.domain.LogFormat;
import web.web.admin.domain.Scenario;
import web.web.admin.repository.LogFormatRepository;
import web.web.admin.repository.ScenarioRepository;

import java.util.List;

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

    @GetMapping("/settings")
    public String getLogFormats(Model model) {
        List<LogFormat> logFormats = logFormatRepository.findAll();
        model.addAttribute("logFormats", logFormats);
        return "AdminLTE-3.2.0-rc/src/_my/settings";
    }
}
