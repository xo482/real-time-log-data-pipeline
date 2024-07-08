package web.web.shoppingmall.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import web.web.shoppingmall.domain.Member;
import web.web.shoppingmall.repository.MemberRepository;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {


    private final MemberRepository memberRepository;

    @GetMapping("/login")
    public String login() {
        return "AdminLTE-3.2.0-rc/src/_my/login";
    }

    @GetMapping("/sign_up")
    public String sign_up() {
        return "AdminLTE-3.2.0-rc/src/_my/sign_up";
    }


    @PostMapping("/sign_up")
    public String signUp(@RequestBody Member member) {
        try {
            System.out.println("Received user: " + member.getEmail());
            memberRepository.save(member);
            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    // 로그인 처리 메소드
    @PostMapping("/login")
    public ModelAndView login(@RequestParam("email") String email, @RequestParam("password") String password, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(email);
        if (member != null && member.getPassword().equals(password)) {
            ModelAndView modelAndView = new ModelAndView("redirect:/"); // 로그인 성공 시 이동할 페이지

            // 로그인 성공했다면 사용자 아이디 쿠키에 삽입
            ResponseCookie memberIdCookie = ResponseCookie.from("memberId", String.valueOf(member.getId()))
                    .path("/")
                    .httpOnly(false)
                    .secure(false) // Secure 속성을 false로 설정
                    .build();

            response.addHeader("Set-Cookie", memberIdCookie.toString());
            log.info("Set-Cookie: " + memberIdCookie.toString());

            return modelAndView;
        } else {
            ModelAndView modelAndView = new ModelAndView("AdminLTE-3.2.0-rc/src/_my/login");
            modelAndView.addObject("error", "로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다.");
            return modelAndView;
        }
    }
}
