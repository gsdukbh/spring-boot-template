package top.werls.springboottemplate.common.utils.dynamicCompilation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * SecurityChecker 单元测试
 * 测试代码安全检查器的各种安全检查功能
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
@SpringBootTest
class SecurityCheckerTest {

    @Test
    void testSafeCode() {
        // 测试安全代码
        String safeCode = """
            package test;
            import java.util.List;
            import java.util.ArrayList;
            
            public class SafeClass {
                public String process(String input) {
                    List<String> list = new ArrayList<>();
                    list.add(input);
                    return "Processed: " + input;
                }
            }
            """;

        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(safeCode);
        assertTrue(result.isSafe(), "安全代码应该通过检查");
        assertTrue(result.getViolations().isEmpty(), "安全代码不应该有违规项");
    }

    @Test
    void testDangerousReflection() {
        // 测试危险反射代码
        String dangerousCode = """
            package test;
            import java.lang.reflect.Method;
            
            public class DangerousClass {
                public void exploit() {
                    Class.forName("java.lang.Runtime");
                }
            }
            """;

        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(dangerousCode);
        assertFalse(result.isSafe(), "包含反射的代码应该被检测为不安全");
        assertFalse(result.getViolations().isEmpty(), "应该有违规项");
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("reflect")),
                   "应该检测到反射相关违规");
    }

    @Test
    void testDangerousIO() {
        // 测试危险IO操作
        String dangerousCode = """
            package test;
            import java.io.File;
            import java.io.FileWriter;
            
            public class DangerousClass {
                public void writeFile() throws Exception {
                    FileWriter writer = new FileWriter("test.txt");
                    writer.write("malicious content");
                }
            }
            """;

        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(dangerousCode);
        assertFalse(result.isSafe(), "包含IO操作的代码应该被检测为不安全");
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("java.io")),
                   "应该检测到IO相关违规");
    }

    @Test
    void testDangerousRuntime() {
        // 测试危险Runtime操作
        String dangerousCode = """
            package test;
            
            public class DangerousClass {
                public void executeCommand() throws Exception {
                    Runtime.getRuntime().exec("rm -rf /");
                }
            }
            """;

        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(dangerousCode);
        assertFalse(result.isSafe(), "包含Runtime操作的代码应该被检测为不安全");
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("Runtime")),
                   "应该检测到Runtime相关违规");
    }

    @Test
    void testDangerousNetworking() {
        // 测试危险网络操作
        String dangerousCode = """
            package test;
            import java.net.Socket;
            
            public class DangerousClass {
                public void connect() throws Exception {
                    Socket socket = new Socket("evil.com", 666);
                }
            }
            """;

        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(dangerousCode);
        assertFalse(result.isSafe(), "包含网络操作的代码应该被检测为不安全");
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("java.net")),
                   "应该检测到网络相关违规");
    }

    @Test
    void testDangerousKeywords() {
        // 测试危险关键字
        String dangerousCode = """
            package test;
            
            public class DangerousClass {
                public native void nativeMethod();
                
                public synchronized void syncMethod() {
                    // dangerous sync
                }
                
                private volatile boolean flag;
            }
            """;

        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(dangerousCode);
        assertFalse(result.isSafe(), "包含危险关键字的代码应该被检测为不安全");
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("native")),
                   "应该检测到native关键字");
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("synchronized")),
                   "应该检测到synchronized关键字");
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("volatile")),
                   "应该检测到volatile关键字");
    }

    @Test
    void testStringConcatenation() {
        // 测试字符串拼接绕过
        String suspiciousCode = """
            package test;
            
            public class SuspiciousClass {
                public void bypass() {
                    String className = "java.lang." + "Runtime";
                    StringBuilder sb = new StringBuilder();
                    sb.append("dangerous");
                }
            }
            """;

        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(suspiciousCode);
        assertFalse(result.isSafe(), "包含可疑字符串拼接的代码应该被检测为不安全");
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("字符串拼接")),
                   "应该检测到字符串拼接违规");
    }

    @Test
    void testCodeLengthLimit() {
        // 测试代码长度限制
        StringBuilder longCode = new StringBuilder();
        longCode.append("package test;\n");
        longCode.append("public class LongClass {\n");
        for (int i = 0; i < 1000; i++) {
            longCode.append("    public void method").append(i).append("() { System.out.println(\"test\"); }\n");
        }
        longCode.append("}\n");

        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(longCode.toString());
        assertFalse(result.isSafe(), "过长的代码应该被检测为不安全");
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("代码长度超过限制")),
                   "应该检测到代码长度违规");
    }

    @Test
    void testMultipleDangerousPatterns() {
        // 测试多种危险模式
        String dangerousCode = """
            package test;
            import java.lang.reflect.Method;
            import java.io.File;
            
            public class VeryDangerousClass {
                public native void nativeMethod();
                
                public void multipleThreats() throws Exception {
                    Class.forName("java.lang.Runtime");
                    new File("/etc/passwd");
                    String cmd = "rm" + " -rf";
                }
            }
            """;

        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(dangerousCode);
        assertFalse(result.isSafe(), "包含多种危险模式的代码应该被检测为不安全");
        assertTrue(result.getViolations().size() > 1, "应该检测到多个违规项");
    }

    @Test
    void testSecurityCheckResultToString() {
        // 测试SecurityCheckResult的toString方法
        String dangerousCode = "import java.lang.reflect.Method;";
        SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(dangerousCode);

        String resultStr = result.toString();
        assertNotNull(resultStr);
        assertTrue(resultStr.contains("SecurityCheckResult"));
        assertTrue(resultStr.contains("safe=false"));
        assertTrue(resultStr.contains("violations="));
    }
}
