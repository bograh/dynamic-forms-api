package dev.ograh.dynamicforms.security;

import dev.ograh.dynamicforms.shared.exception.ErrorResponse;
import dev.ograh.dynamicforms.shared.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtTokenService.validateTokenAndExtractClaims(token, false);

            String subject = claims.getSubject();
            String role = claims.get("role", String.class);

            List<GrantedAuthority> authorities = role != null
                    ? List.of(new SimpleGrantedAuthority(role))
                    : List.of();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(subject, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtAuthenticationException e) {
            SecurityContextHolder.clearContext();
            ErrorResponse errorResponse = ErrorResponse.of(
                    e.getMessage(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    HttpStatus.UNAUTHORIZED.value(),
                    LocalDateTime.now().toString(),
                    request.getRequestURI()
            );
            response.sendError(HttpStatus.UNAUTHORIZED.value(), errorResponse.toString());
            return;
        }

        filterChain.doFilter(request, response);
    }
}