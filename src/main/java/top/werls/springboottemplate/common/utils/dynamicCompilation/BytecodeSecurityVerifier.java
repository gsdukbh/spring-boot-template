package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * 字节码安全验证器
 * 在类加载时进行字节码级别的安全检查
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
public class BytecodeSecurityVerifier implements ClassFileTransformer {

    /** 危险字节码指令集 */
    private static final Set<String> DANGEROUS_BYTECODE_PATTERNS = new HashSet<>();

    static {
        // 添加危险的字节码模式
        DANGEROUS_BYTECODE_PATTERNS.add("java/lang/Runtime");
        DANGEROUS_BYTECODE_PATTERNS.add("java/lang/Process");
        DANGEROUS_BYTECODE_PATTERNS.add("java/lang/System");
        DANGEROUS_BYTECODE_PATTERNS.add("java/io/");
        DANGEROUS_BYTECODE_PATTERNS.add("java/net/");
        DANGEROUS_BYTECODE_PATTERNS.add("java/lang/reflect/");
        DANGEROUS_BYTECODE_PATTERNS.add("sun/misc/Unsafe");
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        // 只检查动态编译的类
        if (isDynamicClass(className)) {
            if (containsDangerousBytecode(classfileBuffer)) {
                throw new SecurityException("检测到危险字节码: " + className);
            }
        }

        return classfileBuffer; // 返回原始字节码
    }

    /**
     * 判断是否为动态编译的类
     */
    private boolean isDynamicClass(String className) {
        // 可以根据包名或其他标识判断
        return className != null && className.contains("dynamicCompilation");
    }

    /**
     * 检查字节码是否包含危险指令
     */
    private boolean containsDangerousBytecode(byte[] bytecode) {
        String bytecodeStr = new String(bytecode);

        for (String pattern : DANGEROUS_BYTECODE_PATTERNS) {
            if (bytecodeStr.contains(pattern)) {
                return true;
            }
        }

        return false;
    }
}
