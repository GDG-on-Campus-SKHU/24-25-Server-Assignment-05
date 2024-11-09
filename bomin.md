- 사용자 관점에서의 로그인 과정 흐름
    - 사용자가 로그인 페이지에 접속하여 ID와 비밀번호를 입력하는 순간부터 로그인에 성공하고 실패하는 순간까지의 과정을 설명하세요. (2경우 전부)
1) 로그인 성공: 로그인 페이지 접속 -> ID와 비밀번호 입력 -> enter나 확인을 누를시 접속 성공

1. loginId와 pw를 입력 후, 로그인 API인 api/members/login(MemberController)으로 로그인 정보를 request(요청)한다.
2. Id와 pw가 loginReqDto에 넘어와 (MemberLoginResDto response = memberService.login(loginReqDto);)
3. MemberService에 있는 login MemberRepository에 있는 findByLoginId를 이용하여 DB에서 존재확인을 한다.
4. DB에 있을 시(조회 된다면) -> validationPassword 메소드에 있는 encoder.mathches를 사용자가 입력한 pw(rawPassword)와 저장된 비밀번호(encodePassword)가 일치하는지 확인한다.
5. DB에 없을 시(조회 안되면) -> IllegalArgumentException에서 회원정보를 찾을 수 없다는 오류가 발생하게 된다.
6. 세션 가져오거나 생성 -> HttpSession session = request.getSession(true);
7. UsernamePasswordAuthenticationToken에 loginReqDto에 있는 id를 authentication에 담아준다.
8. SecurityContext(사용자 인증 정보 저장) context에 빈 상태의 SecurityContext 객체 생성
9. 실제 인증 정보(id,pw를 담고 있는 authentication) 넣어주기 -> setAuthentication(authentication)
10. SecurityContextHolder.setContext(context);를 호출하여 SecurityContext 설정
11. session.setAttribute("SPRING_SECURITY_CONTEXT",context);를 통해 인증 정보를 세션에 저장한다

2) 로그인 실패: 로그인 페이지 접속 -> ID와 비밀번호 입력 -> 오류 발생
1. Id에 문제가 있는 경우: Repository에서 Id를 찾으며 없을시, IllegalArgumentException발생 -> 회원 정보를 찾을 수 없습니다.
2. 비밀번호가 일치하지 않는 경우: validationPassword에서 DB가 없을시, IllegalArgumentException발생 -> 비밀번호가 일치하지 않습니다.
3. 로그인 아이디를 쓰지 않을 시 (null값일 때): validateNotFoundLoginId 메소드 -> 로그인 ID가 필요합니다.

    - 로그인 성공 후 사용자에게 제공되는 경험을 설명
로그인 성공시, 사용자에게 맞춤화된 화면이 제공된다. 또한 비회원일 시 접근 불가능한 곳에 들어갈 수 있다.

    - '로그인 유지'가 어떻게 사용자 관점에서 나타나는지 서술하세요.
1. 새로고침시 로그인 유지가 일어남.
2. 새로운 페이지를 열어, 해당 사이트에 연결해도 로그인 유지가 일어난다.
3. 해당 페이지를 닫고, 일정시간 내에 다시 페이지에 연결할 시 로그인이 유지된다.


- 서버가 로그인한 사용자를 인식하는 방식
    - 로그인한 사용자를 서버가 지속적으로 인식하기 위해 쿠키와 세션이 어떻게 사용되는지 설명하세요. (서버 딴에서 api 호출할 때마다 어떤 일이 일어나는지.)
  -> 예) 새로고침, 사이트 다시 연결 등
1. 서버는 사용자가 로그인할 때마다 새로운 세션 생성. 고유의 session_id
2. session_id 는 쿠키에 저장되어 클라이언트에 전송된다. -> JSESSIONID라는 이름의 쿠키가 사용된다
   로그인 성공 -> (클라이언트 -> 서버 요청) -> 로그인 확인하고 인증 성공시 세션 생성 -> id 생성 -> 쿠키에 저장하여 클라이언트에 전달

[결론]
사용자가 API를 요청할 때, 브라우저는 자동으로 쿠키를 포함하여 서버에 전송하기 때문에 서버는 쿠키에서 session_id를 얻고 이를 통해 사용자 인증상태를 확인하여 요청한다.

  - 쿠키가 세션 기반 로그인에서 어떤 역할을 하는지 구체적으로 서술하고, 특히 세션 ID가 쿠키에 저장되어 클라이언트와 서버 간에 전달되는 과정에 대해 설명하세요.
    쿠키: 클라이언트 로컬에 저장되는 키와 값이 들어있는 작은 데이터 파일(request시 자동으로 header를 넣어 서버에 전송됨.)
    세션: 서버에 중요한 정보를 보관하고 연결을 유지하는 방법. 사용자가 로그인할 때 생성된다.
    서버에서 세션을 생성하고, 클라이언트에게 세션 Id를 쿠키에 담아 전송한다.
[쿠키 역할]
  - HTTP는 기본적으로 무상태프로토콜이기에 독립적으로 처리한다. 즉, 연결한 요청 정보를 기억하지 않는다. -> 사용자별로 상태 정보 유지할 수 있게 해준다.
  - 쿠키의 Secure 속성을 사용하여 HTTPS를 통해서만 쿠키가 전송되도록 한다.
  - HttpOnly 속성을 설정하여 JS를 통한 쿠키의 접근을 제한한다.
1. 쿠키에서의 로그인 과정
   클라이언트 로그인 -> 로그인 성공 -> 서버에서 토큰 생성하고 클라이언트에 전달 -> 쿠키에 토큰 저장 -> 사이트에서 특정 행동을 할 때마다 클라이언트는 쿠키에서 토큰 가져와서 서버에 보낸다. -> 서버에서는 토큰이 유효한지 판단을 하며 클라이언트와 통신한다
2. 세션에서 로그인 과정
   클라이언트 로그인 -> 로그인 성공 -> 클라이언트 고유 session-id 생성하고 서버에 저장 -> 서버는 클라이언트에게 id 전달, 클라이언트는 id를 가져와서 서버와 통신 -> id가 유효한지 확인하며 클라이언트와 소통

+ 세션을 구현할 때 보안적으로 고려해야할 부분이 뭐가 있을까요? (5주차 코드에는 생략되어 있음) 찾아보시기 바랍니다.
1. CSRF보호: 사용자가 의도하지 않은 요철을 서버에 보내도록 유도하는 공격으로, Spring Security는 기본적으로 CSRF 토큰을 사용해 이를 방지한다
2. HTTPS 적용: HTTPS는 클라이언트와 서버 간의 통신을 암호화하여 중간자 공격을 방지한다. HTTPS를 적용하면 쿠키가 HTTPS 환경에서 주고받을 수 있으며, 통신 자체가 암호화되어 보안이 강화된다.
3. SameSite 설정: 쿠키가 동일한 사이트에서만 주고받을 수 있도록 설정하는 방법. (쿠키 속성: Strict, Lax, None) -> SameSite는 CSRF 공격 방지하는데 중요한 역할을 한다. 이를 통해 세션을 안전하게 관리