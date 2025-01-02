package top.werls.springboottemplate.system.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import top.werls.springboottemplate.common.utils.JwtTokenUtils;
import top.werls.springboottemplate.system.param.LoginParam;
import top.werls.springboottemplate.system.service.SysUserService;
import top.werls.springboottemplate.system.vo.LoginVo;


@Service
@Slf4j
public class SysUserServiceImpl implements SysUserService {

  private final UserDetailsServiceImpl userDetailsService;

  private final PasswordEncoder passwordEncoder;
  private final JwtTokenUtils tokenUtils;


  public SysUserServiceImpl(UserDetailsServiceImpl userDetailsService,
      PasswordEncoder passwordEncoder, JwtTokenUtils tokenUtils) {
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
    this.tokenUtils = tokenUtils;
  }

  /**
   * 登录
   *
   * @param param
   * @return
   */
  @Override
  public LoginVo login(LoginParam param) {

    UserDetails userDetails = userDetailsService.loadUserByUsername(param.getUsername());

    if (!passwordEncoder.matches(param.getPassword(), userDetails.getPassword())) {
      throw new BadCredentialsException("密码错误");
    }
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    LoginVo loginVo = new LoginVo();
    loginVo.setToken(tokenUtils.generateToken(userDetails.getUsername()));
    return loginVo;
  }

}
