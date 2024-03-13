package project.trendpick_pro.global.crypto.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRole;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.global.crypto.jwt.JwtToken.JwtToken;
import project.trendpick_pro.global.crypto.jwt.JwtToken.JwtTokenService;
import project.trendpick_pro.global.crypto.jwt.exception.FilterException;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    private final JwtTokenService jwtTokenService;

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(jwtProperties.getSecret());
        secretKey = Keys.hmacShaKeyFor(decodedKey);
    }

    public boolean checkTokenFromCookie(String tokenType, HttpServletRequest request) {
        String header = request.getHeader("cookie");
        return header.contains(tokenType);
    }

    public String getTokenFromCookie(String tokenType, HttpServletRequest request) {
        String header;
        if (request.getHeader("cookie") == null) {
            throw new BaseException(ErrorCode.NOT_FOUND, "쿠키가 없습니다.");
        } else {
            header = request.getHeader("cookie");
        }

        Optional<String> tokenValue = Arrays.stream(header.split("; "))
                .map(cookie -> cookie.split("=", 2))
                .filter(token -> token.length == 2)
                .filter(token -> token[0].equals(tokenType))
                .map(token -> token[1])
                .findFirst();
        if (tokenValue.isPresent()) {
            return tokenValue.get();
        } else {
            throw new FilterException(ErrorCode.BAD_REQUEST, "토큰이 없습니다.");
        }
    }

    public JwtTokenResponse generatedToken(String email, String role) {
        String accessToken = generatedJwtTokens(email, role, jwtProperties.getAccessExpirationTime());
        String refreshToken = generatedJwtTokens(email, role, jwtProperties.getRefreshExpirationTime());

        JwtToken token = jwtTokenService.save(email, accessToken, refreshToken);
        return new JwtTokenResponse(token.getAccessToken(), token.getRefreshToken());
    }

    public String generatedJwtTokens(String email, String role, long expirationTime) {
        Date now = new Date();
        Date expriedDate = new Date(now.getTime() + expirationTime);
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", MemberRole.isType(role).getValue());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expriedDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean verifyToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return claims.getBody().getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            throw new FilterException(ErrorCode.INVALID_TOKEN, "토큰이 만료되었습니다.");
        } catch (Exception e) {
            if (e instanceof JwtException || e instanceof IllegalArgumentException) {
                throw new FilterException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
            } else {
                throw e;
            }
        }
    }

    public Cookie generateTokenCookie(String tokenType, String jwtToken) {
        Cookie cookie = new Cookie(tokenType, jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(false);
        if (tokenType.equals("refreshToken")) {
            cookie.setMaxAge((int) jwtProperties.getRefreshExpirationTime() / 1000);
        } else {
            cookie.setMaxAge((int) jwtProperties.getAccessExpirationTime() / 1000);
        }
        return cookie;
    }

    public Authentication getAuthentication(Member member) {
        UserDetails user = User.builder()
                .username(member.getEmail())
                .password("")
                .authorities(() -> String.valueOf(new SimpleGrantedAuthority(member.getRole().getValue())))
                .build();
        return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
    }

    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
