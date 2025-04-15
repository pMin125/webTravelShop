package com.toyProject.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {
    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        System.out.print(principal.getClass());
        // principal(여기서는 UserEntity)가 그대로 AuthenticationToken에 사용되도록 함.
        return new UsernamePasswordAuthenticationToken(principal, authentication.getCredentials(), user.getAuthorities());
    }
}
