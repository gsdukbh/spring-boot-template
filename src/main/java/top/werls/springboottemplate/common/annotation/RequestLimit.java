package top.werls.springboottemplate.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Li JiaWei
 * @version TODO
 * @date 2022/11/27
 * @since on
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLimit {

  /**
   * 限制 访问次数，
   *
   */
  int frequency() default 10;

  /**
   * 在几分钟内
   *
   */
  int minute() default 1;
}
