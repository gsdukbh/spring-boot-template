package top.werls.springboottemplate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.werls.springboottemplate.system.entity.SysUser;

@SpringBootTest
class SpringBootTemplateApplicationTests {

    @Test
    void contextLoads() {
        SysUser user =new SysUser();
        user.setUsername( "test");
    }

}
