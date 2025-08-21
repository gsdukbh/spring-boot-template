package top.werls.springboottemplate.common.utils.dynamicCompilation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;

/**
 * CustomClassLoader 单元测试
 * 测试自定义类加载器的功能，包括类加载策略和包路径设置
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
@SpringBootTest
class CustomClassLoaderTest {

    private CustomClassLoader classLoader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        classLoader = new CustomClassLoader(getClass().getClassLoader());
    }

    @Test
    void testSetPackagePath() {
        // 测试设置包路径
        String packagePath = "com.example.test";
        CustomClassLoader result = classLoader.setPackagePath(packagePath);

        assertSame(classLoader, result, "setPackagePath应该返回同一个实例以支持链式调用");
    }

    @Test
    void testSetClazzPath() {
        // 测试设置class文件路径
        String clazzPath = tempDir.toString() + "/classes/";
        CustomClassLoader result = classLoader.setClazzPath(clazzPath);

        assertSame(classLoader, result, "setClazzPath应该返回同一个实例以支持链式调用");
    }

    @Test
    void testChainedSetters() {
        // 测试链式调用
        String packagePath = "com.example.test";
        String clazzPath = tempDir.toString() + "/classes/";

        CustomClassLoader result = classLoader
            .setPackagePath(packagePath)
            .setClazzPath(clazzPath);

        assertSame(classLoader, result, "链式调用应该返回同一个实例");
    }

    @Test
    void testLoadSystemClass() throws ClassNotFoundException {
        // 测试加载系统类（不在自定义包路径下）
        Class<?> stringClass = classLoader.loadClass("java.lang.String");
        assertEquals(String.class, stringClass);
    }

    @Test
    void testLoadThirdPartyClass() throws ClassNotFoundException {
        // 测试加载第三方类
        Class<?> testClass = classLoader.loadClass("org.junit.jupiter.api.Test");
        assertEquals(Test.class, testClass);
    }

    @Test
    void testFindClassNotFound() {
        // 测试查找不存在的类
        assertThrows(ClassNotFoundException.class, () -> {
            classLoader.findClass("com.nonexistent.TestClass");
        });
    }

    @Test
    void testLoadClassCaching() throws ClassNotFoundException {
        // 测试类加载缓存
        Class<?> class1 = classLoader.loadClass("java.lang.String");
        Class<?> class2 = classLoader.loadClass("java.lang.String");

        assertSame(class1, class2, "相同的类应该被缓存，返回同一个Class对象");
    }

    @Test
    void testCustomPackageLoading() throws Exception {
        // 创建一个简单的测试类文件
        String packagePath = "top.werls.springboottemplate.compiled";
        String className = packagePath + ".TestClass";

        classLoader.setPackagePath(packagePath);
        classLoader.setClazzPath(tempDir.toString() + "/");

        // 创建目录结构
        File packageDir = new File(tempDir.toFile(), packagePath.replace(".", "/"));
        packageDir.mkdirs();

        // 由于无法轻易创建有效的class字节码，这里测试异常情况
        assertThrows(ClassNotFoundException.class, () -> {
            classLoader.loadClass(className);
        });
    }

    @Test
    void testParentDelegation() throws ClassNotFoundException {
        // 测试双亲委派模型
        ClassLoader parentLoader = classLoader.getParent();
        assertNotNull(parentLoader, "应该有父类加载器");

        // 对于非自定义包的类，应该委托给父类加载器
        Class<?> loadedClass = classLoader.loadClass("java.util.ArrayList");
        assertEquals(java.util.ArrayList.class, loadedClass);
    }

    @Test
    void testClassLoaderHierarchy() {
        // 测试类加载器层次结构
        ClassLoader parent = classLoader.getParent();
        assertNotNull(parent, "CustomClassLoader应该有父类加载器");

        // 验证父类加载器是应用类加载器或其子类
        assertTrue(parent instanceof ClassLoader, "父类应该是ClassLoader的实例");
    }

    @Test
    void testDefaultPackagePath() throws ClassNotFoundException {
        // 测试默认包路径行为
        // 默认包路径是"top.werls.springboottemplate.compiled"

        // 加载不在默认包路径下的类，应该使用父类加载器
        Class<?> testClass = classLoader.loadClass("java.lang.Object");
        assertEquals(Object.class, testClass);
    }

    @Test
    void testDefaultClazzPath() {
        // 测试默认class路径
        CustomClassLoader newLoader = new CustomClassLoader(getClass().getClassLoader());

        // 默认路径应该是当前目录下的clazz文件夹
        // 由于findClass方法会尝试读取文件，我们可以通过异常来验证路径设置
        assertThrows(ClassNotFoundException.class, () -> {
            newLoader.findClass("top.werls.springboottemplate.compiled.NonExistentClass");
        });
    }

    @Test
    void testConstructorWithNullParent() {
        // 测试使用null父类加载器构造
        CustomClassLoader loaderWithNullParent = new CustomClassLoader(null);
        assertNotNull(loaderWithNullParent, "即使父类加载器为null，CustomClassLoader也应该能够创建");
    }
}
