package com.john.breakpoint.network.download;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/10/16 14:11
 * <p/>
 * Description:
 */
public interface DownloadListStateListener {

    void onDownloadStart(String description);

    void onDownloadingList(String saveFileName,long progress, long total );

    void onDownloadListSuccess();

    void onDownloadListError(String msg);



}
