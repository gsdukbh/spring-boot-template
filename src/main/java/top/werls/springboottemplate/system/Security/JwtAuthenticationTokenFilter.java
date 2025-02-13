package top.werls.springboottemplate.system.Security;


import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.werls.springboottemplate.common.utils.JwtTokenUtils;


import java.io.IOException;


/**
 * @author leee
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {


  private final JwtTokenUtils tokenUtils;

  @Value("${env.jwt.tokenPrefix}")
  private String tokenPrefix;

  private final UserDetailsService userDetailsService;

  public JwtAuthenticationTokenFilter(JwtTokenUtils tokenUtils, UserDetailsService userDetailsService) {
    this.tokenUtils = tokenUtils;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Same contract as for {@code doFilter}, but guaranteed to be just invoked once per request
   * within a single request thread. See {@link #shouldNotFilterAsyncDispatch()} for details.
   * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
   * default ServletRequest and ServletResponse ones.
   *
   * @param request
   * @param response
   * @param filterChain
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith(tokenPrefix)) {
      String authToken = authHeader.substring(tokenPrefix.length()).trim();
      String username = tokenUtils.getUsernameFromToken(authToken);
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (tokenUtils.validateToken(authToken, userDetails.getUsername())) {
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userDetails, null,
                  userDetails.getAuthorities());
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
          log.info("Authenticated user: {}", username);
        }
      }
      // todo API 存储在数据库的token ，可以在这里进行校验

    }
    filterChain.doFilter(request, response);
  }
}
