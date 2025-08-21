package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

/**
 * 自定义Java文件管理器
 * 用于动态编译系统中管理Java文件和类文件的访问，支持自定义类加载器与标准文件管理器的集成
 *
 * <p>该文件管理器作为Java编译器和自定义类加载器之间的桥梁，负责：</p>
 * <ul>
 *   <li>管理编译过程中的文件访问</li>
 *   <li>处理不同位置的类文件查找</li>
 *   <li>协调自定义类加载器与标准文件管理器的工作</li>
 * </ul>
 *
 * @author Li JiaWei
 * @version 1
 * @date 2023/3/31
 * @since on
 */
public class CustomClassloaderJavaFileManager implements JavaFileManager {

  /** 自定义类加载器，用于加载动态编译的类 */
  private final ClassLoader classLoader;

  /** 标准Java文件管理器，处理系统类和标准库 */
  private final StandardJavaFileManager standardFileManager;

  /** 包内部查找器，用于查找特定包下的类文件 */
  private final PackageInternalsFinder finder;

  /**
   * 构造自定义Java文件管理器
   *
   * @param classLoader 自定义类加载器
   * @param standardFileManager 标准Java文件管理器
   */
  public CustomClassloaderJavaFileManager(ClassLoader classLoader,
      StandardJavaFileManager standardFileManager) {
    this.classLoader = classLoader;
    this.standardFileManager = standardFileManager;
    finder = new PackageInternalsFinder(classLoader);
  }

  /**
   * 获取指定位置的类加载器
   *
   * @param location 文件位置
   * @return 自定义类加载器
   */
  @Override
  public ClassLoader getClassLoader(Location location) {
    return classLoader;
  }

  /**
   * 推断Java文件对象的二进制名称
   * 根据文件类型选择合适的处理方式
   *
   * @param location 文件位置
   * @param file Java文件对象
   * @return 二进制类名
   */
  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    if (file instanceof CustomJavaFileObject) {
      // 如果是自定义Java文件对象，使用其内置的二进制名称
      return ((CustomJavaFileObject) file).binaryName();
    } else {
      // 如果是标准文件对象，委托给标准文件管理器处理
      return standardFileManager.inferBinaryName(location, file);
    }
  }

  /**
   * 判断两个文件对象是否表示同一个文件
   *
   * @param a 文件对象A
   * @param b 文件对象B
   * @return 如果是同一个文件返回true，否则返回false
   */
  @Override
  public boolean isSameFile(FileObject a, FileObject b) {
    return standardFileManager.isSameFile(a, b);
  }

  /**
   * 处理编译器选项
   * 委托給標準文件管理器處理
   *
   * @param current 当前选项
   * @param remaining 剩余选项迭代器
   * @return 处理结果
   */
  @Override
  public boolean handleOption(String current, Iterator<String> remaining) {
    // 委托给标准文件管理器处理编译器选项
    return standardFileManager.handleOption(current, remaining);
  }

  /**
   * 检查是否支持指定的文件位置
   * 只支持类路径和平台类路径
   *
   * @param location 文件位置
   * @return 如果支持返回true，否则返回false
   */
  @Override
  public boolean hasLocation(Location location) {
    // 只关心类路径和平台类路径，不需要源码和其他位置类型进行编译
    return location == StandardLocation.CLASS_PATH || location
        == StandardLocation.PLATFORM_CLASS_PATH;
  }

  /**
   * 获取用于输入的Java文件对象
   * 委托给标准文件管理器处理
   *
   * @param location 文件位置
   * @param className 类名
   * @param kind 文件类型
   * @return Java文件对象
   * @throws IOException 文件访问异常
   */
  @Override
  public JavaFileObject getJavaFileForInput(Location location, String className,
      Kind kind) throws IOException {
    // 委托给默认管理器处理
    return standardFileManager.getJavaFileForInput(location, className, kind);
  }

  /**
   * 获取模块的位置
   *
   * @param location 基础位置
   * @param moduleName 模块名称
   * @return 模块位置
   * @throws IOException 文件访问异常
   */
  @Override
  public Location getLocationForModule(Location location, String moduleName) throws IOException {
    return standardFileManager.getLocationForModule(location, moduleName);
  }

  /**
   * 根据Java文件对象获取模块位置
   *
   * @param location 基础位置
   * @param fo Java文件对象
   * @return 模块位置
   * @throws IOException 文件访问异常
   */
  @Override
  public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
    return standardFileManager.getLocationForModule(location, fo);
  }

  /**
   * 列出模块的所有位置
   *
   * @param location 基础位置
   * @return 位置集合的可迭代对象
   * @throws IOException 文件访问异常
   */
  @Override
  public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
    return standardFileManager.listLocationsForModules(location);
  }

  /**
   * 推断模块名称
   *
   * @param location 位置
   * @return 模块名称
   * @throws IOException 文件访问异常
   */
  @Override
  public String inferModuleName(Location location) throws IOException {
    return standardFileManager.inferModuleName(location);
  }

  /**
   * 获取用于输出的Java文件对象
   *
   * @param location 文件位置
   * @param className 类名
   * @param kind 文件类型
   * @param sibling 关联文件对象
   * @return Java文件对象
   * @throws IOException 文件访问异常
   */
  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className,
      Kind kind, FileObject sibling) throws IOException {
    return standardFileManager.getJavaFileForOutput(location, className, kind, sibling);
  }

  /**
   * 获取用于输入的文件对象
   *
   * @param location 文件位置
   * @param packageName 包名
   * @param relativeName 相对文件名
   * @return 文件对象
   * @throws IOException 文件访问异常
   */
  @Override
  public FileObject getFileForInput(Location location, String packageName, String relativeName)
      throws IOException {
    return standardFileManager.getFileForInput(location, packageName, relativeName);
  }

  /**
   * 获取用于输出的文件对象
   *
   * @param location 文件位置
   * @param packageName 包名
   * @param relativeName 相对文件名
   * @param sibling 关联文件对象
   * @return 文件对象
   * @throws IOException 文件访问异常
   */
  @Override
  public FileObject getFileForOutput(Location location, String packageName, String relativeName,
      FileObject sibling) throws IOException {
    return standardFileManager.getFileForOutput(location, packageName, relativeName, sibling);
  }

  /**
   * 刷新缓冲区
   * 当前实现为空，不需要刷新操作
   *
   * @throws IOException 文件访问异常
   */
  @Override
  public void flush() throws IOException {
    // 不需要执行任何操作
  }

  /**
   * 关闭文件管理器
   * 当前实现为空，不需要关闭操作
   *
   * @throws IOException 文件访问异常
   */
  @Override
  public void close() throws IOException {
    // 不需要执行任何操作
  }

  /**
   * 列出指定位置和包下的Java文件对象
   * 实现自定义的文件查找逻辑，区分系统类和应用类
   *
   * @param location 文件位置
   * @param packageName 包名
   * @param kinds 文件类型集合
   * @param recurse 是否递归查找
   * @return Java文件对象的可迭代集合
   * @throws IOException 文件访问异常
   */
  @Override
  public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds,
      boolean recurse) throws IOException {

    // 检查是否为基础模块
    boolean baseModule = location.getName().equals("SYSTEM_MODULES[java.base]");
    if (baseModule || location == StandardLocation.PLATFORM_CLASS_PATH) {
      // 对于基础模块和平台类路径，使用标准文件管理器处理
      return standardFileManager.list(location, packageName, kinds, recurse);
    } else if (location == StandardLocation.CLASS_PATH && kinds.contains(Kind.CLASS)) {
      if (packageName.startsWith("java") || packageName.startsWith("com.sun")) {
        // 对于Java系统包和sun包，使用标准文件管理器处理
        return standardFileManager.list(location, packageName, kinds, recurse);
      } else {
        // 对于应用特定的类，使用自定义查找器
        return finder.find(packageName);
      }
    }

    // 其他情况返回空列表
    return Collections.emptyList();
  }

  /**
   * 检查是否支持指定的编译器选项
   *
   * @param option 编译器选项
   * @return 支持级别，-1表示不支持
   */
  @Override
  public int isSupportedOption(String option) {
    return -1;
  }
}
