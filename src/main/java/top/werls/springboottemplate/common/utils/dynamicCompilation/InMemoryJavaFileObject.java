package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.io.IOException;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * @author Li JiaWei
 * @version 1
 * @date 2023/3/31
 * @since on
 */
public class InMemoryJavaFileObject extends SimpleJavaFileObject {

  private String contents = null;

  /**
   * Construct a SimpleJavaFileObject of the given kind and with the given URI.
   *
   * @param uri  the URI for this file object
   * @param kind the kind of this file object
   */
  protected InMemoryJavaFileObject(URI uri, Kind kind) {
    super(uri, kind);
  }

  public InMemoryJavaFileObject(String className, String contents) throws Exception {
    super(URI.create("string:///" + className.replace('.', '/')
        + Kind.SOURCE.extension), Kind.SOURCE);
    this.contents = contents;
  }

  public CharSequence getCharContent(boolean ignoreEncodingErrors)
      throws IOException {
    return contents;
  }
}
