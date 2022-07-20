package top.werls.springboottemplate.system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.werls.springboottemplate.common.ResultData;

/**
 * @author Jiawei Lee
 * @version TODO
 * @date created 2022/7/20
 * @since on
 */
@RestController
@RequestMapping("/file")
public class FileController {

  public ResultData<String> download() {
    return ResultData.success();
  }
}
