package top.werls.springboottemplate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.werls.springboottemplate.common.utils.cache.Cache;

import javax.validation.constraints.NotBlank;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author Jiawei Lee
 * @version TODO
 * @date created 2022/7/20
 * @since on
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "env")
public class ConfigProperties {
  /**
   * app mingc
   */
  private String appName = "template";
  private String version = "0.0.1";
  private boolean isEnableSwagger = false;
  private StorageType storageType = StorageType.LOCAL;
  @Data
  public static class jwtProperties{
    private Integer expire=30;
    private  String tokenHeader= "Authorization";
    private  String tokenPrefix= "Bearer";
    @NotBlank
    private RSAPrivateKey privateKey;
    @NotBlank
    private RSAPublicKey publicKey;
  }
}
