package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义类加载器
 * 用于动态编译系统中加载编译后的class文件，支持从指定路径加载类并实现自定义的类加载策略
 *
 * <p>该类加载器采用双亲委派模型的变种，对特定包路径下的类使用自定义加载逻辑，
 * 其他类则委托给父类加载器处理</p>
 *
 * @author Li JiaWei
 * @version 1
 * @date 2023/3/31
 * @since on
 */
@Slf4j
public class CustomClassLoader extends ClassLoader {

  /**
   * 构造自定义类加载器
   *
   * @param parent 父类加载器，遵循双亲委派模型
   */
  public CustomClassLoader(ClassLoader parent) {
    super(parent);
  }

  /** 包路径，用于确定哪些类需要使用自定义加载逻辑 */
  private String mPackagePath = "top.werls.springboottemplate.compiled";

  /**
   * 设置包路径
   * 只有以此路径开头的类才会使用自定义的findClass方法加载
   *
   * @param packagePath 包路径，如：top.werls.springboottemplate.compiled
   * @return 返回当前实例，支持链式调用
   */
  public CustomClassLoader setPackagePath(String packagePath) {
    mPackagePath = packagePath;
    return this;
  }

  /** class文件存储的根路径 */
  private String clazzPath = new File("").getAbsolutePath() + "/clazz/";

  /**
   * 设置class文件存储路径
   *
   * @param clazzPath class文件存储的根目录路径
   * @return 返回当前实例，支持链式调用
   */
  public CustomClassLoader setClazzPath(String clazzPath) {
    this.clazzPath = clazzPath;
    return this;
  }

  /**
   * 查找并加载指定名称的类
   * 从指定的class文件路径中读取字节码并定义类
   *
   * @param name 完全限定的类名，如：com.example.MyClass
   * @return 加载后的Class对象
   * @throws ClassNotFoundException 当无法找到或加载类文件时抛出
   */
  @Override
  public Class<?> findClass(String name) throws ClassNotFoundException {
    // 根据类名构建class文件的完整路径
    String path = clazzPath + name.replace(".", "/") + ".class";
    byte[] data = new byte[0];
    try {
      // 读取class文件的字节码数据
      data = Files.readAllBytes(Paths.get(path));
    } catch (IOException e) {
      throw new ClassNotFoundException("CustomClassLoader findClass error", e);
    }
    // 使用读取的字节码定义并返回Class对象
    return defineClass(name, data, 0, data.length);
  }

  /**
   * 重写loadClass方法，实现自定义的类加载策略
   * 采用双亲委派模型的变种：
   * 1. 检查类是否已经被加载
   * 2. 对特定包路径下的类使用自定义加载逻辑
   * 3. 其他类委托给父类加载器处理
   *
   * @param name 要加载的类的完全限定名
   * @return 加载后的Class对象
   * @throws ClassNotFoundException 当无法加载类时抛出
   */
  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    // 如果类已经被加载过，直接返回缓存的类
    if (findLoadedClass(name) != null) {
      return findLoadedClass(name);
    }
    // 如果类的全限定名以指定包路径开头，就使用自定义的findClass方法来加载
    if (name.startsWith(mPackagePath)) {
      return findClass(name);
    }
    // 否则，委托给父类加载器尝试加载（遵循双亲委派模型）
    return super.loadClass(name);
  }
}
