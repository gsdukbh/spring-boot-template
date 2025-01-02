package top.werls.springboottemplate.system.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.werls.springboottemplate.common.ResultData;
import top.werls.springboottemplate.common.file.FileManagers;
import top.werls.springboottemplate.common.utils.MessageUtils;

import java.io.*;

/**
 * @author Jiawei Lee
 * @version TODO
 * @date created 2022/7/20
 * @since on
 */
@RestController
@RequestMapping("/file")
public class FileController {

  private final FileManagers fileManagers;
  private final MessageUtils messageUtils;

  public FileController(FileManagers fileManagers, MessageUtils messageUtils) {
    this.fileManagers = fileManagers;
    this.messageUtils = messageUtils;
  }

  @RequestMapping("/download/{filename}")
  public ResultData<String> download(HttpServletResponse response, @PathVariable String filename)
      throws IOException {
    File file = fileManagers.get(filename);

    if (!file.exists()) {
      return ResultData.fail(messageUtils.getMessage("notfound"));
    }
    response.setContentType("application/octet-stream");
    response.setCharacterEncoding("UTF-8");
    response.setContentLength((int) file.length());
    response.addHeader("Content-Disposition", "attachment;fileName=" + filename);
    OutputStream outputStream = response.getOutputStream();
    FileInputStream fileInputStream = new FileInputStream(file);
    byte[] buff = new byte[1024];
    int len;
    while ((len = fileInputStream.read(buff)) > 0) {
      outputStream.write(buff, 0, len);
    }
    outputStream.close();
    fileInputStream.close();
    response.flushBuffer();
    return ResultData.success();
  }

  @RequestMapping("/upload")
  public ResultData<String> fileUpload(@RequestParam("file") MultipartFile file)
      throws IOException {
    if (!file.isEmpty()) {
      fileManagers.save(file.getInputStream());
    }
    return ResultData.success();
  }
}
