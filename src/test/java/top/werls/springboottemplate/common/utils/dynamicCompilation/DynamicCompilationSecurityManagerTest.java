package top.werls.springboottemplate.common.utils.dynamicCompilation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * DynamicCompilationSecurityManager 单元测试
 * 测试动态编译安全管理器的功能
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
@SpringBootTest
class DynamicCompilationSecurityManagerTest {

    private DynamicCompileUtils mockCompileUtils;

    @BeforeEach
    void setUp() {
        // 初始化安全管理器
        DynamicCompilationSecurityManager.initialize();

        // 创建模拟的编译工具
        mockCompileUtils = new DynamicCompileUtils() {
            @Override
            public Object internalCompile(String code, String className) throws Exception {
                // 模拟编译成功
                if (code.contains("safe")) {
                    return new Object() {
                        @Override
                        public String toString() {
                            return "编译成功的对象";
                        }
                    };
                } else if (code.contains("timeout")) {
                    Thread.sleep(35000); // 模拟超时
                    return null;
                } else if (code.contains("exception")) {
                    throw new RuntimeException("编译异常");
                } else {
                    return null;
                }
            }
        };
    }

    @Test
    void testInitialize() {
        // 测试初始化方法
        assertDoesNotThrow(() -> {
            DynamicCompilationSecurityManager.initialize();
        });
    }

    @Test
    void testSecureCompileWithSafeCode() throws Exception {
        // 测试安全代码的编译
        String safeCode = """
            package test;
            public class SafeClass {
                public String process() {
                    return "safe processing";
                }
            }
            """;

        Object result = DynamicCompilationSecurityManager.secureCompile(
            safeCode, "test.SafeClass", mockCompileUtils);

        assertNotNull(result);
        assertEquals("编译成功的对象", result.toString());
    }

    @Test
    void testSecureCompileWithDangerousCode() {
        // 测试危险代码的编译
        String dangerousCode = """
            package test;
            import java.lang.reflect.Method;
            public class DangerousClass {
                public void exploit() {
                    Class.forName("java.lang.Runtime");
                }
            }
            """;

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            DynamicCompilationSecurityManager.secureCompile(
                dangerousCode, "test.DangerousClass", mockCompileUtils);
        });

        assertTrue(exception.getMessage().contains("编译失败"));
    }

    @Test
    void testSecureCompileWithTimeout() {
        // 测试编译超时
        String timeoutCode = """
            package test;
            public class TimeoutClass {
                public void timeout() {
                    // This will timeout in mock
                }
            }
            """;

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            DynamicCompilationSecurityManager.secureCompile(
                timeoutCode, "test.TimeoutClass", mockCompileUtils);
        });

        assertTrue(exception.getMessage().contains("编译失败"));
    }

    @Test
    void testSecureCompileWithException() {
        // 测试编译异常
        String exceptionCode = """
            package test;
            public class ExceptionClass {
                public void exception() {
                    // This will cause exception in mock
                }
            }
            """;

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            DynamicCompilationSecurityManager.secureCompile(
                exceptionCode, "test.ExceptionClass", mockCompileUtils);
        });

        assertTrue(exception.getMessage().contains("编译失败"));
    }

    @Test
    void testBytecodeVerificationEnabled() {
        // 测试字节码验证是否启用
        boolean enabled = DynamicCompilationSecurityManager.isBytecodeVerificationEnabled();
        // 在测试环境中，字节码验证可能不可用
        assertFalse(enabled, "在测试环境中字节码验证通常不可用");
    }

    @Test
    void testManualBytecodeVerificationWithSafeBytecode() {
        // 测试手动字节码验证 - 安全字节码
        byte[] safeBytecode = "java/lang/String java/util/List".getBytes();
        boolean result = DynamicCompilationSecurityManager.manualBytecodeVerification(
            safeBytecode, "test.SafeClass");

        assertTrue(result, "安全字节码应该通过验证");
    }

    @Test
    void testManualBytecodeVerificationWithDangerousBytecode() {
        // 测试手动字节码验证 - 危险字节码
        byte[] dangerousBytecode = "java/lang/Runtime".getBytes();
        boolean result = DynamicCompilationSecurityManager.manualBytecodeVerification(
            dangerousBytecode, "test.DangerousClass");

        assertFalse(result, "危险字节码应该未通过验证");
    }

    @Test
    void testManualBytecodeVerificationWithMultipleDangerousPatterns() {
        // 测试包含多个危险模式的字节码验证
        String[] dangerousPatterns = {
            "java/lang/Runtime",
            "java/lang/Process",
            "java/lang/System",
            "java/io/File",
            "java/net/Socket",
            "java/lang/reflect/Method",
            "sun/misc/Unsafe"
        };

        for (String pattern : dangerousPatterns) {
            byte[] dangerousBytecode = pattern.getBytes();
            boolean result = DynamicCompilationSecurityManager.manualBytecodeVerification(
                dangerousBytecode, "test.DangerousClass");

            assertFalse(result, "包含 " + pattern + " 的字节码应该未通过验证");
        }
    }

    @Test
    void testManualBytecodeVerificationWithException() {
        // 测试手动字节码验证异常情况
        byte[] nullBytecode = null;
        boolean result = DynamicCompilationSecurityManager.manualBytecodeVerification(
            nullBytecode, "test.NullClass");

        assertFalse(result, "null字节码应该未通过验证");
    }

    @Test
    void testManualBytecodeVerificationWithEmptyBytecode() {
        // 测试空字节码验证
        byte[] emptyBytecode = new byte[0];
        boolean result = DynamicCompilationSecurityManager.manualBytecodeVerification(
            emptyBytecode, "test.EmptyClass");

        assertTrue(result, "空字节码应该通过验证");
    }

    @Test
    void testSetInstrumentation() {
        // 测试设置Instrumentation
        // 由于在测试环境中无法获取真实的Instrumentation实例，这里主要测试方法调用不出错
        assertDoesNotThrow(() -> {
            DynamicCompilationSecurityManager.setInstrumentation(null);
        });
    }

    @Test
    void testSecureCompileLogging() throws Exception {
        // 测试安全编译的日志记录
        String testCode = """
            package test;
            public class LogTestClass {
                public String safe() {
                    return "safe operation";
                }
            }
            """;

        // 这个测试主要确保日志记录不会影响编译过程
        Object result = DynamicCompilationSecurityManager.secureCompile(
            testCode, "test.LogTestClass", mockCompileUtils);

        assertNotNull(result);
    }

    @Test
    void testConcurrentSecureCompile() throws Exception {
        // 测试并发安全编译
        String safeCode = """
            package test;
            public class ConcurrentSafeClass {
                public String safe() {
                    return "concurrent safe processing";
                }
            }
            """;

        // 并发执行多个编译任务
        Object[] results = new Object[3];
        Thread[] threads = new Thread[3];

        for (int i = 0; i < 3; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    results[index] = DynamicCompilationSecurityManager.secureCompile(
                        safeCode, "test.ConcurrentSafeClass" + index, mockCompileUtils);
                } catch (Exception e) {
                    results[index] = e;
                }
            });
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证所有编译都成功
        for (Object result : results) {
            assertNotNull(result);
            assertFalse(result instanceof Exception, "并发编译不应该产生异常");
        }
    }
}
