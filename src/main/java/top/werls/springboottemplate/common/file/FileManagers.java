package top.werls.springboottemplate.common.file;

import java.io.File;
import java.io.InputStream;

/**
 * 文件管理
 * @author Jiawei Lee
 * @version TODO
 * @date created 2022/7/20
 * @since on
 */
public interface FileManagers {

  /**
   * 获取文件
   *
   * @param filename 文件名
   * @return file 对象 {@link  File}
   */

  File get(String filename);

  /**
   * 从指定目录(桶)获取文件
   *
   * @param filename 文件名
   * @param path 目录 桶
   * @return file {@link File}
   */
  File get(String filename, String path);
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
  void save(File file);

  /**
   * 保存文件
   * @param file  file file {@link File}
   * @param path 指定目录,or 桶
   */
  void save(File file, String path);

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
