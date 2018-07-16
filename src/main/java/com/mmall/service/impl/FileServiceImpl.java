package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.utils.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

@Service("iFileService")
@Slf4j
public class FileServiceImpl implements IFileService {

    /**
     * springmvc上传的文件的过程
     * 1》先上传到项目发布的服务器的目录中
     * 2》再将本地的生成的文件再去上传到ftp服务器当中
     * 3》最后将本地服务器的文件进行删除
     *
     * @param multipartFile
     * @param path
     * @return
     */
    public String upload(MultipartFile multipartFile, String path) {
        String fileName = multipartFile.getOriginalFilename();
        String flileExtensionFilenName = fileName.substring(fileName.lastIndexOf(".") + 1);
        String uploadFileName = UUID.randomUUID().toString() + "." + flileExtensionFilenName;
        log.info("开始上传文件，上传文件的文件名：{}，上传的路径：{}，新文件名：{}", fileName, path, uploadFileName);
        File fileDir = new File(path);
        //如果没有文件夹那就创建我们要上传的子文件夹
        if (!fileDir.exists()) {
            //先将要设置的文件夹设置为可读的性质
            fileDir.setWritable(true);
            /**
             * File 下面的mkdir和mkdirs的区别
             * mkdir是下面没有子文件夹，而mkdirs创建的是下面具有子文件夹
             */
            fileDir.mkdirs();
        }

        File targetFile = new File(fileDir, uploadFileName);//在fileDir的目录下面生成一个新的文件夹
        try {
            multipartFile.transferTo(targetFile);//使用springmvc框架开始进行文件的上传,上传到项目发布的服务器上面
            //文件已经上传成功
            //todo 将targetFile上传到FTP服务器
            if (FTPUtil.uoloadFile(Lists.newArrayList(targetFile))) {
                log.error("文件上传服务器成功");
            } else {
                log.error("文件上传服务器失败");
            }//建立一个上传到ftp文件的一个工具类
            //todo  上传完之后，删除文upload下面的文件，一般都是存储再tomcat服务器的文件夹下面的
            targetFile.delete();
        } catch (IOException e) {
            log.error("上传文件异常");
            return null;
        }
        return targetFile.getName();

    }

}
