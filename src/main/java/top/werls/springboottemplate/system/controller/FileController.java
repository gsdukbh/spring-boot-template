package top.werls.springboottemplate.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.werls.springboottemplate.common.ResultData;
import top.werls.springboottemplate.common.file.FileManagers;
import top.werls.springboottemplate.common.utils.MessageUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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

  @Resource private FileManagers fileManagers;
  @Resource private MessageUtils messageUtils;

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
}
