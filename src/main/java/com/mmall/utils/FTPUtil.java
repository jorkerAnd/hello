package com.mmall.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import sun.net.ftp.FtpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * 只对外开放一个接口方法
 */
@Data
@Slf4j
public class FTPUtil {
    private static String ftpIp = PropertiesUtils.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtils.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtils.getProperty("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    public static boolean uoloadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPass);
        log.info("开始连接服务器");
        boolean result = ftpUtil.uploadFile("image", fileList);
        log.info("结束上传，");
        return result;
    }

    /**
     *需要注意的是，当
     * @param remotePath
     * @param fileList
     * @return
     * @throws IOException
     */

    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                for (File fileItem : fileList) {
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(), fis);
                }
            } catch (IOException e) {
                e.printStackTrace();
                uploaded = false;
            } finally {
                fis.close();
                ftpClient.disconnect();
            }
        }else{
            log.error("ftp服务器连接失败");
            uploaded=false;
        }
        return uploaded;
    }

    /**
     * @param ip
     * @param port
     * @param user
     * @param pwd
     * @return
     */
    private boolean connectServer(String ip, int port, String user, String pwd) {
        //isSuccess必须声明在外部
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            log.error("连接FTP服务器异常", e);
            e.printStackTrace();
        }
        return isSuccess;
    }


}