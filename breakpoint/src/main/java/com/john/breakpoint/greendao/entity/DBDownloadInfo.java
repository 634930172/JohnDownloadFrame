package com.john.breakpoint.greendao.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/9/27 15:27
 * <p/>
 * Description:数据库里下载的信息表
 */
@Entity(nameInDb = "download_info")
public class DBDownloadInfo {

    /**
     * 下载的id
     */
    @Id(autoincrement = true)
    @Property(nameInDb = "download_id")
    private Long downloadId;

    /**
     * 下载的url
     */
    @Property(nameInDb = "download_url")
    private String downloadUrl;

    /**
     * 已经读取的字节数
     */
    @Property(nameInDb = "read_length")
    private long readLength;

    /**
     * 存储的目录路径
     */
    @Property(nameInDb = "save_path_dir")
    private String savePathDir;

    /**
     * 文件名
     */
    @Property(nameInDb = "file_name")
    private String fileName;

    /**
     * 文件总长度
     */
    @Property(nameInDb = "total_length")
    private long totalLength;

    /**
     * 下载描述 用于识别是否已经有下载的记录
     */
    @Property(nameInDb = "download_description")
    private String downloadDescription;


    /**
     * 文件的md5值 用于校验
     */
    @Property(nameInDb = "file_md5_string")
    private String fileMd5String;

    /**
     * 是否下载完成
     * 0表示未完成 1表示已完成
     */
    @Property(nameInDb = "is_downloaded")
    private int isDownloaded;

    /**
     * 各片段的下载情况 用 - 分开
     */
    @Property(nameInDb = "file_part_info")
    private String partInfo;



    @Generated(hash = 1574069309)
    public DBDownloadInfo(Long downloadId, String downloadUrl, long readLength,
            String savePathDir, String fileName, long totalLength,
            String downloadDescription, String fileMd5String, int isDownloaded,
            String partInfo) {
        this.downloadId = downloadId;
        this.downloadUrl = downloadUrl;
        this.readLength = readLength;
        this.savePathDir = savePathDir;
        this.fileName = fileName;
        this.totalLength = totalLength;
        this.downloadDescription = downloadDescription;
        this.fileMd5String = fileMd5String;
        this.isDownloaded = isDownloaded;
        this.partInfo = partInfo;
    }

    @Generated(hash = 366453647)
    public DBDownloadInfo() {
    }

    public Long getDownloadId() {
        return this.downloadId;
    }

    public void setDownloadId(Long downloadId) {
        this.downloadId = downloadId;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getReadLength() {
        return this.readLength;
    }

    public void setReadLength(long readLength) {
        this.readLength = readLength;
    }

    public String getSavePathDir() {
        return this.savePathDir;
    }

    public void setSavePathDir(String savePathDir) {
        this.savePathDir = savePathDir;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotalLength() {
        return this.totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public String getDownloadDescription() {
        return this.downloadDescription;
    }

    public void setDownloadDescription(String downloadDescription) {
        this.downloadDescription = downloadDescription;
    }

    public String getFileMd5String() {
        return this.fileMd5String;
    }

    public void setFileMd5String(String fileMd5String) {
        this.fileMd5String = fileMd5String;
    }

    public int getIsDownloaded() {
        return this.isDownloaded;
    }

    public void setIsDownloaded(int isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public String getPartInfo() {
        return this.partInfo;
    }

    public void setPartInfo(String partInfo) {
        this.partInfo = partInfo;
    }

    

}
