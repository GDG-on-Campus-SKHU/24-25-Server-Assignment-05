### Q1. 사용자가 로그인 페이지에 접속하여 ID와 비밀번호를 입력하는 순간부터 로그인에 성공하고 실패하는 순간까지의 과정을 설명하세요.

    -> 1. 사용자는 로그인 페이지에서 ID와 PWD를 입력 후에 '로그인 버튼' 클릭
       2. '로그인 버튼'이 클릭되고, 동시에 POST요청 전송
       3. Spring Security가 UPAF(UsernamePasswordAthenticationFilter)로 POST 요청을 가로챔
       4. UPAF는 2가지 일을 처리함.
          1) 사용자의 username와 password를 추출하기
          2) UPAT(UsernamePasswordAthenticationToken)인증 토큰 발행
       5. AuthenticationManager의 AuthenticationProvider에게 4번에서 추출한 username과 password로 인증을 해달라고 위임
       6. AuthenticationProvider는 위임받은 username과 password를 UserDetailsService(username조회 후 UserDetails반환)와 PasswordEncoder(password를 DB의 password랑 비교)를 이용해 인증 수행
            *UserDetails: 추후에 세션에 저장될 Authentication에 들어갈 객체
       7. UserDetatils의 username이 DB에 일치하지 않거나, PasswordEncoder로 암호화한 password랑 DB의 암호화된 password랑 일치하지 않은 경우에는 로그인 실패, 두 경우 모두 일치한다면 로그인 성공
       성공했을 경우
       1. UserDetails를 저장한 Authentication객체를 SecurityContext에 저장
       2. 빈 세션에 SecurityContext에 Authentication정보 저장
       
###inho 코드로 다시 설명

    -> 1. 사용자가 ID와 PASSWORD를 누른다 -> MemberController에서 @PostMapping 요청, ID와PASSWORD는 LoginReqDto에 담겨서 서버로 옴.
       2. LoginReqDto를 매개변수로 memberService의 login메서드 실행, LoginReqDto의 ID를 memberRepository의 findByLoginId로 인해 DB에 존재하는지 확인(없다면 IllegalArgumentException 발생), LoginReqDto의 PASSWORD는 validationPassword의 인자로 가면서 member엔티티의 비번이랑 일치하는지 확인 (위에 서술한 PasswordEncoder로직)
       3. 인증이 완료 되었음 -> 세션에 저장할 준비 !
       4. HttpSession타입의 session을 생성해주고, authentication객체에 UsernamePasswordAuthenticationToken을 이용해서 사용자 정보인 loginReqDto의 ID와 인증 정보(credentials,authorities)를 담아준다.
       5. SecurityContextHolder의 메서드 createEmptyContext()를 이용하여 빈 홀더를 SecurityContext객체에 넣어준다. 이후, 생성한 빈 홀더에 사용자의 인증정보를 담은 authentication을 담아준다.
       6. 이제 SecurityContextHolder.setCotext 메서드를 사용자 인증정보를 담은 authenticataion을 담은 홀더를 세팅해주고, 3번에 세션에 저장할 준비를 하면서 생성한 빈 session에 세팅이 완료된 홀더 context를 담아준다.

###로그인의 실패했을 경우(회원미가입, 비밀번호 불일치)

    -> 1. MemberService의 login메서드에서 사용자에게 받은 LoginReqDto의 ID가 memberRepository에 없는 경우 "회원 정보를 찾을수 없습니다"를 포함한 IllegalArgumentException 발생
       2. MemberService의 calidationPassword에서 사용자가 입력한 패스워드와 member객체의 패스워드를 비교하는데(암호화 상태)일치하지 않는다면(!encoder.matches())"비밀번호가 일치하지 않습니다."를 포함한 IllgalStateException 발생
       
### Q2. 로그인 성공 후 사용자에게 제공되는 경험을 설명하고, 특히 '로그인 유지'가 어떻게 사용자 관점에서 나타나는지 서술하세요.
    
    -> 1. 로그인이 성공하면 이전에 접근하려 했던 페이지나 홈 페이지로 '리다이렉트'
       2. 인가 정보에 따라 접근 권한이 필요한 URL, 메서드에 접근 가능
       3. '로그인 유지'는 새로고침이나 브라우저를 다시 켰을 때도 로그인 상태나 정보가 유지됨. 

### Q3. 로그인한 사용자를 서버가 지속적으로 인식하기 위해 쿠키와 세션이 어떻게 사용되는지 설명하세요. (서버 딴에서 api 호출할 때마다 어떤 일이 일어나는지.)

    -> 우선, 사용자가 로그인을 완료해 세션이 생성되고, 세션ID가 클라이언트 브라우저 측의 쿠키로 저장이 되어 있는 상태를 가정한다.
       1. 클라이언트 브라우저 측에서 서버 측으로 API 요청함과 동시에 JSESSIONID가 담긴 쿠키를 전송
       2. 서버 측에서는 쿠키를 받아 새로운 JSESSIONID를 생성하지 않고, 쿠키에 담긴 JSSEIONID값을 기준으로 세션 메모리 영역에 '상태를 유지'할 값들을 저장할 수 있게 된다.
       3. 즉, 첫 로그인 요청시에만 세션을 생성하고 전송하게 되며, 이후의 요청은 클라이언트 브라우저 측의 쿠키를 전송받아 JSESSIONID값을 기준으로 상태를 유지하게됨.

### Q4. 쿠키가 세션 기반 로그인에서 어떤 역할을 하는지 구체적으로 서술하고, 특히 세션 ID가 쿠키에 저장되어 클라이언트와 서버 간에 전달되는 과정에 대해 설명하세요.

     쿠키의 역할을 논하기 위해서 클라이언트의 첫번 째 요청과 두번 이후의 요청들로 나누어서 살펴본다.    
     첫번 째 요청 -> 사용자 정보를 담아 API 요청, 서버에서는 사용자 정보를 토대로 세션을 생성, 인증, 인가 정보를 저장함. 그리고 쿠키에 SESSIONID를 담아 클라이언트에게 반환.
     두번 째 요청 -> 사용자 정보를 담는 것이 아닌, 이전의 요청에서 받은 쿠키의 SESSIONID를 보냄. 서버는 받은 SESSIONID를 받아 기존 session을 유지. 다시 SESSIONID를 쿠키담아 클라이언트에 전송
     세번 째 요청 -> 두번 째 요청 반복.
    
    -> 세션이 설정되는 원리(사용자 정보를 토대로 세션을 설정 or 쿠키의 SESSIONID를 토대로 세션을 설정)
     <!--(HttpServletRequest request는 클라이언트의 HTTP요청(login의 파라미터 dto랑 request인데, request는 뭐가 담겨져있을까?) 
     (HttpServletRequest인터페이스 객체 request는 getSession메서드가 있다.)-->
     public ResponseEntity<MemberLoginResDto> login(@RequestBody LoginReqDto loginReqDto, HttpServletRequest request)는 login메서드의 정의이다.
     우리는 사용자로부터 ID와PASSWORD가 담겨 있는 Dto와 HttpServletRequest객체를 받게 되는데, 
     클라이언트의 JSESSIONID는 request에 담기게 되고, HttpSession session = request.getSession(true);에 의해 session에 HttpSession객체를 저장하게 되는데, 만약 세션이 존재한다면 기존의 Session을 반환, 세션이 없다면 (true)설정에 의해 새로운 세션을 생성하게 된다.
     그렇다면 선술했듯이, session의 사용자 정보가 담기고, 그 사용자 정보를 토대로 인가를 설정하게 되는데, 기존 session을 반환했다면, 새로운 세션이 발급된 것이 아닌 기존의 session이 '유지' 되는 것이다.
  
     예를 들어 보자.
      ->
     Morty's shop 쇼핑몰에 로그인을 진행하고, 장바구니에 멋진 양말 3개를 담은 상태를 가정해보자, 브라우저를 껐다가 다시 켰다. 그렇다면 클라이언트인 Morty's shop의 장바구니는  서버에 API요청을 보낼 것이다.(이 사용자가 권한이 있는지 없는지) 
     이 때, '쿠키'에 세션ID를 담아 서버에 보내게 된다. 그렇다면 서버는 클라이언트가 쿠키로 전송한 세션ID를 HttpServletRequest request에 담기게 되고, 이는 session을 생성하거나 기존의 세션을 반환하거나(request.getSession(true))를 판단할 때의 기존의 세션을 반환하게 된다.
     그렇다면 클라이언트는 이전에 장바구니에 양말을 3개 담던 인가 정보를 다시 받게되는 것이고, 로그인은 유지된다. 
   
### Q5. 세션을 구현할 때 보안적으로 고려해야할 부분이 뭐가 있을까요? (5주차 코드에는 생략되어 있음) 찾아보시기 바랍니다.

    1. 세션을 통해 로그인을 구현하였을 때, 세션과 쿠키로 로그인이 유지된다. 이 로그인 유지는 영구적인가? 
       -> NO. 브라우저가 종료되면 쿠키가 삭제되어 로그인 유지 불가능
      
    2. 그렇다면 브라우저가 종료되지 않으면 쿠키는 삭제되지 않는가?
        -> YES. 
       
    3. 브라우저가 계속 켜져 있을 경우, 클라이언트는 접근 권한에 따라 영구적으로 서버 API를 호출할 수 있게된다. 만약 개인PC가 아닌 공용PC에서 이와 같은 로직이 구현되었을 때, 보안상 문제가 발생할 수 있지 않을까?
        -> 해결 방법
          1. Properties 또는 yml파일에 세션의 만료 기간을 설정하자. ex)server.servlet.session.timeout=30m
          2. SecurityConfig클래스에 RememberMe 기능을 사용하여 만료 기간을 설정하자 !
