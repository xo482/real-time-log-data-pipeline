package web.web.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import web.web.admin.domain.Scenario;
import web.web.admin.repository.ScenarioRepository;

import java.util.List;

@Controller
public class ScenarioController {

    @Autowired
    private ScenarioRepository scenarioRepository;

    @GetMapping("/managerCenter")
    public String getScenarios(Model model) {
        List<Scenario> scenarios = scenarioRepository.findAll();
        model.addAttribute("scenarios", scenarios);
        return "AdminLTE-3.2.0-rc/src/_my/widgets";
    }
}
