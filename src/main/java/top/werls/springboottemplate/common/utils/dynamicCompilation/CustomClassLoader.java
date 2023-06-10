package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Li JiaWei
 * @version 1
 * @date 2023/3/31
 * @since on
 */
@Slf4j
public class CustomClassLoader extends ClassLoader{

  public CustomClassLoader(ClassLoader parent) {
    super(parent);
  }
  private  String mPackagePath = "top.werls.springboottemplate.compiled";

  public CustomClassLoader setPackagePath(String packagePath) {
    mPackagePath = packagePath;
    return this;
  }
  private  String clazzPath =new File("").getAbsolutePath()+"\\clazz\\";

  public CustomClassLoader setClazzPath(String clazzPath) {
    this.clazzPath = clazzPath;
    return this;
  }

  @Override
  public Class<?> findClass(String name) throws ClassNotFoundException {
    // 从指定路径读取加密后的class文件
    String path = clazzPath + name.replace(".", "\\") + ".class";
    byte[] data = new byte[0];
    try {
      data = Files.readAllBytes(Paths.get(path));
    } catch (IOException e) {
      throw new ClassNotFoundException("CustomClassLoader findClass error", e);
    }
    // 返回解密后的class对象
    return defineClass(name, data, 0, data.length);
  }
  //重写 loadClass () 方法，这个方法负责加载类
  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    //如果类已经被加载过，就直接返回
    if (findLoadedClass (name) != null ) {
      return findLoadedClass (name);
    }
    //如果类的全限定名以 开头，就用自己的 findClass () 方法来加载
    if (name.startsWith (mPackagePath)) {
      return findClass (name);
    }
    //否则，就委托给父类加载器尝试加载
    return super.loadClass (name);
  }
}
