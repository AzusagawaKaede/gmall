package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.FileService;
import com.atguigu.gmall.product.utils.FileUtil;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: rlk
 * @date: 2022/7/29
 * Description:
 */
@Service
public class FileServiceImpl implements FileService {

    @Value("${fileServer.url}")
    private String fileServerUrl;

    /**
     * 文件上传
     * @param file
     * @return
     * @throws Exception
     */
    @Override
    public String fileUpload(MultipartFile file) throws Exception {
        String uploadUrl = FileUtil.upload(file.getBytes(),
                StringUtils.getFilenameExtension(
                        file.getOriginalFilename()));
        String imgUrl = fileServerUrl + uploadUrl;
        //返回文件访问路径
        return imgUrl;
    }

}
