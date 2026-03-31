package top.werls.springboottemplate.common.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import top.werls.springboottemplate.common.annotation.RequestLimit;

/**
 * test 基于 Caffeine 实现的高性能固定窗口限流切面
 *
 * @author JiaWei Lee
 * @since on 28 11月 2025
 * @version 1
 */
@Aspect
@Component
@Slf4j
public class RequestLimitCaffAspect {

  private final HttpServletRequest request;

  // 使用 Caffeine 构建本地缓存
  // key: IP + Method, value: 当前计数
  private final Cache<String, Integer> limitCache;

  public RequestLimitCaffAspect(HttpServletRequest request) {
    this.request = request;
    // 初始化 Caffeine
    this.limitCache =
        Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(10_000).build();
  }

  @Around("@annotation(top.werls.springboottemplate.common.annotation.RequestLimit)")
  public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    RequestLimit requestLimit = method.getAnnotation(RequestLimit.class);

    int frequency = requestLimit.frequency();
    // 注意：如果注解的 minute 经常变化，上面的 expireAfterWrite(1, MIN) 可能不适用。
    // 生产环境通常建议：
    // 1. 统一限流时间窗口（如都是1分钟）。
    // 2. 或者使用 Redis (Redisson) 实现分布式动态限流。

    String ip = request.getRemoteAddr();
    String key = ip + ":" + method.getName(); // 细化 Key 的粒度

    // --- 核心并发安全逻辑 ---
    // compute 是原子的。它在 Map 的锁内执行，保证了并发安全。
    Integer currentCount =
        limitCache
            .asMap()
            .compute(
                key,
                (k, v) -> {
                  if (v == null) {
                    return 1; // 第一次访问
                  }
                  return v + 1; // 累加
                });

    if (currentCount != null && currentCount > frequency) {
      log.warn("IP [{}] 访问 [{}] 超过频率限制 ({}次/分)", ip, method.getName(), frequency);
      throw new RuntimeException("访问过于频繁，请稍后再尝试");
    }

    return joinPoint.proceed();
  }
}
