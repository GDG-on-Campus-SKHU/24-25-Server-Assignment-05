# 사용자 관점에서의 로그인 과정 흐름

---
![img_2.png](img_2.png)
### 로그인 과정
- 사용자가 로그인 화면을 통해 ID와 비밀번호 필드 입력 후 로그인 버튼을 누르면 입력된 정보가 서버로 전송된다.

(클라이언트에서 로그인 요청(POST /api/members/login)을 보내며, `memberService.login` 메소드에 사용자 ID와 비밀번호가 담긴 loginReqDto을 전달한다. `memberRepository.findByLoginId()`를 사용해 사용자가 존재하는 확인하고 없으면 `IllegalArgumentException` 발생,
`validationPassword()` 메소드에서 `encoder.matches()`를 사용하여 비밀번호 검증을 통해 일치하지 않으면 `IllegalStateException` 발생)

### 로그인 성공 시
- 전달받은 ID와 비밀번호가 DB에 저장된 정보와 비교해 인증 성공 시 로그인이 성공되어 메인 홈(로그인 성공 응답 HTTP)으로 리다이렉션 된다.

(이때 서버는 인증된 사용자에 대해 세션을 생성하고, `SecurityContext`를 세션에 저장하여 인증 상태를 관리, 클라이언트는 이후 요청에서 쿠키로 저장된 세션 ID를 이용해 인증 상태를 유지하며, 서버는 이를 통해 사용자를 식별.)

### 로그인 실패 시
- 서버에서 전달받은 ID 또는 비밀번호가 일치하지 않을 시 로그인 실패로 동일한 로그인 화면에서 로그인을 다시 시도할 수 있다.

### 사용자 관점에서의 로그인 성공 및 로그인 유지 경험
-  로그인을 성공한다면 사용자는 웹페이지 내에서 자신의 권한 내 허용된 페이지에 대한 여러 요청을 보낼 때 자신의 계정이 유지되고, 브라우저를 닫았다가 열어도 로그인 상태가 일정 기간동안 유지된다. 


# 서버가 로그인한 사용자를 인식하는 방식

![img_1.png](img_1.png)
---
### Q1 : 로그인한 사용자를 서버가 지속적으로 인식하기 위해 쿠키와 세션이 어떻게 사용되는지 설명하세요. (서버 딴에서 api 호출할 때마다 어떤 일이 일어나는지.)

1. 로그인 요청 처리와 인증 <br>
사용자가 입력한 ID와 비밀번호가 `MemberService.login`을 통해 DB 값과 비교되어 인증된다. 인증에 성공한다면 `MemberLoginResDto`를 통해 해당 사용자의 정보가 `MemberController.login` 메서드로 전달된다.


2. 세션 생성 <br>
`MemberController.login` 메서드에서 `request.getSession(true)`를 호출해 세션을 생성하거나 기존세션을 가져오고 이 세션은 서버에 유지되면서 클라이언트의 브라우저에는 세션 ID가 쿠키로 저장된다.


3. SecurityContext에 인증 정보 설정 <br>
`UsernamePasswordAuthenticationToken`를 통해 사용자의 인증 정보 및 권한 정보를 담은 `Authentication` 객체를 생성, `SecurityContext`를 생성한 후 생성된 `Authentication`를 보관한다.


4. SecurityContext를 세션에 저장 <br>
`SecurityContextHolder`에 사용자의 인증 정보가 담긴 `SecurityContext`를 설정 후, 이를 세션에 저장한다. 세션에 저장된 `SecurityContext`를 통해서 서버는 세션 ID로 사용자의 인증상태를 계속 인식할 수 있다. 

   

### Q2 : 쿠키가 세션 기반 로그인에서 어떤 역할을 하는지 구체적으로 서술하고, 특히 세션 ID가 쿠키에 저장되어 클라이언트와 서버 간에 전달되는 과정에 대해 설명하세요.



- 세션 ID가 쿠키에 저장되어 클라이언트와 서버 간에 전달되는 과정

1. 클라이언트가 로그인 요청을 할 때 서버는 사용자 인증에 성공 시 세션을 생성하고 세션 ID를 쿠키에 담아 클라이언트로 응답


2. 클라이언트는 서버에 요청 시에 세션 ID가 담긴 쿠키를 포함해 전송


3. 서버는 클라이언트가 전송한 세션 ID를 통해 해당 사용자 식별

즉 쿠키는 서버가 클라이언트를 식별할 수 있게 하는 세션 ID를 클라이언트에게 보내는 매개체 역할을 한다. 쿠키는 HTTP 요청 헤더에 포함되면서 서버로 전달되고, 서버는 전달받은 세션ID와 저장된 세션 데이터를 비교해 클라이언트를 식별하고 인증 상태를 유지하는 중요한 역할을 하는 것이다.

# 세션을 구현할 때 보안적으로 고려해야할 부분

---
- 세션 고정 공격 방지 <br>
로그인 후 세션 ID를 재발급해 세션 고정 공격을 방어해야 한다. Security 설정에서 `.sessionManagement().sessionFixation().changeSessionId()`를 통해 로그인할 때마다 새로운 세션 ID를 발급해 공격을 방어할 수 있다.


-  세션 타임아웃 설정 <br>
일정 시간 활동이 없는 세션은 자동으로 만료시켜야 한다. `sessionManagement().maximumSessions()`를 통해 사용자가 로그아웃을 하지 않더라도 일정 시간 후 자동 로그아웃을 유도해 보안을 강화해야 한다.


- CSRF 보호 활성화 <br>
CSRF(Cross Site Request Forgery) 공격은 사용자가 의도하지 않은 요청을 악용해 악의적인 행위를 유도하는 기법이다. CSRF 공격을 방지하려면 Spring Security의 `.csrf(csrf -> csrf.enable())`와 같이 CSRF 보호를 활성화하여야 한다.