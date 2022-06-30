package top.werls.springboottemplate.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.werls.springboottemplate.common.ResultData;

/**
 * @author Jiawei Lee
 * @version TODO
 * @since on  2022/6/30
 */
@RestController("/demo")
public class DemoController {

    @GetMapping("/i")
    public ResultData<String> demo(){
        return ResultData.success("this is  demo mod");
    }

}
