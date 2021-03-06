package com.spring.sio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.sio.constant.SecurityConstants;
import com.spring.sio.dto.UserDto;
import com.spring.sio.model.request.UserLoginRequestModel;
import com.spring.sio.service.IUserService;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    //Step 3-1
    private final AuthenticationManager authenticationManager;

    public AuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    //Step 3-2
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UserLoginRequestModel loginRequestModel = null;
        try {
            loginRequestModel = new ObjectMapper()
                    .readValue(request.getInputStream(), UserLoginRequestModel.class);
            /*
            *---------------------------------------------------------------------------
            * new ObjectMapper()
                    .readValue(request.getInputStream(), UserLoginRequestModel.class);
            *---------------------------------------------------------------------------
            * //read login json body like following data
            *  {
            *    "email" : "dummy@gmail.com",
            *    "password": "dummy"
            *  }
            *
            * */
        } catch (IOException e) {
            e.printStackTrace();
        }

        Authentication returnLogin = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestModel.getEmail(),
                        loginRequestModel.getPassword(),
                        new ArrayList<>()
                )
        );

        return returnLogin;
    }

    //Step 3-3
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        String username = ((User) authResult.getPrincipal()).getUsername();

        String token = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.TOKEN_SECRET)
                .compact();

        IUserService userService = (IUserService) SpringApplicationContext.getBean("userServiceImpl");
        UserDto userDto = userService.getUser(username);


//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
       /* response.getWriter().write(
                "{\"" + SecurityConstrains.HEADERS_STRING + "\":\" \n" +
                        SecurityConstrains.USER_ID + ":" + userDto.getUserId() + "\n" +
                        SecurityConstrains.TOKEN_PREFIX + ":" + token + "\"}"
        );*/
        response.addHeader(SecurityConstants.HEADING_StRING, SecurityConstants.TOKEN_PREFIX + token);
        response.addHeader("UserId" , userDto.getUserId());
}
}
