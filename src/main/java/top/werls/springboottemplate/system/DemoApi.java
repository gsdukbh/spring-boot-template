package top.werls.springboottemplate.system;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.werls.springboottemplate.common.annotation.RequestLimit;
import top.werls.springboottemplate.common.utils.MessageUtils;
import top.werls.springboottemplate.config.SwaggerConfig;

@SecurityRequirement(name = SwaggerConfig.TOKEN_HEADER)
@Tag(name = "DemoApi", description = "the DemoApi API")
@RestController
public class DemoApi {


  private final MessageUtils messageUtils;

  public DemoApi(MessageUtils messageUtils) {
    this.messageUtils = messageUtils;
  }

  @Operation(summary = "getDemo", description = "getDemo")
  @GetMapping(value = "/demo")
  public String getHello() {
    return "Hello World!";
  }

  @Operation(summary = "get", description = "success")
  @GetMapping(value = "/success")
  @RequestLimit(frequency = 2)
  public String getDemo() {
    return "Hello World!" + messageUtils.getMessage("success");
  }


  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(value = "/admin")
  public String  admin(){
    return  "is admin";
  }
}
