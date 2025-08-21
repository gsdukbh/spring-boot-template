package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 编译资源管理器
 * 提供编译过程的资源限制和隔离机制
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
public class CompilationResourceManager {

    /** 最大并发编译数量 */
    private static final int MAX_CONCURRENT_COMPILATIONS = 3;

    /** 编译超时时间（秒） */
    private static final int COMPILATION_TIMEOUT_SECONDS = 30;

    /** 最大内存使用限制（MB） */
    private static final long MAX_MEMORY_MB = 100;

    /** 线程池 */
    private static final ExecutorService COMPILATION_EXECUTOR =
        new ThreadPoolExecutor(
            1,
            MAX_CONCURRENT_COMPILATIONS,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "compilation-worker-" + counter.incrementAndGet());
                    t.setDaemon(true);
                    // 设置受限的权限
                    t.setPriority(Thread.MIN_PRIORITY);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

    /**
     * 在受控环境中执行编译任务
     *
     * @param compilationTask 编译任务
     * @param <T> 返回类型
     * @return 编译结果
     * @throws CompilationException 编译异常
     */
    public static <T> T executeCompilation(Callable<T> compilationTask) throws CompilationException {
        Future<T> future = COMPILATION_EXECUTOR.submit(() -> {
            // 记录编译开始时的内存使用
            long startMemory = getUsedMemory();

            try {
                // 设置线程上下文类加载器为受限的类加载器
                Thread.currentThread().setContextClassLoader(
                    new RestrictedClassLoader(Thread.currentThread().getContextClassLoader())
                );

                T result = compilationTask.call();

                // 检查内存使用
                long endMemory = getUsedMemory();
                if (endMemory - startMemory > MAX_MEMORY_MB * 1024 * 1024) {
                    throw new CompilationException("编译过程内存使用超过限制");
                }

                return result;
            } catch (Exception e) {
                throw new RuntimeException("编译执行失败", e);
            }
        });

        try {
            return future.get(COMPILATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new CompilationException("编译超时");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompilationException("编译被中断");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                RuntimeException re = (RuntimeException) cause;
                if (re.getCause() instanceof Exception) {
                    throw new CompilationException("编译失败", (Exception) re.getCause());
                }
            }
            throw new CompilationException("编译执行异常", e);
        }
    }

    /**
     * 获取当前JVM使用的内存
     */
    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * 受限制的类加载器
     */
    private static class RestrictedClassLoader extends ClassLoader {
        private final ClassLoader parent;

        public RestrictedClassLoader(ClassLoader parent) {
            super(parent);
            this.parent = parent;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            // 检查是否为允许加载的类
            if (isDangerousClass(name)) {
                throw new ClassNotFoundException("禁止加载的类: " + name);
            }
            return parent.loadClass(name);
        }

        private boolean isDangerousClass(String className) {
            return className.startsWith("java.lang.reflect") ||
                   className.startsWith("java.io") ||
                   className.startsWith("java.net") ||
                   className.equals("java.lang.Runtime") ||
                   className.equals("java.lang.Process") ||
                   className.equals("java.lang.System");
        }
    }

    /**
     * 编译异常
     */
    public static class CompilationException extends Exception {
        public CompilationException(String message) {
            super(message);
        }

        public CompilationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
