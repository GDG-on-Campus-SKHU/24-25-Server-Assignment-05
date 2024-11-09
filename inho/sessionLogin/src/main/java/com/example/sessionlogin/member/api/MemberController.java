package com.example.sessionlogin.member.api;

import com.example.sessionlogin.member.api.dto.request.LoginReqDto;
import com.example.sessionlogin.member.api.dto.response.MemberLoginResDto;
import com.example.sessionlogin.member.api.dto.response.UserInfo;
import com.example.sessionlogin.member.application.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 통합된 회원가입 및 사용자 정보 저장 API
     * @param userInfo 사용자 정보 DTO
     * @return 성공 시 회원 로그인 응답 DTO와 200 OK 반환
     */
    @PostMapping("/join")
    public ResponseEntity<MemberLoginResDto> registerOrUpdateUserInfo(@RequestBody UserInfo userInfo) {
        MemberLoginResDto response = memberService.saveUserInfo(userInfo);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 로그인 API
     * @param loginReqDto 로그인 요청 DTO
     * @return 성공 시 회원 로그인 응답 DTO와 200 OK 반환
     */
    @PostMapping("/login")
    public ResponseEntity<MemberLoginResDto> login(@RequestBody LoginReqDto loginReqDto, HttpServletRequest request) {
        MemberLoginResDto response = memberService.login(loginReqDto);

        // 세션을 가져오거나 생성합니다.
        HttpSession session = request.getSession(true);

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginReqDto.loginId(), null, null);

        // SecurityContext에 인증 정보를 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        // 세션에 SecurityContext 저장
        SecurityContextHolder.setContext(context);
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);

        return ResponseEntity.ok(response);
    }
}
