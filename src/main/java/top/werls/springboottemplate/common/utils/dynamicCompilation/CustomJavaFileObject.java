package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

/**
 * 自定义Java文件对象
 * 实现JavaFileObject接口，用于表示动态编译系统中的Java类文件
 *
 * <p>该类主要用于包装URI资源，使其能够被Java编译器识别和处理。
 * 支持从不同来源（文件系统、JAR包等）读取class文件</p>
 *
 * <p>特点：</p>
 * <ul>
 *   <li>只读设计：仅支持输入流操作，不支持写入操作</li>
 *   <li>URI兼容：能够处理各种类型的URI（文件系统、JAR等）</li>
 *   <li>编译器集成：与Java编译器API无缝集成</li>
 * </ul>
 *
 * @author Li JiaWei
 * @version 1
 * @date 2023/3/31
 * @since on
 */
public class CustomJavaFileObject implements JavaFileObject {

  /** 类的二进制名称（完全限定类名） */
  private final String binaryName;

  /** 文件的URI位置 */
  private final URI uri;

  /** 文件名称 */
  private final String name;

  /**
   * 构造自定义Java文件对象
   * 根据URI类型智能确定文件名称
   *
   * @param binaryName 类的二进制名称（��：com.example.MyClass）
   * @param uri 文件的URI位置
   */
  public CustomJavaFileObject(String binaryName, URI uri) {
    this.uri = uri;
    this.binaryName = binaryName;
    // 根据URI类型确定文件名：
    // - 文件系统URI：使用path部分
    // - JAR URI：使用scheme specific部分
    name = uri.getPath() == null ? uri.getSchemeSpecificPart() : uri.getPath();
  }

  /**
   * 获取文件的URI
   *
   * @return 文件的URI位置
   */
  @Override
  public URI toUri() {
    return uri;
  }

  /**
   * 打开输入流读取文件内容
   * 支持各种类型的URI（文件系统、JAR包等）
   *
   * @return 文件内容的输入流
   * @throws IOException 文件访问异常
   */
  @Override
  public InputStream openInputStream() throws IOException {
    // 通过URI.toURL()方法统一处理各种类型的URI
    return uri.toURL().openStream();
  }

  /**
   * 打开输出流写��文件内容
   * 当前实现不支持写入操作
   *
   * @return 不支持，抛出异常
   * @throws IOException 不支持的操作异常
   */
  @Override
  public OutputStream openOutputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * 获取文件名称
   *
   * @return 文件名称
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * 打开字符读取器
   * 当前实现不支持字符读取操作
   *
   * @param ignoreEncodingErrors 是否忽略编码错误
   * @return 不支持，抛出异常
   * @throws IOException 不支持的操作异常
   */
  @Override
  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * 获取文件的字符内容
   * 当前实现不支持字符内容获取
   *
   * @param ignoreEncodingErrors 是否忽略编码错误
   * @return 不支持，抛出异常
   * @throws IOException 不支持的操作异常
   */
  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * 打开字符写入器
   * 当前实现不支持字符写入操作
   *
   * @return 不支持，抛出异常
   * @throws IOException 不支持的操作异常
   */
  @Override
  public Writer openWriter() throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * 获取文件最后修改时间
   * 当前实现返回固定值0
   *
   * @return 固定返回0
   */
  @Override
  public long getLastModified() {
    return 0;
  }

  /**
   * 删除文件
   * 当前实现不支持删除操作
   *
   * @return 不支持，抛出异常
   */
  @Override
  public boolean delete() {
    throw new UnsupportedOperationException();
  }

  /**
   * 获取文件类型
   * 固定返回CLASS类型，表示这是一个编译后的class文件
   *
   * @return Kind.CLASS
   */
  @Override
  public Kind getKind() {
    return Kind.CLASS;
  }

  /**
   * 检查文件名是否与指定的简单名称和类型兼容
   * 参考SimpleJavaFileManager的实现
   *
   * @param simpleName 简单类名
   * @param kind 文件类型
   * @return 如果兼容返回true，否则返回false
   */
  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    String baseName = simpleName + kind.extension;
    return kind.equals(getKind())
        && (baseName.equals(getName())
        || getName().endsWith("/" + baseName));
  }

  /**
   * 获取嵌套类型
   * 当前实现不支持嵌套类型查询
   *
   * @return 不支持，抛出异常
   */
  @Override
  public NestingKind getNestingKind() {
    throw new UnsupportedOperationException();
  }

  /**
   * 获取访问级别
   * 当前实现不支持访问级别查询
   *
   * @return 不支持，抛出异常
   */
  @Override
  public Modifier getAccessLevel() {
    throw new UnsupportedOperationException();
  }

  /**
   * 获取类的二进制名称
   * 提供给文件管理器使用的便捷方法
   *
   * @return 类的完全限定二进制名称
   */
  public String binaryName() {
    return binaryName;
  }

  /**
   * 返回对象的字符串表示
   *
   * @return 包含URI信息的字符串表示
   */
  @Override
  public String toString() {
    return "CustomJavaFileObject{" +
        "uri=" + uri +
        '}';
  }
}
