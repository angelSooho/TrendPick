package project.trendpick_pro.global.crypto.jwt.JwtToken;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@RedisHash(value = "jwtToken", timeToLive = 60 * 60 * 24 * 15)
@AllArgsConstructor
public class JwtToken implements Serializable {

    @Id
    private String id;
    private String accessToken;
    private String refreshToken;
}
