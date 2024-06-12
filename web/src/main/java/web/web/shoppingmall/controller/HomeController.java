package web.web.shoppingmall.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import web.web.visualization.VisualizationService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final VisualizationService visualizationService;

    @GetMapping("/")
    public String home(){
        return "/AdminLTE-3.2.0-rc/src/_my/shoppingmall";
    }

    @GetMapping("/adminPage")
    public String adminPage(Model model){

        Long[] countList = visualizationService.logCount();

        model.addAttribute("success", countList[0]);
        model.addAttribute("failure", countList[1]);

        return "/AdminLTE-3.2.0-rc/src/_my/index";
    }


    @GetMapping("/product_detail/p_name_Adidas Spezial Handball Collegiate Navy")
    public String detailPage(){
        return "/AdminLTE-3.2.0-rc/src/_my/product_detail/p_name_Adidas Spezial Handball Collegiate Navy";
    }

    @GetMapping("/list")
    public String list(){
        return "/AdminLTE-3.2.0-rc/src/_my/shopping_list";
    }
}
