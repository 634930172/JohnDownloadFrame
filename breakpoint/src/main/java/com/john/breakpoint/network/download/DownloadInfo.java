package com.john.breakpoint.network.download;


/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/10/14 10:27
 * <p/>
 * Description:
 */
public class DownloadInfo {


    /**
     * 下载的url
     */
    private String downloadUrl;

    /**
     * 存储的目录路径
     */
    private String savePathDir;

    /**
     * 文件名
     */
    private String fileName;


    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getSavePathDir() {
        return savePathDir;
    }

    public void setSavePathDir(String savePathDir) {
        this.savePathDir = savePathDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
