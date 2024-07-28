package com.snowhub.server.dummy.filter;

import com.snowhub.server.dummy.service.security.CustomUserDetailsService;
import com.snowhub.server.dummy.service.UserService;
import com.snowhub.server.dummy.module.Firebase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 수정사항: 추후 Configuration으로 만든 후, 생성자 주입을 하자.
@RequiredArgsConstructor
@Slf4j
public class VerifyTokenFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final UserService userService;
    
    @Override
    protected boolean shouldNotFilter (HttpServletRequest request) throws ServletException {
        // 일반적인 Case: false (필터링)
        // OPTIONS 같은 경우 : true (Not Filtering)
        log.info("shouldNotFilter operated!!!");

        if(request.getMethod().equals("OPTIONS")){
            log.info("Pre-flight request pass!");
            String excludePath = "/board/list";
            String path = request.getRequestURI();
            return path.startsWith(excludePath);
        }
        return false;

    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Start verify tokens");
        
        // 브라우저 쿠키에서 토큰 추출
        String getAccessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        String getRefreshToken = request.getHeader("RefreshToken");

        String accessToken = getBearer(getAccessToken);
        String refreshToken = getBearer(getRefreshToken);

        // 1. firebase토큰인지 확인을 한다.
        Firebase firebase = Firebase.getInstance();

        // 사용할때마다 계속해서 Service를 Param으로 보내는 방법은 좀 아닌듯하다.
        // 그래서, firebase를 일반 클래스가 아닌 빈으로 등록하는 방법을 고려해보자.
        firebase.setCustomUserDetailsService(customUserDetailsService,userService);
        firebase.verifyToken(request,response,accessToken,refreshToken,filterChain);

    }

    protected String getBearer(String getToken){

        if (getToken != null && getToken.startsWith("Bearer ")) {
            return getToken.substring(7); // Extracting the token after "Bearer "
        }
        // else로 예외처리를 할 경우, accessToken만 보낸 경우(정상)에서 오류가 발생한다
        return null;

    }

}

/*
    1. 생성자 주입 방식
    public VerifyTokenFilter(CustomUserDetailsService customUserDetailsService,
                             UserService userService
                             ){
        log.info("VerifyTokenFilter is created!!!");
        this.customUserDetailsService=customUserDetailsService;
        this.userService=userService;
    }
*/


// 2. Google토큰 검증.
        /*
        if(isTokenVerified==false) {
            Google google = Google.getInstance();
            google.setEnviromentConfig(enviromentConfig);
            google.setCustomUserDetailsService(customUserDetailsService);
            google.setUserService(userService);
            google.verifyToken(request, response, bearerToken, filterChain);
        }

         */


