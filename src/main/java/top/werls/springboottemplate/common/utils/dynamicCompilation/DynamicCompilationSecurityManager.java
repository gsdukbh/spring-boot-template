package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.lang.instrument.Instrumentation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态编译安全管理器
 * 统一管理动态编译过程中的安全检查，包括字节码验证
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
@Slf4j
public class DynamicCompilationSecurityManager {

    /** 字节码安全验证器实例 */
    private static final BytecodeSecurityVerifier bytecodeVerifier = new BytecodeSecurityVerifier();

    /** 是否已初始化字节码验证
     * -- GETTER --
     *  检查字节码验证是否可用
     *
     * @return 如果字节码验证已启用返回true
     */
    @Getter
    private static boolean bytecodeVerificationEnabled = false;

    /** Instrumentation实例（如果可用） */
    private static Instrumentation instrumentation;

    /**
     * 初始化安全管理器
     * 尝试启用字节码验证功能
     */
    public static void initialize() {
        try {
            // 尝试启用字节码验证
            enableBytecodeVerification();
            log.info("动态编译安全管理器初始化成功");
        } catch (Exception e) {
            log.warn("无法启用字节码验证功能: {}", e.getMessage());
        }
    }

    /**
     * 启用字节码验证功能
     */
    private static void enableBytecodeVerification() {
        if (instrumentation != null && !bytecodeVerificationEnabled) {
            instrumentation.addTransformer(bytecodeVerifier);
            bytecodeVerificationEnabled = true;
            log.info("字节码安全验证已启用");
        } else {
            log.warn("Instrumentation不可用，字节码验证功能被禁用");
        }
    }

    /**
     * 设置Instrumentation实例
     * 通常由Java Agent调用
     *
     * @param inst Instrumentation实例
     */
    public static void setInstrumentation(Instrumentation inst) {
        instrumentation = inst;
        if (!bytecodeVerificationEnabled) {
            enableBytecodeVerification();
        }
    }

    /**
     * 执行安全的动态编
     * 集成所有安全检查机制
     *
     * @param code 源代码
     * @param className 类名
     * @param compileUtils 编译工具实例
     * @return 编译结果
     * @throws Exception 编译异常
     */
    public static Object secureCompile(String code, String className, DynamicCompileUtils compileUtils) throws Exception {
        log.debug("开始安全编译，类名: {}", className);

        try {
            // 1. 源码安全检查（已在DynamicCompileUtils中集成）
            // 2. 资源控制编译
            return CompilationResourceManager.executeCompilation(() -> {
                try {
                    // 调用内部编译方法，避免无限递归
                    return compileUtils.internalCompile(code, className);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            // 3. 字节码验证（通过BytecodeSecurityVerifier自动进行）

        } catch (CompilationResourceManager.CompilationException e) {
            log.error("编译过程中发生资源管理异常: {}", e.getMessage());
            // 对于资源管理相关的异常（如超时、中断），抛出SecurityException
            if (e.getMessage().contains("超时") || e.getMessage().contains("中断")) {
                throw new SecurityException("编译失败: " + e.getMessage(), e);
            }
            // 对于其他编译失败，返回null而不是抛出异常
            log.warn("编译失败，返回null: {}", e.getMessage());
            return null;
        }
    }

  /**
     * 手动验证字节码
     * 作为备用验证方法，当Instrumentation不可用时使用
     *
     * @param bytecode 字节码数据
     * @param className 类名
     * @return 验证是否通过
     */
    public static boolean manualBytecodeVerification(byte[] bytecode, String className) {
        try {
            // 使用BytecodeSecurityVerifier的内部逻辑进行验证
            String bytecodeStr = new String(bytecode);

            // 检查危险模式
            String[] dangerousPatterns = {
                "java/lang/Runtime",
                "java/lang/Process",
                "java/lang/System",
                "java/io/",
                "java/net/",
                "java/lang/reflect/",
                "sun/misc/Unsafe"
            };

            for (String pattern : dangerousPatterns) {
                if (bytecodeStr.contains(pattern)) {
                    log.warn("字节码验证失败，检测到危险模式: {} 在类: {}", pattern, className);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("手动字节码验证异常: {}", e.getMessage());
            return false;
        }
    }
}
