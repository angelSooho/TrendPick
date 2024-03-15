package project.trendpick_pro.global.crypto.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.global.crypto.jwt.JwtToken.JwtTokenService;
import project.trendpick_pro.global.crypto.jwt.exception.FilterException;
import project.trendpick_pro.global.crypto.jwt.exception.FilterExceptionResponse;
import project.trendpick_pro.global.exception.ErrorCode;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final JwtTokenService jwtTokenService;
    private final MemberService memberService;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            if (request.getRequestURI().contains("api")) {
                filterChain.doFilter(request, response);
                return;
            }
            if (request.getRequestURI().contains("login")) {
                boolean isToken = jwtTokenUtil.checkTokenFromCookie("accessToken", request);
                if (isToken) {
                    throw new FilterException(ErrorCode.UNAUTHORIZED, "이미 로그인 되어있습니다.");
                } else {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            if (request.getRequestURI().contains("reissue")) {
                String refreshToken = jwtTokenUtil.getTokenFromCookie("refreshToken", request);
                if (!StringUtils.hasText(refreshToken)) {
                    throw new FilterException(ErrorCode.UNAUTHORIZED, "헤더에 토큰이 없습니다.");
                }
                if (!jwtTokenUtil.verifyToken(refreshToken) && jwtTokenService.verifyToken("refreshToken", refreshToken)) {
                    throw new FilterException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
                } else {
                    String email = jwtTokenUtil.getEmail(refreshToken);
                    Member member = memberService.findByEmail(email);
                    Authentication authentication = jwtTokenUtil.getAuthentication(member);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                String accessToken = jwtTokenUtil.getTokenFromCookie("accessToken", request);
                if (!StringUtils.hasText(accessToken)) {
                    throw new FilterException(ErrorCode.UNAUTHORIZED, "헤더에 토큰이 없습니다.");
                }
                if (!jwtTokenUtil.verifyToken(accessToken) && jwtTokenService.verifyToken("accessToken", accessToken)) {
                    throw new FilterException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
                } else {
                    String email = jwtTokenUtil.getEmail(accessToken);
                    Member member = memberService.findByEmail(email);
                    Authentication authentication = jwtTokenUtil.getAuthentication(member);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        } catch (FilterException e) {
            handleException(request, response, e, null);
        } catch (Exception e) {
            handleException(request, response, null, e.getMessage());
        }
    }

    private void handleException(HttpServletRequest request, HttpServletResponse response, FilterException e, String message) throws IOException {
        if (e instanceof FilterException) {
            FilterExceptionResponse errorResponse = FilterExceptionResponse.of(e, request.getRequestURI());
            response.setStatus(e.getErrorCode().getStatus().value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write(objectMapper.writeValueAsBytes(errorResponse));
        } else {
            FilterExceptionResponse errorResponse = FilterExceptionResponse.of(
                    new FilterException(ErrorCode.UNAUTHORIZED, message != null ? message : "예외가 발생했습니다."),
                    request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write(objectMapper.writeValueAsBytes(errorResponse));
        }
    }
}
