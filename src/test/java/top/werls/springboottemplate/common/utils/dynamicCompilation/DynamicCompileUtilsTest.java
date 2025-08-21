package top.werls.springboottemplate.common.utils.dynamicCompilation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;

/**
 * DynamicCompileUtils 单元测试
 * 测试动态编译工具的各种功能和安全特性
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
@SpringBootTest
class DynamicCompileUtilsTest {

  private DynamicCompileUtils dynamicCompileUtils;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    dynamicCompileUtils = new DynamicCompileUtils();
    // 设置临时目录作为class文件存储路径
    dynamicCompileUtils.setClazzPath(tempDir.toString() + "/");
  }

  @Test
  void compile() throws Exception {
    String code = """
        package dynamicCompilation;
            public class HelloWorld {
                 public String  hello(String msg){
                   return "Hello World! "+msg;
                  }
            }
        """;
    var o = dynamicCompileUtils.compile(code,
        "dynamicCompilation.HelloWorld");
    var res = o.getClass().getMethod("hello", String.class).invoke(o, "compile");
    assert res.toString().equals("Hello World! compile");
    System.out.println(res);
  }

  @Test
  void testCompileWithMathOperations() throws Exception {
    // 测试包含数学运算的安全代码
    String code = """
        package dynamicCompilation;
        public class MathClass {
            public int calculate(int a, int b) {
                return a + b * 2;
            }
            
            public double sqrt(double x) {
                return Math.sqrt(x);
            }
        }
        """;

    Object instance = dynamicCompileUtils.compile(code, "dynamicCompilation.MathClass");
    assertNotNull(instance);

    // 测试calculate方法
    var calculateResult = instance.getClass()
        .getMethod("calculate", int.class, int.class)
        .invoke(instance, 5, 3);
    assertEquals(11, calculateResult);

    // 测试sqrt方法
    var sqrtResult = instance.getClass()
        .getMethod("sqrt", double.class)
        .invoke(instance, 16.0);
    assertEquals(4.0, sqrtResult);
  }

  @Test
  void testCompileWithStringOperations() throws Exception {
    // 测试字符串操作
    String code = """
        package dynamicCompilation;
        public class StringClass {
            public String processString(String input) {
                if (input == null) return "null input";
                return input.toUpperCase().trim();
            }
            
            public int getLength(String input) {
                return input != null ? input.length() : 0;
            }
        }
        """;

    Object instance = dynamicCompileUtils.compile(code, "dynamicCompilation.StringClass");
    assertNotNull(instance);

    var result1 = instance.getClass()
        .getMethod("processString", String.class)
        .invoke(instance, "  hello world  ");
    assertEquals("HELLO WORLD", result1);

    var result2 = instance.getClass()
        .getMethod("getLength", String.class)
        .invoke(instance, "test");
    assertEquals(4, result2);
  }

  @Test
  void testCompileFailureWithDangerousReflection() throws Exception {
    // 测试包含反射的危险代码
    String dangerousCode = """
        package dynamicCompilation;
        import java.lang.reflect.Method;
        public class DangerousReflectionClass {
            public void exploit() throws Exception {
                Class<?> clazz = Class.forName("java.lang.Runtime");
                Method method = clazz.getMethod("getRuntime");
            }
        }
        """;

    Object result = dynamicCompileUtils.compile(dangerousCode, "dynamicCompilation.DangerousReflectionClass");
    assertNull(result, "包含反射的危险代码应该编译失败");
  }

  @Test
  void testCompileFailureWithDangerousIO() throws Exception {
    // 测试包含IO操作的危险代码
    String dangerousCode = """
        package dynamicCompilation;
        import java.io.FileWriter;
        public class DangerousIOClass {
            public void writeFile() throws Exception {
                FileWriter writer = new FileWriter("/tmp/malicious.txt");
                writer.write("malicious content");
                writer.close();
            }
        }
        """;

    Object result = dynamicCompileUtils.compile(dangerousCode, "dynamicCompilation.DangerousIOClass");
    assertNull(result, "包含IO操作的危险代码应该编译失败");
  }

  @Test
  void testCompileFailureWithDangerousRuntime() throws Exception {
    // 测试包含Runtime操作的危险代码
    String dangerousCode = """
        package dynamicCompilation;
        public class DangerousRuntimeClass {
            public void executeCommand() throws Exception {
                Runtime.getRuntime().exec("rm -rf /");
            }
        }
        """;

    Object result = dynamicCompileUtils.compile(dangerousCode, "dynamicCompilation.DangerousRuntimeClass");
    assertNull(result, "包含Runtime操作的危险代码应该编译失败");
  }

  @Test
  void testCompileFailureWithDangerousSystem() throws Exception {
    // 测试包含System操作的危险代码
    String dangerousCode = """
        package dynamicCompilation;
        public class DangerousSystemClass {
            public void accessSystem() {
                System.exit(1);
                System.setProperty("user.home", "/tmp");
            }
        }
        """;

    Object result = dynamicCompileUtils.compile(dangerousCode, "dynamicCompilation.DangerousSystemClass");
    assertNull(result, "包含System操作的危险代码应该编译失败");
  }

  @Test
  void testCompileFailureWithDangerousNetworking() throws Exception {
    // 测试包含网络操作的危险代码
    String dangerousCode = """
        package dynamicCompilation;
        import java.net.Socket;
        public class DangerousNetworkClass {
            public void connectToServer() throws Exception {
                Socket socket = new Socket("malicious.com", 8080);
                socket.close();
            }
        }
        """;

    Object result = dynamicCompileUtils.compile(dangerousCode, "dynamicCompilation.DangerousNetworkClass");
    assertNull(result, "包含网络操作的危险代码应该编译失败");
  }

  @Test
  void testCompileWithInvalidSyntax() throws Exception {
    // 测试语法错误的代码
    String invalidCode = """
        package dynamicCompilation;
        public class InvalidSyntaxClass {
            public void method() {
                // 缺少分号和括号
                int x = 5
                if (x > 0 {
                    return "positive"
                }
            }
        }
        """;

    Object result = dynamicCompileUtils.compile(invalidCode, "dynamicCompilation.InvalidSyntaxClass");
    assertNull(result, "语法错误的代码应该编译失败");
  }

  @Test
  void testCompileWithEmptyCode() throws Exception {
    // 测试空代码
    String emptyCode = "";

    Object result = dynamicCompileUtils.compile(emptyCode, "dynamicCompilation.EmptyClass");
    assertNull(result, "空代码应该编译失败并返回null");
  }

  @Test
  void testCompileWithNullCode() throws Exception {
    // 测试null代码
    String nullCode = null;

    Object result = dynamicCompileUtils.compile(nullCode, "dynamicCompilation.NullClass");
    assertNull(result, "null代码应该返回null");
  }

  @Test
  void testCompileWithComplexSafeCode() throws Exception {
    // 测试复杂但安全的代码
    String complexCode = """
        package dynamicCompilation;
        import java.util.List;
        import java.util.ArrayList;
        import java.util.Map;
        import java.util.HashMap;
        
        public class ComplexSafeClass {
            private List<String> items = new ArrayList<>();
            private Map<String, Integer> counts = new HashMap<>();
            
            public void addItem(String item) {
                items.add(item);
                counts.put(item, counts.getOrDefault(item, 0) + 1);
            }
            
            public int getCount(String item) {
                return counts.getOrDefault(item, 0);
            }
            
            public List<String> getItems() {
                return new ArrayList<>(items);
            }
            
            public String processItems() {
                StringBuilder result = new StringBuilder();
                for (String item : items) {
                    result.append(item).append(" ");
                }
                return result.toString().trim();
            }
        }
        """;

    Object instance = dynamicCompileUtils.compile(complexCode, "dynamicCompilation.ComplexSafeClass");
    assertNotNull(instance, "复杂但安全的代码应该编译成功");

    // 测试功能
    instance.getClass().getMethod("addItem", String.class).invoke(instance, "test");
    instance.getClass().getMethod("addItem", String.class).invoke(instance, "test");
    instance.getClass().getMethod("addItem", String.class).invoke(instance, "hello");

    var count = instance.getClass().getMethod("getCount", String.class).invoke(instance, "test");
    assertEquals(2, count);

    var processed = instance.getClass().getMethod("processItems").invoke(instance);
    assertEquals("test test hello", processed);
  }

  @Test
  void testSetClazzPath() {
    // 测试设置class路径
    String newPath = "/tmp/test/";
    DynamicCompileUtils result = dynamicCompileUtils.setClazzPath(newPath);

    assertSame(dynamicCompileUtils, result, "setClazzPath应该返回同一个实例以支持链式调用");
  }

  @Test
  void testCompileWithDifferentPackages() throws Exception {
    // 测试不同包名的编译
    String code1 = """
        package com.test.package1;
        public class TestClass1 {
            public String getName() { return "TestClass1"; }
        }
        """;

    String code2 = """
        package com.test.package2;
        public class TestClass2 {
            public String getName() { return "TestClass2"; }
        }
        """;

    // 由于安全检查会阻止非dynamicCompilation包的编译，这些应该返回null
    Object result1 = dynamicCompileUtils.compile(code1, "com.test.package1.TestClass1");
    Object result2 = dynamicCompileUtils.compile(code2, "com.test.package2.TestClass2");

    assertNull(result1, "非dynamicCompilation包的代码应该被安全检查阻止");
    assertNull(result2, "非dynamicCompilation包的代码应该被安全检查阻止");
  }
}