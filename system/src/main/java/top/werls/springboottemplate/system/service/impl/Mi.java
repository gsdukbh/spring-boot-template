package top.werls.springboottemplate.system.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import top.werls.springboottemplate.Mmo;

/**
 * @author Jiawei Lee
 * @version TODO
 * @date created 2022/7/7
 * @since on
 */
@Component
public class Mi implements Mmo {
    @Override
    public String getName() {
        return "holle ";
    }
}
