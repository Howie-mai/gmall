package com.zhku.mh.gmall.web.util;

import com.zhku.mh.common.constant.FastDFSConstant;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName：
 * Time：2019/12/26 19:40
 * Description：
 * Author： mh
 */
public class PmsUploadUtil {

    public static String upload(MultipartFile multipartFile){

        String url = FastDFSConstant.DOMAIN;

        //配置fdfs的全局连接地址
        String tracker = PmsUploadUtil.class.getResource("/tracker.conf").getPath();

        try {
            ClientGlobal.init(tracker);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);

            byte[] bytes = multipartFile.getBytes();

            //获取后缀名
            String originalFilename = multipartFile.getOriginalFilename();
            int index = originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(index+1);

            String fileNames[] = storageClient.upload_file(bytes,extName,null);

            for (String name : fileNames) {
                url += "/" + name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return url;
    }
}
