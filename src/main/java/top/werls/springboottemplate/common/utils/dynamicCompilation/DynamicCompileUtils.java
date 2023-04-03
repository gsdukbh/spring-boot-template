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
 * @author Li JiaWei
 * @version TODO
 * @date 2023/3/31
 * @since on
 */
@Component
@Slf4j
public class DynamicCompileUtils {

  private String clazzPath = new File("").getAbsolutePath() + "\\clazz\\";

  private String libsPath;
  private  String mPackagePath = "top.werls.springboottemplate.compiled";

  public DynamicCompileUtils setClazzPath(String clazzPath) {
    this.clazzPath = clazzPath;
    return this;
  }

  public  DynamicCompileUtils setPackagePath(String mPackagePath) {
    this.mPackagePath = mPackagePath;
    return this;
  }


  public DynamicCompileUtils setLibsPath(String libsPath) {
    this.libsPath = libsPath;
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
        Thread.currentThread().getContextClassLoader()).setClazzPath(clazzPath).setPackagePath(mPackagePath);

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
        compiler.getTask(null, standardJavaFileManager, null, options, null,
            javaFiles);
    // 编译

    boolean success = task.call();
    if (success) {
      Class<?> clazz = classLoader.loadClass(classname);
      return clazz.newInstance();
    }
    fileClass.delete();
    fileSource.delete();
    return null;
  }

  private String loadLibs() throws Exception {
    StringBuilder sb = new StringBuilder();
    if (libsPath != null && !libsPath.isEmpty()) {
      File lib = new File(libsPath);
      if (lib.exists() && lib.isDirectory()) {
        for (File f : lib.listFiles()) {
          if (f.getPath().endsWith(".jar")) {
            sb.append(f.getPath()).append(File.pathSeparator);
          }
        }
      }
    }
    log.warn(" sb : {}", sb);
    return sb.toString();
  }
}
