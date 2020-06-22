package com.zhku.mh.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class GmallManageWebApplicationTests {


    @Test
    public void contextLoads() throws IOException, MyException {
        //配置fdfs的全局连接地址
        String tracker = GmallManageWebApplicationTests.class.getResource("/tracker.conf").getPath();

        ClientGlobal.init(tracker);

        TrackerClient trackerClient = new TrackerClient();

        TrackerServer trackerServer = trackerClient.getConnection();

        StorageClient storageClient = new StorageClient(trackerServer, null);

//        String fileName = "C:\\Users\\MH\\Pictures\\QQ图片20170227201028.png";
        String fileName = "C:\\Users\\MH\\Pictures\\704_1540818_523209.jpg";

        String fileNames[] = storageClient.upload_file(fileName,"jpg",null);

        for (String name : fileNames) {
            System.out.println(name);
        }


    }

}
