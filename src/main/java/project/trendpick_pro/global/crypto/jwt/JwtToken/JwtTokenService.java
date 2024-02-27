package project.trendpick_pro.global.crypto.jwt.JwtToken;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.global.crypto.jwt.exception.FilterException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtTokenRepository jwtTokenRepository;

    public JwtToken save(String email, String accessToken, String refreshToken) {
        return jwtTokenRepository.save(new JwtToken(email, accessToken, refreshToken));
    }

    public boolean verifyToken(String tokenType, String token) {
        switch (tokenType) {
            case "accessToken" -> {
                Optional<JwtToken> findToken = jwtTokenRepository.findByAccessToken(token);
                return findToken.isPresent() && findToken.get().getAccessToken().equals(token);
            }
            case "refreshToken" -> {
                Optional<JwtToken> findToken = jwtTokenRepository.findByRefreshToken(token);
                return findToken.isPresent() && findToken.get().getRefreshToken().equals(token);
            }
            default -> throw new FilterException(ErrorCode.BAD_REQUEST, "토큰 타입이 잘못되었습니다.");
        }
    }

    public void deleteById(String id) {
        jwtTokenRepository.deleteById(id);
    }
}
