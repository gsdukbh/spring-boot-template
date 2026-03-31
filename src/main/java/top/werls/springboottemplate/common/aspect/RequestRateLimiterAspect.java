package top.werls.springboottemplate.common.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import top.werls.springboottemplate.common.annotation.RequestLimit;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import top.werls.springboottemplate.common.annotation.RequestRateLimit;

/**
 * 基于 Guava RateLimiter 的匀速限流切面
 *
 * @author JiaWei Lee
 * @since on 28 11月 2025
 * @version 1
 */
@Aspect
@Component
@Slf4j
public class RequestRateLimiterAspect {

  private final HttpServletRequest request;

  // 使用 Caffeine 缓存 RateLimiter 实例
  private final Cache<String, RateLimiter> limiters;

  public RequestRateLimiterAspect(HttpServletRequest request) {
    this.request = request;
    // 初始化缓存：设置写入后 1 小时过期
    this.limiters =
        Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).maximumSize(10_000).build();
  }

    @Around("@annotation(top.werls.springboottemplate.common.annotation.RequestLimit)")
  public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    RequestRateLimit requestLimit = method.getAnnotation(RequestRateLimit.class);

    // 1. 计算 QPS (Queries Per Second)
    // 注解定义: minute 分钟内允许 frequency 次
    // RateLimiter 需要: 每秒允许多少个令牌
    double permitsPerSecond = (double) requestLimit.frequency() / (requestLimit.minute() * 60);

    // 2. 构建唯一 Key
    String key = resolveKey();
    String cacheKey = key + ":" + method.getName();

    // 3. 获取或创建 RateLimiter (原子操作)
    RateLimiter rateLimiter = limiters.get(cacheKey, k -> RateLimiter.create(permitsPerSecond));

    // 4. 尝试获取令牌 (非阻塞)
    if (rateLimiter != null && rateLimiter.tryAcquire()) {
      return joinPoint.proceed();
    } else {
      log.warn(
          "IP [{}] 访问 [{}] 过于频繁，被拒绝 (QPS限制: {})",
          key,
          method.getName(),
          String.format("%.2f", permitsPerSecond));
      throw new RuntimeException("访问过于频繁，请稍后再尝试");
    }
  }

  /** 解析限流 Key (IP 或 SessionID) */
  private String resolveKey() {
    String ip = request.getRemoteAddr();
    String sessionId = request.getRequestedSessionId();

    if (StringUtils.isAllBlank(ip)) {
      if (StringUtils.isNotBlank(sessionId)) {
        return sessionId;
      } else {
        return "defaultKey";
      }
    }
    return ip;
  }
}
