package top.werls.springboottemplate.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
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

  /**
   * 根据claims生成JWT token，使用默认过期时间。
   *
   * @param claims 要包含在token中的声明信息
   * @return 生成的JWT token字符串
   */
  public String generateToken(Map<String, Object> claims) {
    return Jwts.builder()
        .claims(claims)
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(getExpirationDate())
        .signWith(configProperties.getJwt().getPrivateKey())
        .compact();
  }

  /**
   * 根据claims和指定过期时间生成JWT token。
   *
   * @param claims 要包含在token中的声明信息
   * @param time   token的过期时间
   * @return 生成的JWT token字符串
   */
  public String generateToken(Map<String, Object> claims, Date time) {
    return Jwts.builder()
        .claims(claims)
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(time)
        .signWith(configProperties.getJwt().getPrivateKey())
        .compact();
  }

  /**
   * 根据claims生成永不过期的JWT token。
   *
   * @param claims 要包含在token中的声明信息
   * @return 生成的永不过期的JWT token字符串
   */
  public String generateTokenWithoutExpiry(Map<String, Object> claims) {
    return Jwts.builder()
        .claims(claims)
        .issuedAt(new Date())
        .id(UUID.randomUUID().toString())
        .signWith(configProperties.getJwt().getPrivateKey())
        .compact();
  }

  /**
   * 为指定用户名生成JWT token，使用默认过期时间。
   *
   * @param username 用户名
   * @return 生成的JWT token字符串
   */
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

  /**
   * 为指定用户名生成永不过期的JWT token。
   *
   * @param username 用户名
   * @return 生成的永不过期的JWT token字符串
   */
  public String generateTokenWithoutExpiry(String username) {
    Map<String, Object> claims =
        Map.of(CLAIM_KEY_USERNAME, username, CLAIM_KEY_TIME, new Date(System.currentTimeMillis()));
    return generateTokenWithoutExpiry(claims);
  }

  /**
   * 验证永不过期的token是否有效（通过比较用户名）。
   *
   * @param token    要验证的JWT token
   * @param username 预期的用户名
   * @return 如果token中的用户名与提供的用户名匹配则返回true，否则返回false
   */
  public boolean validateTokenWithoutExpiry(String token, String username) {
    String usernameFromToken = getUsernameFromToken(token);
    return usernameFromToken.equals(username);
  }

  /**
   * 计算token的默认过期时间。
   *
   * @return 基于配置的过期时间的Date对象
   */
  private Date getExpirationDate() {
    return new Date(
        Instant.now()
            .plus(Duration.ofMinutes(configProperties.getJwt().getExpire()))
            .toEpochMilli());
  }

  /**
   * 从JWT token中提取用户名。
   *
   * @param token JWT token字符串
   * @return token中包含的用户名
   */
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

  /**
   * 验证JWT token是否有效（通过比较用户名）。
   *
   * @param token    要验证的JWT token
   * @param username 预期的用户名
   * @return 如果token中的用户名与提供的用户名匹配则返回true，否则返回false
   */
  public boolean validateToken(String token, String username) {
    String usernameFromToken = getUsernameFromToken(token);
    return usernameFromToken.equals(username) ;
  }

  /**
   * 从JWT token中获取过期时间。
   *
   * @param token JWT token字符串
   * @return token的过期时间
   */
  public Date getExpirationDateFromToken(String token) {
    return getClaimsFromToken(token).getExpiration();
  }

  /**
   * 从JWT token中解析并获取所有claims。
   *
   * @param token JWT token字符串
   * @return 包含token所有声明信息的Claims对象
   */
  public Claims getClaimsFromToken(String token) {
    return Jwts.parser()
        .verifyWith(configProperties.getJwt().getPublicKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * 刷新JWT token。如果旧token在刷新窗口期内，则返回原token；否则生成新token。
   *
   * @param oldToken 待刷新的旧token（包含前缀）
   * @return 刷新后的token字符串，如果输入为空则返回null
   */
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

  /**
   * 将字节数组转换为十六进制字符串。
   *
   * @param hash 哈希计算后得到的字节数组
   * @return     十六进制表示的字符串
   */
  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  /**
   * 计算给定字符串的 SHA-256 哈希值。
   *
   * @param input 原始字符串 (例如 API Token)
   * @return      计算出的 SHA-256 哈希值，以十六进制字符串形式表示。
   */
  public static String hashString(String input) {
    try {
      // 1. 获取 SHA-256 算法的 MessageDigest 实例
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // 2. 将输入字符串按 UTF-8 编码转换为字节数组，并进行哈希计算
      // digest() 方法返回哈希值的字节数组
      byte[] encodedhash = digest.digest(
          input.getBytes(StandardCharsets.UTF_8));

      // 3. 将字节数组转换为十六进制字符串
      return bytesToHex(encodedhash);

    } catch (NoSuchAlgorithmException e) {
      // 在一个支持 SHA-256 的标准 Java 环境中，这几乎不可能发生
      throw new RuntimeException("SHA-256 algorithm not found", e);
    }
  }

  /**
   * 生成一个类似 GitHub 风格的安全 API Token。
   *
   * @param prefix Token 的���缀，例如 "ghp", "sk"。
   * @param byteLength 随机部分的字节长度。32 字节是一个很好的安全起点。
   * @return 生成的 API Token，格式为 "prefix_randomString"。
   */
  public static String generateApiToken(String prefix, int byteLength) {
    // 1. 生成指定长度的随机字节
    byte[] randomBytes = new byte[byteLength];
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(randomBytes);

    // 2. 使用 Base64 对字节进行编码
    Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();
    String randomString = base64Encoder.encodeToString(randomBytes);

    // 3. 拼接前缀并返回
    return prefix + "_" + randomString;
  }
}
