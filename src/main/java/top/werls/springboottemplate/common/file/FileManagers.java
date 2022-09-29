package top.werls.springboottemplate.common.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件管理
 * @author Jiawei Lee
 * @version TODO
 * @date created 2022/7/20
 * @since on
 */
public interface FileManagers {

  /**
   * 在默认目录（包括子目录）根据文件名查询， 若有多个相同的文件名， 则返回第一个。
   *
   * @param filename 文件名
   * @return file 对象 {@link  File} 文件
   */

  File get(String filename);

  /**
   * 自动添加 默认路径前缀
   * @param path 文件路径
   * @return file {@link File}
   */
  File getByPath(String path) throws FileNotFoundException;

  /**
   * 根据文件名 从默认目录下 指定目录(桶),(包括子目录) 获取文件，若有多个相同的文件名， 则返回第一个。
   *
   * @param filename 文件名
   * @param path 目录 桶
   * @return file {@link File}
   */
  File get(String filename, String path) throws FileNotFoundException;
  /**
   * 从指定目录(桶)获取 输入流 方便minio
   *
   * @param filename 文件名
   * @param path 目录/ 桶
   * @return InputStream {@link InputStream}
   */
  InputStream getInputStream ( String filename, String path);
  /**
   * 从指定目录(桶)获取 输入流 方便minio
   *
   * @param filename 文件名
   * @return InputStream {@link InputStream}
   */
  InputStream getInputStream ( String filename);

  /**
   * 保存文件
   * @param file file {@link File}
   */
  void save(InputStream file);

  /**
   * 保存文件
   * @param file  file file {@link File}
   * @param path 指定目录,or 桶
   */
  void save(InputStream file, String path);

  /**
   * 删除文件
   * @param filename 文件名
   */
  void delete(String  filename);

  /**
   *删除文件
   * @param filename  文件名
   * @param path 指定目录 or 桶
   */
  void delete(String  filename, String path);
}
