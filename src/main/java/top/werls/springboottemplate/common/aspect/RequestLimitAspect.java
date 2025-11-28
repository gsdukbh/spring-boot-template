package top.werls.springboottemplate.common.aspect;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.werls.springboottemplate.common.annotation.RequestLimit;
import top.werls.springboottemplate.common.utils.cache.Cache;
import top.werls.springboottemplate.common.utils.cache.impl.SimpleCache;

import java.lang.reflect.Method;

/**
 * 缺陷
 * @author Li JiaWei
 * @version 1
 * @date 2022/11/27
 * @since on
 */
@Aspect
@Component
@Slf4j
public class RequestLimitAspect {

  private final HttpServletRequest request;

  public RequestLimitAspect(HttpServletRequest request) {
    this.request = request;
  }

  private static final Cache<Object, AtomicInteger> cache = new SimpleCache<>(1000, 60 * 1000);

  //  @Pointcut(value = "@annotation(top.werls.springboottemplate.common.annotation.RequestLimit)")
  public void point() {}

  //  @Around("point()")
  public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    RequestLimit requestLimit = method.getAnnotation(RequestLimit.class);

    int frequency = requestLimit.frequency();
    int minute = requestLimit.minute();

    String key = request.getRemoteAddr();
    var sessionId = request.getRequestedSessionId();

    if (StringUtils.isAllBlank(key)) {
      if (StringUtils.isNotBlank(sessionId)) {
        key = sessionId;
      } else {
        // 默认给一个固定值
        key = "defaultKey";
      }
    }

    AtomicInteger counter = cache.get(key);
    if (counter == null) {
      counter = new AtomicInteger(0);
      cache.put(key, counter, (long) minute * 60 * 1000);
    }

    // 2. 原子自增并获取新值 (这是线程安全的核心)
    int limit = counter.incrementAndGet();

    if (limit <= frequency) {
      return joinPoint.proceed();
    } else {
      throw new RuntimeException("访问过于频繁，请稍后再尝试");
    }
  }
}
