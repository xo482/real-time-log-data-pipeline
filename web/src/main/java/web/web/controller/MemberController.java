package web.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import web.web.domain.Member;
import web.web.service.MemberService;

import java.util.Optional;

@Controller
@Slf4j
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/select-member")
    public String selectMember(@RequestParam Long memberId, HttpServletResponse response) {
        Optional<Member> member = memberService.findMemberById(memberId);
        if (member.isPresent()) {
            Member selectedMember = member.get();

            ResponseCookie memberIdCookie = ResponseCookie.from("memberId", String.valueOf(selectedMember.getId()))
                    .path("/")
                    .httpOnly(false)
                    .secure(false) // Secure 속성을 false로 설정
                    .build();

            response.addHeader("Set-Cookie", memberIdCookie.toString());
            // 로그에 쿠키 정보 출력
            log.info("Set-Cookie: " + memberIdCookie.toString());
        }
        return "redirect:/"; // 리다이렉트할 페이지로 변경 가능
    }
}
