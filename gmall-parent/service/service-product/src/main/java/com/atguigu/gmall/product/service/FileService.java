package com.atguigu.gmall.product.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author: rlk
 * @date: 2022/7/29
 * Description:
 */
public interface FileService {

    /**
     * 文件上传
     * @param file
     * @return
     */
    String fileUpload(MultipartFile file) throws Exception;
}
