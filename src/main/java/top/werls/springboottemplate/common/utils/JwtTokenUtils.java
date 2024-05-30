package top.werls.springboottemplate.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.werls.springboottemplate.config.ConfigProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenUtils {

  private static final String CLAIM_KEY_USERNAME = "username";
  private static final String CLAIM_KEY_TIME = "time";
  @Resource
  private ConfigProperties configProperties;

  public String generateToken(Map<String, Object> claims) {
    return Jwts.builder()
        .claims(claims)
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(getExpirationDate())
        .signWith(configProperties.getJwt().getPrivateKey())
        .compact();
  }

  public String generateToken(Map<String, Object> claims, Date time) {
    return Jwts.builder()
        .claims(claims)
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(time)
        .signWith(configProperties.getJwt().getPrivateKey())
        .compact();
  }

  public String generateTokenWithoutExpiry(Map<String, Object> claims) {
    return Jwts.builder()
        .claims(claims)
        .issuedAt(new Date())
        .id(UUID.randomUUID().toString())
        .signWith(configProperties.getJwt().getPrivateKey())
        .compact();
  }
  public String generateToken(String username) {
    Map<String, Object> claims =
        Map.of(CLAIM_KEY_USERNAME, username, CLAIM_KEY_TIME, new Date(System.currentTimeMillis()));
    return generateToken(claims);
  }


  /**
   * Generates a JWT token for a given username with a specified expiration time.
   *
   * @param username The username for which the token is to be generated.
   * @param time     The expiration time for the token in milliseconds.
   * @return The generated JWT token as a String.
   */
  public String generateToken(String username, Date time) {
    Map<String, Object> claims =
        Map.of(CLAIM_KEY_USERNAME, username, CLAIM_KEY_TIME, new Date(System.currentTimeMillis()));
    return generateToken(claims, time);
  }

  public String generateTokenWithoutExpiry(String username) {
    Map<String, Object> claims =
        Map.of(CLAIM_KEY_USERNAME, username, CLAIM_KEY_TIME, new Date(System.currentTimeMillis()));
    return generateTokenWithoutExpiry(claims);
  }

  public boolean validateTokenWithoutExpiry(String token, String username) {
    String usernameFromToken = getUsernameFromToken(token);
    return usernameFromToken.equals(username);
  }


  private Date getExpirationDate() {
    return new Date(
        Instant.now()
            .plus(Duration.ofMinutes(configProperties.getJwt().getExpire()))
            .toEpochMilli());
  }

  public String getUsernameFromToken(String token) {
    return getClaimsFromToken(token).get(CLAIM_KEY_USERNAME, String.class);
  }

  /**
   * 获取token 创建时间
   *
   * @param token token
   * @return Date Milli
   */
  public Date getTimeFromToken(String token) {
    return getClaimsFromToken(token).get(CLAIM_KEY_TIME, Date.class);
  }

  public boolean validateToken(String token, String username) {
    String usernameFromToken = getUsernameFromToken(token);
    return usernameFromToken.equals(username) ;
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimsFromToken(token).getExpiration();
  }

  public Claims getClaimsFromToken(String token) {
    return Jwts.parser()
        .verifyWith(configProperties.getJwt().getPublicKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String refreshToken(String oldToken) {
    if (StringUtils.isBlank(oldToken)) {
      return null;
    }
    String token = oldToken.substring(configProperties.getJwt().getTokenPrefix().length());
    Claims claims = getClaimsFromToken(token);
    Date createdDate = claims.get(CLAIM_KEY_TIME, Date.class);
    Date refreshDate = new Date(Instant.now().minus(Duration.ofMinutes(30)).toEpochMilli());
    if (createdDate.after(refreshDate) && createdDate.before(new Date())) {
      return oldToken;
    }
    String username = getUsernameFromToken(token);
    return generateToken(username);
  }
}
