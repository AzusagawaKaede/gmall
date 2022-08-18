package com.atguigu.gmall.product.utils;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;

import java.io.FileOutputStream;
import java.util.UUID;

/**
 * @author: rlk
 * @date: 2022/7/29
 * Description:
 */
public class FileUtil {

    //只加载一次，所以直接放在静态代码块中
    static {
        try {
            //加载配置文件
            ClassPathResource resource = new ClassPathResource("fastdfs.properties");
            //初始化fastdfs
            ClientGlobal.init(resource.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @param bytes
     * @param extName
     * @return
     * @throws Exception
     */
    public static String upload(byte[] bytes, String extName) throws Exception {
        /*
         * 文件上传
         * 第一个参数： 文件的byte数组
         * 第二个参数： 文件的扩展名
         * 第三个参数： 附加参数，例如图片上加水印，加日期等
         * 返回String数组： 数组中有两个元素，第一个元素是文件在fastdfs的哪个分组上，第二个元素就是文件的路径
         */
        String[] strings = getStorageClient().upload_file(bytes, extName, null);
        return strings[0] + "/" + strings[1];
    }

    /**
     * 文件下载
     * @param groupName 文件在fastdfs的哪个分组
     * @param remoteName 文件在分组里的地址（文件夹位置）
     * @return 返回文件的字节数组
     * @throws Exception
     */
    public static byte[] download(String groupName, String remoteName) throws Exception{
        byte[] bytes = getStorageClient().download_file(groupName, remoteName);
        return bytes;
    }

    /**
     * 将文件下载到指定的path
     * @param groupName
     * @param remoteName
     * @param path
     * @throws Exception
     */
    public static void download2Local(String groupName, String remoteName, String path) throws Exception{
        byte[] bytes = getStorageClient().download_file(groupName, remoteName);
        String suffix = remoteName.substring(remoteName.lastIndexOf("."));
        FileOutputStream fos = new FileOutputStream(
                path + UUID.randomUUID().toString().replace("-", "")
                        + suffix);
        fos.write(bytes);
        fos.close();
    }

    /**
     * 文件的删除
     * @param groupName
     * @param remoteName
     * @return 文件删除成功，则delete_file方法返回 0 ，返回其他值均为删除失败
     * @throws Exception
     */
    public static boolean delete(String groupName, String remoteName) throws Exception{
        int rs = getStorageClient().delete_file(groupName, remoteName);
        return rs == 0;
    }

    /**
     * 获取storageClient
     * @return
     * @throws Exception
     */
    private static StorageClient getStorageClient() throws Exception {
        //初始化tracker
        TrackerClient trackerClient = new TrackerClient();
        //获取一个trackerServer连接
        TrackerServer trackerServer = trackerClient.getConnection();
        //初始化storage
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return storageClient;
    }

}