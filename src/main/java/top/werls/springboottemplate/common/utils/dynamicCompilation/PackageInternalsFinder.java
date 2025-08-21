package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import javax.tools.JavaFileObject;

/**
 * 包内部查找器
 * 用于在动态编译系统中查找指定包下的所有Java类文件（.class文件）
 *
 * <p>该类能够从不同的来源查找类文件：</p>
 * <ul>
 *   <li>文件系统目录：扫描本地文件系统中的.class文件</li>
 *   <li>JAR文件：扫描JAR包中的.class文件</li>
 * </ul>
 *
 * <p>主要用途：</p>
 * <ul>
 *   <li>支持动态编译过程中的类路径扫描</li>
 *   <li>为自定义类加载器提供类文件发现功能</li>
 *   <li>统一处理不同来源的类文件访问</li>
 * </ul>
 *
 * @author Li JiaWei
 * @version 1
 * @date 2023/3/31
 * @since on
 */
public class PackageInternalsFinder {

  /** 用于查找资源的类加载器 */
  private final ClassLoader classLoader;

  /** .class文件的扩展名常量 */
  private static final String CLASS_FILE_EXTENSION = ".class";

  /**
   * 构造包内部查找器
   *
   * @param classLoader 用于查找资源的类加载器
   */
  public PackageInternalsFinder(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * 查找指定包下的所有Java文件对象
   * 遍历类路径中所有包含该包的位置（目录或JAR文件）
   *
   * @param packageName 包名，使用点分隔符（如：com.example.package）
   * @return 包含该包下所有.class文件的Java文件对象列表
   * @throws IOException 访问文件或JAR时可能抛出的IO异常
   */
  public List<JavaFileObject> find(String packageName) throws IOException {
    // 将包名转换为文件路径格式（用/替换.）
    String javaPackageName = packageName.replaceAll("\\.", "/");

    List<JavaFileObject> result = new ArrayList<JavaFileObject>();

    // 获取类路径中所有包含该包的URL位置
    Enumeration<URL> urlEnumeration = classLoader.getResources(javaPackageName);
    while (urlEnumeration.hasMoreElements()) {
      // 类路径中的每个JAR或目录都会有一个对应的URL
      URL packageFolderURL = urlEnumeration.nextElement();
      result.addAll(listUnder(packageName, packageFolderURL));
    }

    return result;
  }

  /**
   * 列出指定URL位置下的所有Java文件对象
   * 根据URL类型选择不同的处理方式
   *
   * @param packageName 包名
   * @param packageFolderURL 包所在的URL位置
   * @return 该位置下的所有Java文件对象集合
   */
  private Collection<JavaFileObject> listUnder(String packageName, URL packageFolderURL) {
    File directory = new File(packageFolderURL.getFile());
    if (directory.isDirectory()) {
      // 处理本地文件系统目录 - 适用于本地执行环境
      return processDir(packageName, directory);
    } else {
      // 处理JAR文件
      return processJar(packageFolderURL);
    }
    // 注：可能还有其他更复杂的类加载器需要处理的情况
  }

  /**
   * 处理JAR文件中的类文件
   * 扫描JAR包内指定包路径下的所有.class文件
   *
   * @param packageFolderURL JAR包中包路径的URL
   * @return JAR包中该包下的所有Java文件对象列表
   */
  private List<JavaFileObject> processJar(URL packageFolderURL) {
    List<JavaFileObject> result = new ArrayList<JavaFileObject>();
    try {
      // 建立JAR连接
      JarURLConnection jarConn = (JarURLConnection) packageFolderURL.openConnection();
      String rootEntryName = jarConn.getEntryName();
      int rootEnd = rootEntryName.length() + 1;

      // 构建JAR文件的URI（去掉!后面的部分）
      int endIndex = packageFolderURL.toExternalForm().lastIndexOf("!");
      String jarUri = packageFolderURL.toExternalForm().substring(0, endIndex);

      // 遍历JAR文件中的所有条目
      Enumeration<JarEntry> entryEnum = jarConn.getJarFile().entries();
      while (entryEnum.hasMoreElements()) {
        JarEntry jarEntry = entryEnum.nextElement();
        String name = jarEntry.getName();

        // 检查条目是否：
        // 1. 以目标包路径开头
        // 2. 是直接子文件（不是子目录中的文件）
        // 3. 以.class扩展名结尾
        if (name.startsWith(rootEntryName) &&
            name.indexOf('/', rootEnd) == -1 &&
            name.endsWith(CLASS_FILE_EXTENSION)) {

          // 创建完整的JAR条目URI
          URI uri = URI.create(jarUri + "!/" + name);

          // 将文件路径转换为二进制类名
          String binaryName = name.replaceAll("/", ".");
          binaryName = binaryName.replaceAll(CLASS_FILE_EXTENSION + "$", "");

          result.add(new CustomJavaFileObject(binaryName, uri));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("无法打开JAR文件: " + packageFolderURL, e);
    }
    return result;
  }

  /**
   * 处理文件系统目录中的类文件
   * 扫描指定目录下的所有.class文件
   *
   * @param packageName 包名
   * @param directory 包对应的文件系统目录
   * @return 目录中所有.class文件的Java文件对象列表
   */
  private List<JavaFileObject> processDir(String packageName, File directory) {
    List<JavaFileObject> result = new ArrayList<JavaFileObject>();

    // 获取目录下的所有文件
    File[] childFiles = directory.listFiles();
    for (File childFile : childFiles) {
      if (childFile.isFile()) {
        // 只处理.class文件
        if (childFile.getName().endsWith(CLASS_FILE_EXTENSION)) {
          // 构建完整的二进制类名：包名 + 文件名（去掉.class扩展名）
          String binaryName = packageName + "." + childFile.getName();
          binaryName = binaryName.replaceAll(CLASS_FILE_EXTENSION + "$", "");

          result.add(new CustomJavaFileObject(binaryName, childFile.toURI()));
        }
      }
    }

    return result;
  }
}
