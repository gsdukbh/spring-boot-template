package top.werls.springboottemplate.common.utils.dynamicCompilation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Li JiaWei
 * @version 1
 * @date 2023/4/9
 * @since on
 */
//@SpringBootTest
class DynamicCompileUtilsTest {

  @Test
  void compile() throws Exception {
    String code = """
        package top.werls.springboottemplate.common.utils.dynamicCompilation;
            public class HelloWorld {
                 public String  hello(String msg){
                   System.out.println("Hello World!"+msg);
                   return "Hello World!"+msg;
                  }
            }
        """;

    DynamicCompileUtils dynamicCompileUtils = new DynamicCompileUtils();
    var o = dynamicCompileUtils.compile(code,"top.werls.springboottemplate.common.utils.dynamicCompilation.HelloWorld");
    var res =o.getClass().getMethod("hello", String.class).invoke(o, "JiaWei");
    System.out.println(res);
  }
}