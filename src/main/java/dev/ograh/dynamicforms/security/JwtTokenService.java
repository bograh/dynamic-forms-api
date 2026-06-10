package dev.ograh.dynamicforms.security;

import dev.ograh.dynamicforms.auth.dto.TokensDTO;
import dev.ograh.dynamicforms.shared.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    public TokensDTO generateTokens(String email, String role) {
        String accessToken = generateAccessToken(email, role);
        String refreshToken = generateRefreshToken(email, role);
        return new TokensDTO(accessToken, refreshToken);
    }

    public Claims validateTokenAndExtractClaims(String token, boolean isRefreshToken) {
        String secret = isRefreshToken
                ? jwtProperties.getRefreshTokenSecret()
                : jwtProperties.getAccessTokenSecret();

        SecretKey signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("Token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException("Unsupported JWT token", e);
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException("Malformed JWT token", e);
        } catch (SecurityException e) {
            throw new JwtAuthenticationException("Invalid JWT signature", e);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("JWT claims string is empty", e);
        }
    }

    public String extractSubject(String token, boolean isRefreshToken) {
        return validateTokenAndExtractClaims(token, isRefreshToken).getSubject();
    }

    public String extractRole(String token) {
        return validateTokenAndExtractClaims(token, false).get("role", String.class);
    }

    public Date extractExpiration(String token, boolean isRefreshToken) {
        return validateTokenAndExtractClaims(token, isRefreshToken).getExpiration();
    }

    public boolean isTokenExpired(String token, boolean isRefreshToken) {
        return extractExpiration(token, isRefreshToken).before(new Date());
    }

    private String generateAccessToken(String email, String... claims) {
        return buildToken(
                jwtProperties.getAccessTokenSecret(),
                jwtProperties.getAccessTokenExpirationMs(),
                email,
                claims
        );
    }

    private String generateRefreshToken(String email, String... claims) {
        return buildToken(
                jwtProperties.getRefreshTokenSecret(),
                jwtProperties.getRefreshTokenExpirationMs(),
                email,
                claims
        );
    }

    private String buildToken(String secret, long expiration, String subject, String... claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey);

        if (claims != null && claims.length > 0) {
            builder.claim("role", claims[0]);
        }

        return builder.compact();
    }
}