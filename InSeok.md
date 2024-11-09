##사용자 관점에서의 로그인 과정 흐름

사용자가 로그인 페이지에 접속해서 ID와 비밀번호를 입력하게 되면 MemberController
postMapping("/login") 메소드 실행하고 memberService.login으로 loginReqDto를 전달
MemberServices class 메소드 login에서 memberRepository.findByLoginId(loginReqDto.loginId()) 실행하고
거기서 로그인 아이디와 비밀번호를 db에서 비교



##성공 
같으면은 로그인 성공 응답 Http로 ok를 return해준다.

##실패
같은 로그인 아이디가 없으면은 IllegalArgumentException("회원 정보를 찾을 수 없습니다.") 실행
로그인 아이디가 있으면은 alidationPassword 메소드를 실행해서 id값과 같은 비밀번호와 인자값 비교
다르면 IllegalStateException("비밀번호가 일치하지 않습니다.");



##로그인 유지'가 어떻게 사용자 관점에서 나타나는지 서술하세요.

로그인을 성공한다면 사용자는 웹페이지 내에서 자신의 권한 내 허용된 페이지에 대한 여러 요청을 보낼 때 자신의 계정이 유지되고
서비스 사용



##서버가 지속적으로 인식하기 위해 쿠키와 세션이 어떻게 사용되는지 설명하세요
사용자가 로그인을 요청해서 인증을 성공
이후 로그인이 잘되어서 본인 인증이 되면 requset.getSession(true); 세션을 생성
Authentication 객체 생성
SecurityContext에 인증 정보를 설정
이때 세션 ID를 클라이언트에게 쿠키로 전달하고 웹브라우저에 쿠키를 저장한다. 이후 계속 요청이 올때 쿠키 전송 
사용자가 API호출시  쿠키를 HTTP 요청 헤더에 포함시켜 서버로 전송 그럼 서버는 세션 ID를 사용해 SecurityContext에서 사용자의 인증 정보를 찾아 API 요청을 처리, 권한 검사도 진행



##세션 기반 로그인에서 어떤 역할을 하는지 구체적으로 서술하고, 특히 세션 ID가 쿠키에 저장되어 클라이언트와 서버 간에 전달되는 과정에 대해 설명하세요.

request.getSession(true)를 통해 세션에 저장해 서버는 세션 ID로 쿠키를 브라우저에 저장해 사용자의 인증 상태를 계속 인식할 수 있다.
SecurityContext에 인증 정보 설정해  SecurityContext를 생성하여 생성된 `Authentication`을 보관
SecurityContextHolder에서는 세션을 생성하거나 기존 세션을 가져오고, 이 세션은 서버에 유지되며 클라이언트의 브라우저에는 세션 ID가 쿠키로 저장된다.



##세션을 구현할 때 보안적으로 고려해야할 부분이 뭐가 있을까요?
.csrf(AbstractHttpConfigurer::disable) 이부분의 csrf 부분을 활성화 시킨다

로그아웃 시 세션을 완전히 무효화하고 세션 ID를 재사용하지 않도록 해야 함으로 
이때, 스프링 시큐리티 설정에서 invalidateHttpSession(true)와 deleteCookies("JSESSIONID") 설정을 사용하여 세션과 관련 쿠키를 삭제