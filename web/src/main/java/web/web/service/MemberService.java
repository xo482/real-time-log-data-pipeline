package web.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.web.domain.Member;
import web.web.repository.MemberRepository;

import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    public Optional<Member> findMemberById(Long id) {
        return memberRepository.findById(id);
    }
}
