package com.example.employee_management.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        logger.info("[JWT-FILTER] {} {}", method, path);
        logger.info("[JWT-FILTER] Authorization header: {}", authHeader);

        String token = null;

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
            logger.info("[JWT-FILTER] token length: {}", token.length());
        }

        try {
            if (token != null && jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromJwt(token);
                String role = jwtTokenUtil.getRoleFromJwt(token);

                logger.info("[JWT-FILTER] token valid. user={}, role={}", username, role);

                // Spring Security expects roles to start with "ROLE_"
                String springRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority(springRole))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("[JWT-FILTER] SecurityContext set with {}", springRole);
            } else {
                logger.info("[JWT-FILTER] No valid token found.");
            }
        } catch (Exception ex) {
            logger.error("[JWT-FILTER] Error validating token: {}", ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }
}
