package com.contextflow.auth.config;

import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.auth.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        jwtTokenService.parseUser(authorizationHeader)
                .ifPresent(this::authenticate);

        filterChain.doFilter(request, response);
    }

    private void authenticate(CurrentUserResponse user) {
        var authority = new SimpleGrantedAuthority("ROLE_" + user.role().name());
        var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

