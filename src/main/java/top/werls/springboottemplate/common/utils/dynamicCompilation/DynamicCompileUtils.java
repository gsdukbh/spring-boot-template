package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * 类加载器无法加载 lib中jar包，所以需要自定义类加载器，这里没有实现加载lib中jar包
 * @author Li JiaWei
 * @version TODO
 * @date 2023/3/31
 * @since on
 */
@Component
@Slf4j
public class DynamicCompileUtils {

  private String clazzPath = new File("").getAbsolutePath() + "\\clazz\\";
  @Value("${libs}")
  private String mPath;

  public DynamicCompileUtils(){}


  public DynamicCompileUtils setClazzPath(String clazzPath) {
    this.clazzPath = clazzPath;
    return this;
  }

  /**
   * 从字符串中动态编译java文件
   * <p>example : </p>
   * <pre><code>
   * package com.example.demo.me.dynamicCompilation;
   *   public class HelloWorld {
   *        public static void main(String[] args){
   *          System.out.println("Hello World!");
   *            }
   *       }
   *  </code></pre>
   *
   * @param code      源代码
   * @param classname com.example.demo.me.dynamicCompilation.HelloWorld
   * @return object
   */
  public Object compile(String code, String classname) throws Exception {

    if ( codeSecurityCheck(code)) {
      return  null;
    }

    String SourcePath = clazzPath + classname.replace(".", "\\") + Kind.SOURCE.extension;
    String ClassPath = clazzPath + classname.replace(".", "\\") + Kind.CLASS.extension;
    File fileSource = new File(SourcePath);
    File fileClass = new File(ClassPath);
    int lasted = classname.lastIndexOf(".");
    if (lasted > 0) {
      File fileDir = new File(clazzPath + classname.substring(0, lasted).replace(".", "\\"));
      if (!fileDir.exists()) {
        fileDir.mkdirs();
      }
    }
    fileSource.createNewFile();
    FileOutputStream outputStream = new FileOutputStream(fileSource);
    outputStream.write(code.getBytes(StandardCharsets.UTF_8));
    outputStream.close();

    // 获取系统编译器实例
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    ClassLoader classLoader = new CustomClassLoader(
        Thread.currentThread().getContextClassLoader()).setClazzPath(clazzPath)
        .setPackagePath(classname.substring(0, lasted));

    StandardJavaFileManager standardJavaFileManager =
        compiler.getStandardFileManager(diagnostics, null, null);

    final JavaFileManager javaFileManager = new CustomClassloaderJavaFileManager(classLoader,
        standardJavaFileManager);
    var javaFiles = standardJavaFileManager.getJavaFileObjectsFromFiles(List.of(fileSource));

    // bind the custom file manager to the compiler
    List<String> options = new ArrayList<>();
    options.add("-classpath");
    options.add(
        System.getProperty("java.class.path")
            + File.pathSeparator
            + "classes/InlineCompiler.jar;"
            + clazzPath
            + File.pathSeparator + loadLibs()
    );
    JavaCompiler.CompilationTask task =
        compiler.getTask(null, standardJavaFileManager, diagnostics, options, null,
            javaFiles);
    // 编译

    boolean success = task.call();

    if (success) {
      Class<?> clazz = classLoader.loadClass(classname);
      return clazz.getDeclaredConstructor().newInstance();
    } else {
      log.error("  cjuowuo  {} ", diagnostics.getDiagnostics());
    }
    fileClass.delete();
    fileSource.delete();
    return null;
  }

  private boolean codeSecurityCheck(String code) {
    // 检查代码是否有安全隐患
    // 禁止使用网络类的方法
// 禁止使用反射
    boolean res = code.contains("java.lang.reflect");
// 禁止使用文件操作
    res = res || code.contains("java.io");
// 禁止使用线程操作
    res = res || code.contains("java.lang.Thread");
// 禁止使用系统类
    res = res || code.contains("java.lang.System");
    res = res || code.contains("System.");
// 禁止使用java.lang.ClassLoader类
    res = res || code.contains("java.lang.ClassLoader");
// 禁止使用java.lang.Runtime类
    res = res || code.contains("java.lang.Runtime");
// 禁止使用java.lang.Process类
    res = res || code.contains("java.lang.Process");
// 禁止使用java.lang.Compiler类
    res = res || code.contains("java.lang.Compiler");
// 禁止使用java.lang.Class类
    res = res || code.contains("java.lang.Class");
// 禁止使用java.net 包类
    res = res || code.contains("java.net");
    // 禁止 jdi
    res = res || code.contains("com.sun.jdi");
    // 禁止 jdk.httpserver
    res = res || code.contains("com.sun.net");
    // 禁止 java.compiler
    res = res || code.contains("javax.tools");
    // 禁止内存操作
    res = res || code.contains("sun.misc.Unsafe");
    // 禁止使用 java.lang.invoke
    res = res || code.contains("java.lang.invoke");
    return res;
  }

  private String loadLibs() throws Exception {
    StringBuilder sb = new StringBuilder();
    File lib = new File(mPath);
    if (lib.exists() && lib.isDirectory()) {
      for (File f : lib.listFiles()) {
        if (f.getPath().endsWith(".jar")) {
          sb.append(f.getPath()).append(File.pathSeparator);
        }
      }
    }
    log.warn(" sb : {}", sb);
    return sb.toString();
  }
}