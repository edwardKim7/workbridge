package com.workbridge.server.security;

import com.workbridge.server.dto.auth.JwtClaims;
import com.workbridge.server.exception.BusinessException;
import com.workbridge.server.service.auth.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwt;
    private final PrincipalLoader loader;
    private final SecurityErrorResponseWriter writer;

    public BearerTokenAuthenticationFilter(JwtTokenService jwt, PrincipalLoader loader, SecurityErrorResponseWriter writer) {
        this.jwt = jwt;
        this.loader = loader;
        this.writer = writer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h == null || !h.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }
        try {
            JwtClaims claims = jwt.parseAccessToken(h.substring(7).trim());
            WorkBridgePrincipal p = loader.load(claims.employeeId());
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(p, "", p.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(req, res);
        } catch (BusinessException e) {
            SecurityContextHolder.clearContext();
            writer.write(req, res, e.getErrorCode(), e.getMessage());
        }
    }
}
