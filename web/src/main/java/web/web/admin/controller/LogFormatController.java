package web.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import web.web.admin.domain.LogFormat;
import web.web.admin.repository.LogFormatRepository;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LogFormatController {

    private final LogFormatRepository logFormatRepository;

    @GetMapping("/logFormatPage")
    public String showLogFormatPage(Model model) {
        List<LogFormat> logFormats = logFormatRepository.findAll();
        model.addAttribute("logFormats", logFormats);
        return "AdminLTE-3.2.0-rc/src/_my/logFormatPage";
    }

    @PostMapping("/logFormatPage/saveKeys")
    @Transactional
    public String saveKeys(@RequestParam(value = "keys", required = false) List<String> keys) {
        LogFormat logFormat = new LogFormat();

        // 선택된 키들을 1로 설정
        if (keys != null) {
            for (String key : keys) {
                switch (key) {
                    case "idsite": logFormat.setIdsite(1); break;
                    case "e_a": logFormat.setE_a(1); break;
                    case "e_c": logFormat.setE_c(1); break;
                    case "rec": logFormat.setRec(1); break;
                    case "java": logFormat.setJava(1); break;
                    case "e_n": logFormat.setE_n(1); break;
                    case "pf_dm1": logFormat.setPf_dm1(1); break;
                    case "uadata": logFormat.setUadata(1); break;
                    case "e_v": logFormat.setE_v(1); break;
                    case "pf_tfr": logFormat.setPf_tfr(1); break;
                    case "memberId": logFormat.setMemberId(1); break;
                    case "send_image": logFormat.setSend_image(1); break;
                    case "res": logFormat.setRes(1); break;
                    case "qt": logFormat.setQt(1); break;
                    case "cookie": logFormat.setCookie(1); break;
                    case "ag": logFormat.setAg(1); break;
                    case "_id": logFormat.set_id(1); break;
                    case "pf_net": logFormat.setPf_net(1); break;
                    case "_refts": logFormat.set_refts(1); break;
                    case "pf_srv": logFormat.setPf_srv(1); break;
                    case "wma": logFormat.setWma(1); break;
                    case "pf_onl": logFormat.setPf_onl(1); break;
                    case "_idn": logFormat.set_idn(1); break;
                    case "fla": logFormat.setFla(1); break;
                    case "ca": logFormat.setCa(1); break;
                    case "urlref": logFormat.setUrlref(1); break;
                    case "realp": logFormat.setRealp(1); break;
                    case "h": logFormat.setH(1); break;
                    case "pf_dm2": logFormat.setPf_dm2(1); break;
                    case "m": logFormat.setM(1); break;
                    case "url": logFormat.setUrl(1); break;
                    case "r": logFormat.setR(1); break;
                    case "s": logFormat.setS(1); break;
                    case "pdf": logFormat.setPdf(1); break;
                    case "date": logFormat.setDate(1); break;
                }
            }
        }

        // 데이터베이스에 저장
        logFormatRepository.save(logFormat);

        return "redirect:/logFormatPage";
    }


}
