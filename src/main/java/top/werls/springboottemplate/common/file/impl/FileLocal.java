package top.werls.springboottemplate.common.file.impl;

import top.werls.springboottemplate.common.file.FileManagers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 本地目录实现
 *
 * @author Jiawei Lee
 * @version TODO
 * @date created 2022/7/20
 * @since on
 */
public class FileLocal implements FileManagers {
  private final String filePath;

  public FileLocal(String filePath) {
    this.filePath = filePath;
  }

  /**
   * 获取文件
   *
   * @param filename 文件名
   * @return file 对象 {@link File} 没有文件时返回null
   */
  @Override
  public File get(String filename) {
    File file = new File(filePath);
    List<File> list = new ArrayList<>();
    findPathFile(file, list);
    return list.stream()
        .filter(file1 -> file1.getName().equalsIgnoreCase(filename))
        .findFirst()
        .orElse(null);
  }

  @Override
  public File getByPath(String path) throws FileNotFoundException {
    if (path.startsWith("/") || path.startsWith("\\")) {
      return new File(filePath + path);
    } else {
      throw new FileNotFoundException("file must start with / or \\");
    }
  }

  private void findPathFile(File filename, List<File> list) {
    if (filename.isDirectory()) {
      var dirFiles = filename.listFiles();
      if (dirFiles != null) {
        for (var i : dirFiles) {
          findPathFile(i, list);
        }
      }
    } else {
      list.add(filename);
    }
  }

  /**
   * 从指定目录(桶)获取文件
   *
   * @param filename 文件名
   * @param path 目录 桶
   * @return file {@link File} 没有文件时返回null
   */
  @Override
  public File get(String filename, String path) throws FileNotFoundException {
    if (!path.startsWith("/") && !path.startsWith("\\")) {
      throw new FileNotFoundException("file must start with / or \\");
    }
    File file = new File(filePath + path);
    List<File> list = new ArrayList<>();
    findPathFile(file, list);
    return list.stream()
        .filter(file1 -> file1.getName().equalsIgnoreCase(filename))
        .findFirst()
        .orElse(null);
  }

  /**
   * 从指定目录(桶)获取 输入流 方便minio
   *
   * @param filename 文件名
   * @param path 目录/ 桶
   * @return InputStream {@link InputStream}
   */
  @Override
  public InputStream getInputStream(String filename, String path) {
    try {
      File file = get(filename, path);
      if (file != null && file.exists()) {
        return new FileInputStream(file);
      }
    } catch (FileNotFoundException e) {
      return null;
    }
    return null;
  }

  /**
   * 从指定目录(桶)获取 输入流 方便minio
   *
   * @param filename 文件名
   * @return InputStream {@link InputStream}
   */
  @Override
  public InputStream getInputStream(String filename) {
    File file = get(filename);
    if (file != null && file.exists()) {
      try {
        return new FileInputStream(file);
      } catch (FileNotFoundException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * 保存文件
   *
   * @param file     InputStream
   * @param filename 文件名
   */
  @Override
  public void save(InputStream file, String filename) {
    save(file, "/", filename);
  }

  /**
   * 保存文件
   *
   * @param file     InputStream
   * @param path     指定目录,or 桶
   * @param filename 文件名
   */
  @Override
  public void save(InputStream file, String path, String filename) {
    try {
      Path dirPath = Paths.get(filePath, path);
      if (!Files.exists(dirPath)) {
        Files.createDirectories(dirPath);
      }
      Path targetPath = dirPath.resolve(filename);
      Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Save file error", e);
    }
  }

  /**
   * 删除文件
   *
   * @param filename 文件名
   */
  @Override
  public void delete(String filename) {
    File file = get(filename);
    if (file != null && file.exists()) {
      file.delete();
    }
  }

  /**
   * 删除文件
   *
   * @param filename 文件名
   * @param path     指定目录 or 桶
   */
  @Override
  public void delete(String filename, String path) {
    try {
      File file = get(filename, path);
      if (file != null && file.exists()) {
        file.delete();
      }
    } catch (FileNotFoundException ignored) {
    }
  }
}
