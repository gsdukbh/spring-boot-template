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
 * @author Li JiaWei
 * @version 1
 * @date 2023/3/31
 * @since on
 */
public class CustomClassloaderJavaFileManager implements JavaFileManager {

  private final ClassLoader classLoader;
  private final StandardJavaFileManager standardFileManager;
  private final PackageInternalsFinder finder;

  public CustomClassloaderJavaFileManager(ClassLoader classLoader,
      StandardJavaFileManager standardFileManager) {
    this.classLoader = classLoader;
    this.standardFileManager = standardFileManager;
    finder = new PackageInternalsFinder(classLoader);
  }


  @Override
  public ClassLoader getClassLoader(Location location) {
    return classLoader;
  }

  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    if (file instanceof CustomJavaFileObject) {
      return ((CustomJavaFileObject) file).binaryName();
    } else { // if it's not CustomJavaFileObject, then it's coming from standard file manager - let it handle the file
      return standardFileManager.inferBinaryName(location, file);
    }
  }

  @Override
  public boolean isSameFile(FileObject a, FileObject b) {
    return standardFileManager.isSameFile(a,b);
  }

  @Override
  public boolean handleOption(String current, Iterator<String> remaining) {
//    throw new UnsupportedOperationException();

    return standardFileManager.handleOption(current,remaining);
  }

  @Override
  public boolean hasLocation(Location location) {
    return location == StandardLocation.CLASS_PATH || location
        == StandardLocation.PLATFORM_CLASS_PATH;
    // we don't care about source and other location types - not needed for compilation
  }

  @Override
  public JavaFileObject getJavaFileForInput(Location location, String className,
      Kind kind) throws IOException {
    // Delegate to the Default Manager
    return  standardFileManager.getJavaFileForInput(location,className,kind);

  }
  @Override
  public Location getLocationForModule(Location location, String moduleName) throws IOException {
    return standardFileManager.getLocationForModule(location, moduleName);
  }

  @Override
  public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
    return standardFileManager.getLocationForModule(location, fo);
  }

  @Override
  public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
    return standardFileManager.listLocationsForModules(location);
  }

  @Override
  public String inferModuleName(Location location) throws IOException {
    return standardFileManager.inferModuleName(location);
  }
  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className,
      Kind kind, FileObject sibling) throws IOException {
    return  standardFileManager.getJavaFileForOutput(location,className,kind,sibling);

  }

  @Override
  public FileObject getFileForInput(Location location, String packageName, String relativeName)
      throws IOException {
    return  standardFileManager.getFileForInput(location,packageName,relativeName);
  }

  @Override
  public FileObject getFileForOutput(Location location, String packageName, String relativeName,
      FileObject sibling) throws IOException {
    return  standardFileManager.getFileForOutput(location,packageName,relativeName,sibling);
  }

  @Override
  public void flush() throws IOException {
    // do nothing
  }

  @Override
  public void close() throws IOException {
    // do nothing

  }

  @Override
  public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds,
      boolean recurse) throws IOException {
//    if (location == StandardLocation.PLATFORM_CLASS_PATH) { // let standard manager hanfle
//      return standardFileManager.list(location, packageName, kinds, recurse);
//    } else if (location == StandardLocation.CLASS_PATH && kinds.contains(
//        JavaFileObject.Kind.CLASS)) {
//      if (packageName.startsWith(
//          "java")) { // a hack to let standard manager handle locations like "java.lang" or "java.util". Prob would make sense to join results of standard manager with those of my finder here
//        return standardFileManager.list(location, packageName, kinds, recurse);
//      } else { // app specific classes are here
//        return finder.find(packageName);
//      }
//    }
//    return Collections.emptyList();
    boolean baseModule = location.getName().equals("SYSTEM_MODULES[java.base]");
    if (baseModule || location == StandardLocation.PLATFORM_CLASS_PATH) {
      // **MODIFICATION CHECK FOR BASE MODULE**
      return standardFileManager.list(location, packageName, kinds, recurse);
    } else if (location == StandardLocation.CLASS_PATH && kinds.contains(Kind.CLASS)) {
      if (packageName.startsWith("java") || packageName.startsWith("com.sun")) {
        return standardFileManager.list(location, packageName, kinds, recurse);
      } else {
        // app specific classes are here
        return finder.find(packageName);
      }
    }
    return Collections.emptyList();

  }

  @Override
  public int isSupportedOption(String option) {
    return -1;
  }

}
