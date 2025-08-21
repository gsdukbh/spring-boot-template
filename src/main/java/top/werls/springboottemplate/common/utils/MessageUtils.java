package top.werls.springboottemplate.common.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化消息工具类
 * 提供获取国际化消息的便捷方法，支持根据当前用户的语言环境自动选择对应的消息内容
 *
 * @author Spring Boot Template
 * @since 1.0
 */
@Component
@Slf4j
public class MessageUtils {

  /** 注入Spring的���息源，用于获取国际化消息 */
  @Resource private MessageSource messageSource;

  /**
   * 根据消息键获取国际化消息（无参数版本）
   * 使用当前线程的语言环境来确定返回哪种语言的消息
   *
   * @param msgKey 消息键，对应国际化配置文件中的key
   * @return 对应当前语言环境的消息内容，如果获取失败则返回原始的msgKey
   */
  public String getMessage(String msgKey) {
    try {
      // 使用当前线程的语言环境获取国际化消息
      return messageSource.getMessage(msgKey, null, LocaleContextHolder.getLocale());
    } catch (Exception e) {
      // 记录异常信息并返回原始的消息键作为降级处理
      log.error(e.getMessage(), e);
      return msgKey;
    }
  }

  /**
   * 根据消息键和参数获取国际化消息（带参数版本）
   * 支持在消息模板中使用占位符，通过参数数组进行替换
   *
   * @param code 消息键，对应国际化配置文件中的key
   * @param args 消息参数数组，用于替换消息模板中的占位符，可以为null
   * @return 对应当前语言环境的格式化后的消息内容
   */
  String getMessage(String code, @Nullable Object[] args) {
    // 使用当前线程的语言环境和参数数组获取格式化的国际化消息
    return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
  }
}
