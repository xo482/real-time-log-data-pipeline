package web.web.shoppingmall.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import web.web.admin.service.VisualizationService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final VisualizationService visualizationService;

    @GetMapping("/")
    public String home(){
        return "AdminLTE-3.2.0-rc/src/_my/shoppingmall";
    }

    @GetMapping("/adminPage")
    public String adminPage(Model model){

        // 회원 수 전달
        Long memberCount = visualizationService.memberCount();
        model.addAttribute("memberCount", memberCount);

        // 새로운 주문 건 수 전달
        Long orderCount = visualizationService.dayOrderCount();
        model.addAttribute("orderCount", orderCount);

        Long visitorCount = visualizationService.pageView();
        model.addAttribute("visitorCount", visitorCount);

        return "AdminLTE-3.2.0-rc/src/_my/index";
    }


    @GetMapping("/product_detail/p_name_Adidas Spezial Handball Collegiate Navy")
    public String detailPage(){
        return "AdminLTE-3.2.0-rc/src/_my/product_detail/p_name_Adidas Spezial Handball Collegiate Navy";
    }

    @GetMapping("/list")
    public String list(){
        return "AdminLTE-3.2.0-rc/src/_my/shopping_list";
    }


    @GetMapping("/shopping_list")
    public String shopping_list() {
        return "AdminLTE-3.2.0-rc/src/_my/shopping_list";
    }


    @GetMapping("/myLogin")
    public String myLogin() {
        return "AdminLTE-3.2.0-rc/index";
    }

    @GetMapping("/api/live-count")
    @ResponseBody
    public Map<Long, Long> getLiveCounts() {
        return visualizationService.liveCount();
    }

    @GetMapping("/api/logCount")
    @ResponseBody
    public Map<String, Long> getLogCount() {
        Long[] countList = visualizationService.logCount();
        Map<String, Long> result = new HashMap<>();
        result.put("success", countList[0]);
        result.put("failure", countList[1]);
        return result;
    }
}
