package top.werls.springboottemplate.common.file;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.werls.springboottemplate.common.file.impl.FileLocal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * date created 2022/7/27
 *
 * @author Jiawei Lee
 * @version TODO
 * @since on
 */
class FileManagersTest {

  private String testPath = "./test-files";
  private FileManagers fileManagers;

  @BeforeEach
  void setUp() throws Exception {
    Path p = Path.of(testPath);
    if (!Files.exists(p)) {
      Files.createDirectories(p);
    }
    fileManagers = new FileLocal(testPath);
  }

  @AfterEach
  void tearDown() throws Exception {
    // 清理测试目录
    deleteDir(new File(testPath));
  }

  private void deleteDir(File dir) {
    File[] files = dir.listFiles();
    if (files != null) {
      for (File f : files) {
        if (f.isDirectory()) {
          deleteDir(f);
        } else {
          f.delete();
        }
      }
    }
    dir.delete();
  }

  @Test
  void testSaveAndGet() throws Exception {
    String content = "hello world";
    String filename = "test.txt";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

    fileManagers.save(inputStream, filename);

    File file = fileManagers.get(filename);
    assertNotNull(file);
    assertTrue(file.exists());
    assertEquals(filename, file.getName());

    InputStream is = fileManagers.getInputStream(filename);
    assertNotNull(is);
    String readContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
    assertEquals(content, readContent);
    is.close();
  }

  @Test
  void testSaveInPath() throws Exception {
    String content = "sub folder content";
    String filename = "sub.txt";
    String path = "/sub";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

    fileManagers.save(inputStream, path, filename);

    File file = fileManagers.get(filename, path);
    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.getPath().contains("sub"));

    InputStream is = fileManagers.getInputStream(filename, path);
    assertNotNull(is);
    assertEquals(content, new String(is.readAllBytes(), StandardCharsets.UTF_8));
    is.close();
  }

  @Test
  void testDelete() throws Exception {
    String filename = "to-be-deleted.txt";
    fileManagers.save(new ByteArrayInputStream("data".getBytes()), filename);
    assertNotNull(fileManagers.get(filename));

    fileManagers.delete(filename);
    assertNull(fileManagers.get(filename));
  }
}
