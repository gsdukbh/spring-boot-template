package top.werls.springboottemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author leejiawei
 */
@SpringBootApplication
@Slf4j
public class SpringBootTemplateApplication {

  public static void main(String[] args) {
    var context = SpringApplication.run(SpringBootTemplateApplication.class, args);
    var env = context.getEnvironment();
    var port =env.getProperty("server.port");
    log.warn("""
            
            ==============================================
            = App run success !!
            = Swagger doc : http://localhost:{}/swagger-ui.html
            ==============================================
            """,port);
  }
}
