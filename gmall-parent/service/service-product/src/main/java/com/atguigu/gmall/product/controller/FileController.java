package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.FileService;
import com.atguigu.gmall.product.utils.FileUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author: rlk
 * @date: 2022/7/29
 * Description:
 */
@RestController
@RequestMapping("/admin/product")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 文件上传
     *
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/fileUpload")
    public Result fileUpload(@RequestParam MultipartFile file) throws Exception {
        String imgUrl = fileService.fileUpload(file);
        return Result.ok(imgUrl);
    }

    /**
     * 文件下载到指定路径
     * @param groupName
     * @param remoteName
     * @param path
     * @return
     * @throws Exception
     */
    @GetMapping("/download2Local")
    public Result download(String groupName, String remoteName, String path) throws Exception {
        FileUtil.download2Local(groupName, remoteName, path);
        return Result.ok();
    }
}
