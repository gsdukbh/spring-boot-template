package top.werls.springboottemplate.common.file;

import org.junit.jupiter.api.Test;
import top.werls.springboottemplate.common.file.impl.FileLocal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * date created 2022/7/27
 *
 * @author Jiawei Lee
 * @version TODO
 * @since on
 */
class FileManagersTest {

    @Test
    void get() {
        FileManagers fileManagers = new FileLocal("D:\\Development\\Code\\Github\\spring-boot-template\\log");

        fileManagers.get("a");
    }
}