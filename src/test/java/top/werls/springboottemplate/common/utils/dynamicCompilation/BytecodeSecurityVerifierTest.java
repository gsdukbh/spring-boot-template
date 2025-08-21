package top.werls.springboottemplate.common.utils.dynamicCompilation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * BytecodeSecurityVerifier 单元测试
 * 测试字节码安全验证器的功能
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
@SpringBootTest
class BytecodeSecurityVerifierTest {

    private BytecodeSecurityVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new BytecodeSecurityVerifier();
    }

    @Test
    void testTransformNullClassName() {
        // 测试类名为null的情况
        byte[] originalBytecode = "safe bytecode".getBytes();
        byte[] result = verifier.transform(
            getClass().getClassLoader(),
            null,
            null,
            null,
            originalBytecode
        );

        assertSame(originalBytecode, result, "类名为null时应该返回原始字节码");
    }

    @Test
    void testTransformNonDynamicClass() {
        // 测试非动态编译类
        byte[] originalBytecode = "safe bytecode".getBytes();
        byte[] result = verifier.transform(
            getClass().getClassLoader(),
            "com.example.RegularClass",
            null,
            null,
            originalBytecode
        );

        assertSame(originalBytecode, result, "非动态编译类应该返回原始字节码");
    }

    @Test
    void testTransformSafeDynamicClass() {
        // 测试安全的动态编译类
        byte[] safeBytecode = "safe dynamic class bytecode".getBytes();
        byte[] result = verifier.transform(
            getClass().getClassLoader(),
            "dynamicCompilation.SafeClass",
            null,
            null,
            safeBytecode
        );

        assertSame(safeBytecode, result, "安全的动态编译类应该返回原始字节码");
    }

    @Test
    void testTransformDangerousBytecode_Runtime() {
        // 测试包含Runtime的危险字节码
        byte[] dangerousBytecode = "java/lang/Runtime".getBytes();

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            verifier.transform(
                getClass().getClassLoader(),
                "dynamicCompilation.DangerousClass",
                null,
                null,
                dangerousBytecode
            );
        });

        assertTrue(exception.getMessage().contains("检测到危险字节码"));
        assertTrue(exception.getMessage().contains("dynamicCompilation.DangerousClass"));
    }

    @Test
    void testTransformDangerousBytecode_Process() {
        // 测试包含Process的危险字节码
        byte[] dangerousBytecode = "java/lang/Process".getBytes();

        assertThrows(SecurityException.class, () -> {
            verifier.transform(
                getClass().getClassLoader(),
                "dynamicCompilation.DangerousClass",
                null,
                null,
                dangerousBytecode
            );
        });
    }

    @Test
    void testTransformDangerousBytecode_System() {
        // 测试包含System的危险字节码
        byte[] dangerousBytecode = "java/lang/System".getBytes();

        assertThrows(SecurityException.class, () -> {
            verifier.transform(
                getClass().getClassLoader(),
                "dynamicCompilation.DangerousClass",
                null,
                null,
                dangerousBytecode
            );
        });
    }

    @Test
    void testTransformDangerousBytecode_IO() {
        // 测试包含IO的危险字节码
        byte[] dangerousBytecode = "java/io/FileWriter".getBytes();

        assertThrows(SecurityException.class, () -> {
            verifier.transform(
                getClass().getClassLoader(),
                "dynamicCompilation.DangerousClass",
                null,
                null,
                dangerousBytecode
            );
        });
    }

    @Test
    void testTransformDangerousBytecode_Network() {
        // 测试包含网络操作的危险字节码
        byte[] dangerousBytecode = "java/net/Socket".getBytes();

        assertThrows(SecurityException.class, () -> {
            verifier.transform(
                getClass().getClassLoader(),
                "dynamicCompilation.DangerousClass",
                null,
                null,
                dangerousBytecode
            );
        });
    }

    @Test
    void testTransformDangerousBytecode_Reflection() {
        // 测试包含反射的危险字节码
        byte[] dangerousBytecode = "java/lang/reflect/Method".getBytes();

        assertThrows(SecurityException.class, () -> {
            verifier.transform(
                getClass().getClassLoader(),
                "dynamicCompilation.DangerousClass",
                null,
                null,
                dangerousBytecode
            );
        });
    }

    @Test
    void testTransformDangerousBytecode_Unsafe() {
        // 测试包含Unsafe的危险字节码
        byte[] dangerousBytecode = "sun/misc/Unsafe".getBytes();

        assertThrows(SecurityException.class, () -> {
            verifier.transform(
                getClass().getClassLoader(),
                "dynamicCompilation.DangerousClass",
                null,
                null,
                dangerousBytecode
            );
        });
    }

    @Test
    void testMultipleDangerousPatterns() {
        // 测试包含多个危险模式的字节码
        String dangerousBytecodeStr = "java/lang/Runtime java/io/File java/net/Socket";
        byte[] dangerousBytecode = dangerousBytecodeStr.getBytes();

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            verifier.transform(
                getClass().getClassLoader(),
                "dynamicCompilation.VeryDangerousClass",
                null,
                null,
                dangerousBytecode
            );
        });

        assertTrue(exception.getMessage().contains("检测到危险字节码"));
    }

    @Test
    void testDynamicClassIdentification() {
        // 测试动态类识别逻辑
        byte[] safeBytecode = "safe bytecode".getBytes();

        // 包含dynamicCompilation的类名应该被识别为动态类
        String[] dynamicClassNames = {
            "dynamicCompilation.TestClass",
            "com.example.dynamicCompilation.TestClass",
            "test.dynamicCompilation.sub.TestClass"
        };

        for (String className : dynamicClassNames) {
            byte[] result = verifier.transform(
                getClass().getClassLoader(),
                className,
                null,
                null,
                safeBytecode
            );
            assertSame(safeBytecode, result, "动态类 " + className + " 应该被正确处理");
        }

        // 不包含dynamicCompilation的类名不应该被识别为动态类
        String[] nonDynamicClassNames = {
            "com.example.TestClass",
            "test.compilation.TestClass",
            "dynamic.TestClass"
        };

        for (String className : nonDynamicClassNames) {
            byte[] result = verifier.transform(
                getClass().getClassLoader(),
                className,
                null,
                null,
                safeBytecode
            );
            assertSame(safeBytecode, result, "非动态类 " + className + " 应该直接返回原始字节码");
        }
    }

    @Test
    void testEmptyBytecode() {
        // 测试空字节码
        byte[] emptyBytecode = new byte[0];

        byte[] result = verifier.transform(
            getClass().getClassLoader(),
            "dynamicCompilation.EmptyClass",
            null,
            null,
            emptyBytecode
        );

        assertSame(emptyBytecode, result, "空字节码应该正常通过");
    }

    @Test
    void testBytecodeWithSafeContent() {
        // 测试包含安全内容的字节码
        String safeBytecodeStr = "java/lang/String java/util/List java/math/BigDecimal";
        byte[] safeBytecode = safeBytecodeStr.getBytes();

        byte[] result = verifier.transform(
            getClass().getClassLoader(),
            "dynamicCompilation.SafeClass",
            null,
            null,
            safeBytecode
        );

        assertSame(safeBytecode, result, "包含安全内容的字节码应该正常通过");
    }
}
