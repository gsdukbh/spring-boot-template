package top.werls.springboottemplate.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Locale;

@Component
@Slf4j
public class MessageUtils {
  @Resource private MessageSource messageSource;

  public String getMessage(String msgKey) {
    try {
      return messageSource.getMessage(msgKey, null, LocaleContextHolder.getLocale());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return msgKey;
    }
  }

  String getMessage(String code, @Nullable Object[] args) {
    return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
  }
}
