package web.web.shoppingmall.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import web.web.admin.repository.FailureLogRepository;
import web.web.admin.repository.SuccessLogRepository;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;

    @GetMapping("/")
    public String home(){
        return "/AdminLTE-3.2.0-rc/src/_my/shoppingmall";
    }

    @GetMapping("/adminPage")
    public String adminPage(Model model){

        long success = successLogRepository.count();
        long failure = failureLogRepository.count();

        model.addAttribute("success", success);
        model.addAttribute("failure", failure);

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
