package top.werls.springboottemplate.common.utils.dynamicCompilation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.tools.JavaFileObject;
import java.io.*;
import java.net.URI;

/**
 * CustomJavaFileObject 单元测试
 * 测试自定义Java文件对象的功能
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
@SpringBootTest
class CustomJavaFileObjectTest {

    private CustomJavaFileObject fileObject;
    private String binaryName;
    private URI uri;

    @BeforeEach
    void setUp() throws Exception {
        binaryName = "com.example.TestClass";
        uri = URI.create("file:///tmp/com/example/TestClass.class");
        fileObject = new CustomJavaFileObject(binaryName, uri);
    }

    @Test
    void testConstructor() throws Exception {
        // 测试构造函数
        String testBinaryName = "test.MyClass";
        URI testUri = URI.create("jar:file:/path/to/jar.jar!/test/MyClass.class");

        CustomJavaFileObject testObject = new CustomJavaFileObject(testBinaryName, testUri);
        assertNotNull(testObject);
        assertEquals(testBinaryName, testObject.binaryName());
        assertEquals(testUri, testObject.toUri());
    }

    @Test
    void testToUri() {
        // 测试获取URI
        assertEquals(uri, fileObject.toUri());
    }

    @Test
    void testBinaryName() {
        // 测试获取二进制名称
        assertEquals(binaryName, fileObject.binaryName());
    }

    @Test
    void testGetKind() {
        // 测试获取文件类型
        assertEquals(JavaFileObject.Kind.CLASS, fileObject.getKind());
    }

    @Test
    void testGetName() {
        // 测试获取文件名
        String name = fileObject.getName();
        assertNotNull(name);
        assertTrue(name.contains("TestClass.class"));
    }

    @Test
    void testGetNameWithJarUri() throws Exception {
        // 测试JAR URI的文件名获取
        URI jarUri = URI.create("jar:file:/path/to/jar.jar!/com/example/TestClass.class");
        CustomJavaFileObject jarFileObject = new CustomJavaFileObject(binaryName, jarUri);

        String name = jarFileObject.getName();
        assertNotNull(name);
        assertTrue(name.contains("TestClass.class"));
    }

    @Test
    void testIsNameCompatible() {
        // 测试名称兼容性检查
        assertTrue(fileObject.isNameCompatible("TestClass", JavaFileObject.Kind.CLASS));
        assertFalse(fileObject.isNameCompatible("TestClass", JavaFileObject.Kind.SOURCE));
        assertFalse(fileObject.isNameCompatible("OtherClass", JavaFileObject.Kind.CLASS));
    }

    @Test
    void testIsNameCompatibleWithPath() throws Exception {
        // 测试带路径的名称兼容性
        URI pathUri = URI.create("file:///com/example/TestClass.class");
        CustomJavaFileObject pathFileObject = new CustomJavaFileObject(binaryName, pathUri);

        assertTrue(pathFileObject.isNameCompatible("TestClass", JavaFileObject.Kind.CLASS));
    }

    @Test
    void testOpenOutputStream() {
        // 测试打开输出流（应该抛出异常）
        assertThrows(UnsupportedOperationException.class, () -> {
            fileObject.openOutputStream();
        });
    }

    @Test
    void testOpenReader() {
        // 测试打开字符读取器（应该抛出异常）
        assertThrows(UnsupportedOperationException.class, () -> {
            fileObject.openReader(false);
        });
    }

    @Test
    void testGetCharContent() {
        // 测试获取字符内容（应该抛出异常）
        assertThrows(UnsupportedOperationException.class, () -> {
            fileObject.getCharContent(false);
        });
    }

    @Test
    void testOpenWriter() {
        // 测试打开字符写入器（应该抛出异常）
        assertThrows(UnsupportedOperationException.class, () -> {
            fileObject.openWriter();
        });
    }

    @Test
    void testGetLastModified() {
        // 测试获取最后修改时间
        assertEquals(0, fileObject.getLastModified());
    }

    @Test
    void testDelete() {
        // 测试删除文件（应该抛出异常）
        assertThrows(UnsupportedOperationException.class, () -> {
            fileObject.delete();
        });
    }

    @Test
    void testGetNestingKind() {
        // 测试获取嵌套类型（应该抛出异常）
        assertThrows(UnsupportedOperationException.class, () -> {
            fileObject.getNestingKind();
        });
    }

    @Test
    void testGetAccessLevel() {
        // 测试获取访问级别（应该抛出异常）
        assertThrows(UnsupportedOperationException.class, () -> {
            fileObject.getAccessLevel();
        });
    }

    @Test
    void testToString() {
        // 测试toString方法
        String toString = fileObject.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("CustomJavaFileObject"));
        assertTrue(toString.contains(uri.toString()));
    }

    @Test
    void testWithDifferentUriSchemes() throws Exception {
        // 测试不同的URI方案
        URI[] testUris = {
            URI.create("file:///path/to/TestClass.class"),
            URI.create("jar:file:/path/to/jar.jar!/TestClass.class"),
            URI.create("http://example.com/TestClass.class")
        };

        for (URI testUri : testUris) {
            CustomJavaFileObject testObject = new CustomJavaFileObject("test.TestClass", testUri);
            assertEquals(testUri, testObject.toUri());
            assertEquals("test.TestClass", testObject.binaryName());
            assertEquals(JavaFileObject.Kind.CLASS, testObject.getKind());
        }
    }

    @Test
    void testEquals() throws Exception {
        // 测试对象相等性
        CustomJavaFileObject sameObject = new CustomJavaFileObject(binaryName, uri);
        CustomJavaFileObject differentObject = new CustomJavaFileObject("different.Class", uri);

        // 注意：CustomJavaFileObject没有重写equals方法，所以使用默认的引用比较
        assertNotEquals(fileObject, sameObject);
        assertEquals(fileObject, fileObject);
    }
}
