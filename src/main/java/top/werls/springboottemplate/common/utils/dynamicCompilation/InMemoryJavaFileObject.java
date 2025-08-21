package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.io.IOException;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * 内存中的Java文件对象
 * 继承自SimpleJavaFileObject，用于在动态编译系统中表示存储在内存中的Java源代码
 *
 * <p>该类主要用于动态编译场景，将Java源代码字符串包装成JavaFileObject���
 * 使得Java编译器能够直接从内存中读取源代码进行编译，而不需要实际的文件系统支持</p>
 *
 * <p>特点：</p>
 * <ul>
 *   <li>内存存储：源代码直接存储在内存中，无需文件系统</li>
 *   <li>动态创建：可以根据类名和源代码动态创建文件对象</li>
 *   <li>编译器兼容：完全兼容Java编译器API</li>
 *   <li>虚拟URI：使用string://协议创建虚拟文件URI</li>
 * </ul>
 *
 * @author Li JiaWei
 * @version 1
 * @date 2023/3/31
 * @since on
 */
public class InMemoryJavaFileObject extends SimpleJavaFileObject {

  /** 存储在内存中的Java源代码内容 */
  private String contents = null;

  /**
   * 使用给定的URI和文件类型构造SimpleJavaFileObject
   * 这是受保护的构造方法，通常用于子类的内部实现
   *
   * @param uri  此文件对象的URI
   * @param kind 此文件对象的类型
   */
  protected InMemoryJavaFileObject(URI uri, Kind kind) {
    super(uri, kind);
  }

  /**
   * 根据类名和源代码内容创建内存中的Java文件对象
   * 自动生成虚拟URI，格式为：string:///package/path/ClassName.java
   *
   * @param className 完全限定的类名（如：com.example.MyClass）
   * @param contents  Java源代码内容
   * @throws Exception 创建过程中可能抛出的异常
   */
  public InMemoryJavaFileObject(String className, String contents) throws Exception {
    // 创建虚拟URI：将类名转换为文件路径格式，并添加.java扩展名
    // 例如：com.example.MyClass -> string:///com/example/MyClass.java
    super(URI.create("string:///" + className.replace('.', '/')
        + Kind.SOURCE.extension), Kind.SOURCE);
    this.contents = contents;
  }

  /**
   * 获取文件的字符内容
   * 返回存储在内存中的Java源代码
   *
   * @param ignoreEncodingErrors 是否忽略编码错误（当前实现中未使用）
   * @return 存储在内存中的Java源代码内容
   * @throws IOException 读取内容时可能抛出的IO异常
   */
  public CharSequence getCharContent(boolean ignoreEncodingErrors)
      throws IOException {
    return contents;
  }
}
