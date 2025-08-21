package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * 动态编译工具类
 * 提供从字符串源码动态编译Java类的功能，支持运行时编译和加载Java代码
 *
 * <p>注意：类加载器无法加载lib中jar包，所以需要自定义类加载器，这里没有实现加载lib中jar包</p>
 *
 * <p>安全性：内置代码安全检查机制，禁止使用危险的Java API（如反射、文件操作、网络操作等）</p>
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2023/3/31
 */
@Slf4j
public class DynamicCompileUtils {

  /** 编译后的class文件存储路径 */
  private String clazzPath = new File("").getAbsolutePath() + "/clazz/";

  /** 外部库文件路径，从配置文件读取 */
  @Value("${env.libs:}")
  private String mPath = "";

  /**
   * 默认构造函数
   */
  public DynamicCompileUtils() {
  }

  /**
   * 设置class文件存储路径
   *
   * @param clazzPath class文件存储的目录路径
   * @return 返回当前实例，支持链式调用
   */
  public DynamicCompileUtils setClazzPath(String clazzPath) {
    this.clazzPath = clazzPath;
    return this;
  }

  /**
   * 从字符串中动态编译java文件（安全增强版本）
   * 集成完整的安全检查体系，包括源码检查、资源控制和字节码验证
   *
   * <p>示例代码：</p>
   * <pre><code>
   * package com.example.demo.me.dynamicCompilation;
   *   public class HelloWorld {
   *        public static void main(String[] args){
   *          System.out.println("Hello World!");
   *            }
   *       }
   *  </code></pre>
   *
   * @param code      Java源代码字符串
   * @param classname 完整的类名，如：com.example.demo.me.dynamicCompilation.HelloWorld
   * @return 编译成功后创建的类实例对象，编译失败或安全检查不通过时返回null
   * @throws Exception 编译过程中可能抛出的异常
   */
  public Object compile(String code, String classname) throws Exception {
    // 使用安全管理器进行全面的安全编译
    return DynamicCompilationSecurityManager.secureCompile(code, classname, this);
  }

  /**
   * 内部编译方法
   * 实际执行编译逻辑，由安全管理器调用
   *
   * @param code      Java源代码字符串
   * @param classname 完整的类名
   * @return 编译成功后创建的类实例对象，编译失败或安全检查不通过时返回null
   * @throws Exception 编译过程中可能抛出的异常
   */
  public Object internalCompile(String code, String classname) throws Exception {

    // 输入验证
    if (code == null || code.trim().isEmpty()) {
      log.warn("代码为空或null，无法编译");
      return null;
    }

    if (classname == null || classname.trim().isEmpty()) {
      log.warn("类名为空或null，无法编译");
      return null;
    }

    // 代码安全检查，防止恶意代码执行
    if (codeSecurityCheck(code)) {
      return null;
    }

    // 构建源文件和class文件的路径
    String SourcePath = clazzPath + classname.replace(".", "/") + Kind.SOURCE.extension;
    String ClassPath = clazzPath + classname.replace(".", "/") + Kind.CLASS.extension;
    File fileSource = new File(SourcePath);
    File fileClass = new File(ClassPath);

    // 创建包目录结构
    int lasted = classname.lastIndexOf(".");
    if (lasted > 0) {
      File fileDir = new File(clazzPath + classname.substring(0, lasted).replace(".", "/"));
      if (!fileDir.exists()) {
        boolean created = fileDir.mkdirs();
        if (!created) {
          log.warn("无法创建目录: {}", fileDir.getPath());
          return null;
        }
      }
    }

    try {
      // 将源代码写入临时java文件
      boolean created = fileSource.createNewFile();
      if (!created && !fileSource.exists()) {
        log.warn("无法创建源文件: {}", fileSource.getPath());
        return null;
      }

      try (FileOutputStream outputStream = new FileOutputStream(fileSource)) {
        outputStream.write(code.getBytes(StandardCharsets.UTF_8));
      }

      // 获取系统编译器实例
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      if (compiler == null) {
        log.error("无法获取系统Java编译器");
        cleanupFiles(fileSource);
        return null;
      }

      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

      // 创建自定义类加载器
      ClassLoader classLoader = new CustomClassLoader(
          Thread.currentThread().getContextClassLoader()).setClazzPath(clazzPath)
          .setPackagePath(classname.substring(0, lasted));

      // 创建文件管理器
      StandardJavaFileManager standardJavaFileManager =
          compiler.getStandardFileManager(diagnostics, null, null);

      JavaFileManager javaFileManager = new CustomClassloaderJavaFileManager(classLoader,
          standardJavaFileManager);
      var javaFiles = standardJavaFileManager.getJavaFileObjectsFromFiles(List.of(fileSource));

      // 配置编译选项，包括classpath设置
      List<String> options = new ArrayList<>();
      options.add("-classpath");
      options.add(
          System.getProperty("java.class.path")
              + File.pathSeparator
              + "classes/InlineCompiler.jar;"
              + clazzPath
              + File.pathSeparator + loadLibs()
      );

      // 创建编译任务
      JavaCompiler.CompilationTask task =
          compiler.getTask(null, javaFileManager, diagnostics, options, null,
              javaFiles);

      // 执行编译
      boolean success = task.call();

      if (success) {
        // 编译成功，加载类并创建实例
        try {
          Class<?> clazz = classLoader.loadClass(classname);

          // 手动字节码验证（作为备用验证）
          if (!DynamicCompilationSecurityManager.isBytecodeVerificationEnabled()) {
            try {
              byte[] bytecode = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(ClassPath));
              if (!DynamicCompilationSecurityManager.manualBytecodeVerification(bytecode, classname)) {
                log.error("字节码安全验证失败: {}", classname);
                cleanupFiles(fileClass, fileSource);
                return null;
              }
            } catch (Exception e) {
              log.warn("无法进行手动字节码验证: {}", e.getMessage());
            }
          }

          cleanupFiles(fileClass, fileSource);
          return clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
          log.error("编译成功但无法加载类: {}", classname, e);
          cleanupFiles(fileClass, fileSource);
          return null;
        }
      } else {
        // 编译失败，记录错误信息
        log.error("编译失败，错误信息：{}", diagnostics.getDiagnostics());
      }

    } catch (Exception e) {
      log.error("编译过程发生异常: {}", e.getMessage(), e);
    } finally {
      // 清理临时文件
      cleanupFiles(fileClass, fileSource);
    }

    return null;
  }

  /**
   * 清理临时文件
   */
  private void cleanupFiles(File... files) {
    for (File file : files) {
      if (file.exists()) {
        boolean deleted = file.delete();
        if (!deleted) {
          log.warn("无法删除临时文件: {}", file.getPath());
        }
      }
    }
  }

  /**
   * 代码安全检查
   * 使用增强的安全检查器，提供多层次的安全验证
   *
   * @param code 待检查的Java源代码
   * @return 如果代码包含危险操作返回true，否则返回false
   */
  private boolean codeSecurityCheck(String code) {
    // 首先使用增强的安全检查器
    SecurityChecker.SecurityCheckResult result = SecurityChecker.checkCode(code);

    if (!result.isSafe()) {
      log.warn("代码安全检查失败，违规项: {}", result.getViolations());
      return true;
    }

    // 额外的基础检查（保留原有逻辑作为双重保险）
    return containsDangerousContent(code);
  }

  /**
   * 检查代码是否包含危险内容
   */
  private boolean containsDangerousContent(String code) {
    // 禁止使用反射
    if (code.contains("java.lang.reflect")) return true;
    // 禁止使用文件操作
    if (code.contains("java.io")) return true;
    // 禁止使用线程操作
    if (code.contains("java.lang.Thread")) return true;
    // 禁止使用系统类
    if (code.contains("java.lang.System") || code.contains("System.")) return true;
    // 禁止使用java.lang.ClassLoader类
    if (code.contains("java.lang.ClassLoader")) return true;
    // 禁止使用java.lang.Runtime类
    if (code.contains("java.lang.Runtime")) return true;
    // 禁止使用java.lang.Process类
    if (code.contains("java.lang.Process")) return true;
    // 禁止使用java.lang.Compiler类
    if (code.contains("java.lang.Compiler")) return true;
    // 禁止使用java.lang.Class类
    if (code.contains("java.lang.Class")) return true;
    // 禁止使用java.net包类（网络操作）
    if (code.contains("java.net")) return true;
    // 禁止使用JDI（Java调试接口）
    if (code.contains("com.sun.jdi")) return true;
    // 禁止使用JDK内置HTTP服务器
    if (code.contains("com.sun.net")) return true;
    // 禁止使用Java编译器工具
    if (code.contains("javax.tools")) return true;
    // 禁止使用Unsafe类进行内存操作
    if (code.contains("sun.misc.Unsafe")) return true;
    // 禁止使用方法句柄和动态调用
    if (code.contains("java.lang.invoke")) return true;

    return false;
  }

  /**
   * 加载外部库文件
   * 扫描指定目录下的所有jar文件，构建classpath字符串
   *
   * @return 包含所有jar文件路径的classpath字符串
   */
  private String loadLibs() {
    StringBuilder sb = new StringBuilder();
    File lib = new File(mPath);

    // 检查库目录是否存��
    if (lib.exists() && lib.isDirectory()) {
      // 遍历目录下的所有文件
      File[] files = lib.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.getPath().endsWith(".jar")) {
            // 将jar文件路径添加到classpath中
            sb.append(f.getPath()).append(File.pathSeparator);
          }
        }
      }
    }
    log.debug("加载的库文件路径：{}", sb);
    return sb.toString();
  }
}
