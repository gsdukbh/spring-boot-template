package top.werls.springboottemplate.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  匀速限流
 * @author JiaWei Lee
 * @since on 28 11月 2025
 * @version 1
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestRateLimit {
  /**
   * 限制 访问次数，
   *
   */
  int frequency() default 100;
  /**
   * 在几分钟内
   *
   */
  int minute() default 1;
}
