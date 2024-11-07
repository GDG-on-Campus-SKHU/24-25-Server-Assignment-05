1. 사용자가 로그인 페이지에 접속하여 ID와 비밀번호를 입력하는 순간부터 로그인에 성공하고 실패하는 순간까지의 과정을 설명하세요.

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
       
2. 로그인 성공 후 사용자에게 제공되는 경험을 설명하고, 특히 '로그인 유지'가 어떻게 사용자 관점에서 나타나는지 서술하세요.
    
   -> 1. 로그인이 성공하면 이전에 접근하려 했던 페이지나 홈 페이지로 '리다이렉트'
      2. 인가 정보에 따라 접근 권한이 필요한 URL, 메서드에 접근 가능
      3. '로그인 유지'는 새로고침이나 브라우저를 다시 켰을 때도 로그인 상태나 정보가 유지됨. 

3. 로그인한 사용자를 서버가 지속적으로 인식하기 위해 쿠키와 세션이 어떻게 사용되는지 설명하세요. (서버 딴에서 api 호출할 때마다 어떤 일이 일어나는지.)
   -> 1.
    


