package com.john.breakpoint.network.download;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/10/11 17:01
 * <p/>
 * Description:
 */
public interface DownloadStateListener {

    void onDownloadStart(String description);

    void onDownloading(String saveFileName,long progress, long total);

    void onDownloadSuccess(String description);

    void onDownloadError(String msg);

}
