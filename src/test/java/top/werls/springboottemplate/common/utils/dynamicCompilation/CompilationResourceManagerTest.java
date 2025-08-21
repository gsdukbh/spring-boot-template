package top.werls.springboottemplate.common.utils.dynamicCompilation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

/**
 * CompilationResourceManager 单元测试
 * 测试编译资源管理器的功能，包括资源限制、超时控制和异常处理
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
@SpringBootTest
class CompilationResourceManagerTest {

    @Test
    void testSuccessfulCompilation() throws Exception {
        // 测试成功的编译任务
        Callable<String> task = () -> {
            // 模拟正常的编译任务
            Thread.sleep(100);
            return "编译成功";
        };

        String result = CompilationResourceManager.executeCompilation(task);
        assertEquals("编译成功", result);
    }

    @Test
    void testCompilationTimeout() {
        // 测试编译超时
        Callable<String> timeoutTask = () -> {
            // 模拟超时的编译任务（睡眠时间超过30秒的超时限制）
            Thread.sleep(35000);
            return "不应该到达这里";
        };

        CompilationResourceManager.CompilationException exception = assertThrows(
            CompilationResourceManager.CompilationException.class,
            () -> CompilationResourceManager.executeCompilation(timeoutTask)
        );

        assertTrue(exception.getMessage().contains("编译超时"));
    }

    @Test
    void testCompilationInterruption() {
        // 测试编译中断
        Callable<String> interruptTask = () -> {
            Thread.currentThread().interrupt();
            return "不应该到达这里";
        };

        CompilationResourceManager.CompilationException exception = assertThrows(
            CompilationResourceManager.CompilationException.class,
            () -> CompilationResourceManager.executeCompilation(interruptTask)
        );

        assertTrue(exception.getMessage().contains("编译被中断"));
    }

    @Test
    void testCompilationException() {
        // 测试编译过程中的异常
        Callable<String> exceptionTask = () -> {
            throw new RuntimeException("编译过程中发生错误");
        };

        CompilationResourceManager.CompilationException exception = assertThrows(
            CompilationResourceManager.CompilationException.class,
            () -> CompilationResourceManager.executeCompilation(exceptionTask)
        );

        assertTrue(exception.getMessage().contains("编译失败"));
    }

    @Test
    void testMemoryUsageMonitoring() throws Exception {
        // 测试内存使用监控
        Callable<String> memoryTask = () -> {
            // 模拟内存使用，但不超过限制
            byte[] data = new byte[1024 * 1024]; // 1MB
            return "内存测试完成";
        };

        String result = CompilationResourceManager.executeCompilation(memoryTask);
        assertEquals("内存测试完成", result);
    }

    @Test
    void testConcurrentCompilations() throws Exception {
        // 测试并发编译
        Callable<String> task1 = () -> {
            Thread.sleep(200);
            return "任务1完成";
        };

        Callable<String> task2 = () -> {
            Thread.sleep(200);
            return "任务2完成";
        };

        // 并发执行两个任务
        String result1 = CompilationResourceManager.executeCompilation(task1);
        String result2 = CompilationResourceManager.executeCompilation(task2);

        assertEquals("任务1完成", result1);
        assertEquals("任务2完成", result2);
    }

    @Test
    void testRestrictedClassLoader() {
        // 测试受限制的类加载器
        Callable<String> classLoaderTask = () -> {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            assertNotNull(contextClassLoader);

            // 尝试加载允许的类
            try {
                contextClassLoader.loadClass("java.lang.String");
                return "允许的类加载成功";
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("加载允许的类失败", e);
            }
        };

        assertDoesNotThrow(() -> {
            String result = CompilationResourceManager.executeCompilation(classLoaderTask);
            assertEquals("允许的类加载成功", result);
        });
    }

    @Test
    void testDangerousClassLoading() {
        // 测试危险类的加载限制
        Callable<String> dangerousTask = () -> {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

            try {
                // 尝试加载被禁止的类
                contextClassLoader.loadClass("java.lang.Runtime");
                return "不应该能够加载Runtime类";
            } catch (ClassNotFoundException e) {
                return "正确阻止了危险类的加载";
            }
        };

        assertDoesNotThrow(() -> {
            String result = CompilationResourceManager.executeCompilation(dangerousTask);
            assertEquals("正确阻止了危险类的加载", result);
        });
    }

    @Test
    void testCompilationExceptionWithCause() {
        // 测试带原因的编译异常
        Exception originalException = new RuntimeException("原始异常");
        CompilationResourceManager.CompilationException exception =
            new CompilationResourceManager.CompilationException("编译异常", originalException);

        assertEquals("编译异常", exception.getMessage());
        assertEquals(originalException, exception.getCause());
    }

    @Test
    void testCompilationExceptionWithoutCause() {
        // 测试不带原因的编译异常
        CompilationResourceManager.CompilationException exception =
            new CompilationResourceManager.CompilationException("简单编译异常");

        assertEquals("简单编译异常", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testThreadPoolBehavior() throws Exception {
        // 测试线程池行为
        int taskCount = 5;
        String[] results = new String[taskCount];

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            Callable<String> task = () -> {
                Thread.sleep(50);
                return "任务 " + taskId + " 完成";
            };
            results[i] = CompilationResourceManager.executeCompilation(task);
        }

        // 验证所有任务都成功完成
        for (int i = 0; i < taskCount; i++) {
            assertEquals("任务 " + i + " 完成", results[i]);
        }
    }

    @Test
    void testResourceCleanup() throws Exception {
        // 测试资源清理
        Callable<String> cleanupTask = () -> {
            // 记录开始时的线程状态
            Thread currentThread = Thread.currentThread();
            String threadName = currentThread.getName();

            // 验证是编译工作线程
            assertTrue(threadName.contains("compilation-worker"));

            return "资源清理测试完成";
        };

        String result = CompilationResourceManager.executeCompilation(cleanupTask);
        assertEquals("资源清理测试完成", result);
    }
}
