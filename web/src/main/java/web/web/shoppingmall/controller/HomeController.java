package web.web.shoppingmall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(){
        return "/AdminLTE-3.2.0-rc/src/_my/shoppingmall";
    }

    @GetMapping("/adminPage")
    public String adminPage(){
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
