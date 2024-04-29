package top.werls.springboottemplate.common.file;

import org.junit.jupiter.api.Test;
import top.werls.springboottemplate.common.file.impl.FileLocal;

import java.io.File;
import java.io.FileNotFoundException;
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

//  @Test
  void get() {
    FileManagers fileManagers =
        new FileLocal("D:\\Development\\Code\\Github\\spring-boot-template\\log");

   var file =  fileManagers.get("spring boot template-2022-07-20-0.log");

    System.out.println(file.length());
    System.out.println(file.getPath());
  }
//  @Test
  void  getByPath(){
      try {
          FileManagers fileManagers =
                  new FileLocal("D:\\Development\\Code\\Github\\spring-boot-template");
          var file= fileManagers.getByPath("/log/info/spring boot template-2022-07-20-0.log");
      System.out.println(file.getName());
      System.out.println(file.getPath());
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }

  }
}
