package top.werls.springboottemplate.system.controller;


import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.werls.springboottemplate.common.ResultData;
import top.werls.springboottemplate.common.annotation.RequestLimit;
import top.werls.springboottemplate.system.param.LoginParam;
import top.werls.springboottemplate.system.service.SysUserService;
import top.werls.springboottemplate.system.vo.LoginVo;


@Slf4j
@RestController
public class LoginController {


  private final SysUserService userService;




  public LoginController(SysUserService userService) {
    this.userService = userService;
  }

  @PostMapping("/login")
  @RequestLimit(frequency = 2)
  public ResultData<LoginVo> login(@RequestBody LoginParam param,
      HttpServletRequest servletRequest) {

    return ResultData.success(userService.login(param));
  }
}
