package com.example.sessionlogin.member.application;

import com.example.sessionlogin.member.api.dto.request.JoinReqDto;
import com.example.sessionlogin.member.api.dto.request.LoginReqDto;
import com.example.sessionlogin.member.api.dto.response.MemberLoginResDto;
import com.example.sessionlogin.member.api.dto.response.UserInfo;
import com.example.sessionlogin.member.domain.Member;
import com.example.sessionlogin.member.domain.Role;
import com.example.sessionlogin.member.domain.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 예외처리 로직 구현
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public MemberLoginResDto saveUserInfo(UserInfo userInfo) {
        validateNotFoundLoginId(userInfo.loginId());

        Member member = memberRepository.findByLoginId(userInfo.loginId())
                .orElseGet(() -> createMember(userInfo));

        return MemberLoginResDto.from(member);
    }

    private void validateNotFoundLoginId(String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            throw new IllegalStateException("로그인 ID가 필요합니다.");
        }
    }

    private Member createMember(UserInfo userInfo) {
        return memberRepository.save(
                Member.builder()
                        .loginId(userInfo.loginId())
                        .pwd(encoder.encode(userInfo.pwd()))
                        .name(userInfo.name())
                        .role(Role.ROLE_USER)
                        .build()
        );
    }

    public MemberLoginResDto login(LoginReqDto loginReqDto) {
        Member member = memberRepository.findByLoginId(loginReqDto.loginId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        validationPassword(loginReqDto.pwd(), member.getPwd());

        return MemberLoginResDto.from(member);
    }

    private void validationPassword(String rawPassword, String encodedPassword) {
        if (!encoder.matches(rawPassword, encodedPassword)) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
    }
}
