package com.john.breakpoint;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.john.breakpoint.greendao.dao.DBDownloadInfoDao;
import com.john.breakpoint.greendao.entity.DBDownloadInfo;
import com.john.breakpoint.greendao.helper.GreenDaoManager;

import com.john.breakpoint.network.DownloadTask;
import com.john.breakpoint.network.StartTask;
import com.john.breakpoint.network.download.DownloadController;
import com.john.breakpoint.network.download.DownloadInfo;
import com.john.breakpoint.network.download.DownloadListStateListener;
import com.john.breakpoint.network.download.DownloadStateListener;

import org.greenrobot.greendao.query.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;


/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/10/24 14:57
 * <p/>
 * Description:
 */
public class DownloadManager {

    private static final String TAG = "DownloadManager";
    private DBDownloadInfoDao downloadInfoDao;
    private HashMap<String, DownloadController> downloadMap;
    private List<String> descriptionList;
    private long startLong;
    private ExecutorService executorService;


    /**
     * 线程片段数量
     */
    private static final int THEAD_POOL_COUNT = 3;


    private DownloadManager() {
        downloadInfoDao = GreenDaoManager.getDaoSession().getDBDownloadInfoDao();
        downloadMap = new HashMap<>();
        descriptionList = new ArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    /**
     * 调用单例对象
     */
    public static DownloadManager get() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 创建单例对象
     */
    private static class SingletonHolder {
        static DownloadManager INSTANCE = new DownloadManager();
    }


    public void download(Activity activity, DownloadInfo preDownloadInfo, final DownloadStateListener downloadListenner) {
        download(activity, preDownloadInfo, downloadListenner, false);
    }

    /**
     * 文件下载
     */
    private void download(Activity activity, DownloadInfo preDownloadInfo, final DownloadStateListener downloadStateListener, boolean isDownloadList) {
        if (preDownloadInfo == null) {
            throw new RuntimeException("preDownloadInfo is empty , you must set");
        }
        //参数校验
        String preUrl = preDownloadInfo.getDownloadUrl();
        String preSavePathDir = preDownloadInfo.getSavePathDir();
        String preFileName = preDownloadInfo.getFileName();
        if (TextUtils.isEmpty(preUrl)) {
            throw new RuntimeException("url is empty , you must set");
        }
        if (TextUtils.isEmpty(preSavePathDir)) {
            throw new RuntimeException("file dir is empty , you must set");
        }
        if (TextUtils.isEmpty(preFileName)) {
            throw new RuntimeException("fileName is empty , you must set");
        }
        String downloadDescription = getDescription(preUrl, preSavePathDir, preFileName);
        //是否正在下载
        if (downloadMap.containsKey(downloadDescription)) {
            Log.e(TAG, "download: DownloadObserver is downloading ------>" + downloadDescription);
            return;
        }
        Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        String pauseDownloadInfo = (String) msg.obj;
                        Log.e(TAG, "handleMessage: 暂停完毕,过程用时 " + (System.currentTimeMillis() - startLong) + "ms");
                        break;
                    case 1://下载完成
                        String finishDownloadInfo = (String) msg.obj;
                        if (downloadStateListener != null) {
                            downloadStateListener.onDownloadSuccess(finishDownloadInfo);
                        }
                        Log.e(TAG, "handleMessage: 本任务下载完成,过程用时 " + (System.currentTimeMillis() - startLong) + "ms");
                        break;
                    case 2:
                        startLong = System.currentTimeMillis();
                        String description = (String) msg.obj;
                        //
                        break;

                    case 3://正在下载
                        Bundle data = msg.getData();
                        String filename = data.getString("filename");
                        long progress = data.getLong("progress");
                        long total = data.getLong("total");
                        if (downloadStateListener != null) {
                            downloadStateListener.onDownloading(filename, progress, total);
                        }
                        break;
                }

            }
        };


        //查找数据库
        Query<DBDownloadInfo> build = downloadInfoDao.queryBuilder().
                where(DBDownloadInfoDao.Properties.DownloadDescription.eq(downloadDescription)).build();
        final DBDownloadInfo dbDownloadInfo = build.unique();
        final DBDownloadInfo realDownloadInfo;
        //如果数据库有此下载记录
        if (dbDownloadInfo != null) {
            realDownloadInfo = dbDownloadInfo;
            if (realDownloadInfo.getIsDownloaded() == 1) {//文件以下载完毕
                File file = new File(realDownloadInfo.getSavePathDir(), realDownloadInfo.getFileName());
                if (file.exists()) {
                    Log.e(TAG, realDownloadInfo.getFileName() + " is downloaded ---->");
                    return;
                }
                Log.e(TAG, "download: some error cause db state is 1 but file is deleted--->");
                resetDownloadInfo(realDownloadInfo);
            }
            DownloadController controller = new DownloadController();
            //读出各片段的信息
            String partInfo = realDownloadInfo.getPartInfo();
            Log.e(TAG, "分段信息 | " + realDownloadInfo.getFileName() + " 片段信息 " + partInfo);
            long hasReadLong = realDownloadInfo.getReadLength();
            controller.atomicLong.set(hasReadLong);
            String[] parts = partInfo.split("&");
            boolean firstPartTag = true;
            downloadMap.put(downloadDescription, controller);
            if (isDownloadList) {
                descriptionList.add(realDownloadInfo.getDownloadDescription());
            }

            for (int i = 0; i < parts.length; i++) {
                String[] startEnd = parts[i].split("-");
                long start = Long.parseLong(startEnd[0]);
                long end = Long.parseLong(startEnd[1]);
                if (start < end) {//如果开始小于结束，则说明没有下载完
                    if (firstPartTag) {//以第一个加载的片段作为回调进度的标志
                        controller.readTag = i;
                        firstPartTag = false;
                        Log.d(TAG, "download: 回调的线程片段为：" + controller.readTag);
                    }
                    controller.partInfoMap.put(i, start + "-" + end);
                    controller.atomicInteger.incrementAndGet();
                    DownloadTask task = new DownloadTask(activity, i, start, end, realDownloadInfo,
                            downloadInfoDao, controller, mHandler, downloadStateListener);
                    controller.tasks.add(task);
                    if (!executorService.isShutdown()) {
                        executorService.execute(task);
                    } else {
                        Log.e(TAG, "download: controller.executorService is Shutdown----->");
                    }
                }
            }
            return;
        }

        //如果没有此记录，则将此下载信息插入到数据库
        DBDownloadInfo info = new DBDownloadInfo();
        info.setDownloadUrl(preUrl);
        info.setSavePathDir(preSavePathDir);
        info.setFileName(preFileName);
        info.setDownloadDescription(downloadDescription);
        info.setThreadPoolCount(THEAD_POOL_COUNT);
        downloadInfoDao.insert(info);
        realDownloadInfo = downloadInfoDao.queryBuilder()
                .where(DBDownloadInfoDao.Properties.DownloadDescription
                        .eq(downloadDescription))
                .build().unique();
        DownloadController controller = new DownloadController();
        StartTask task = new StartTask(activity,executorService,downloadInfoDao, controller, mHandler, downloadStateListener);
        downloadMap.put(downloadDescription, controller);
        if (isDownloadList) {
            descriptionList.add(realDownloadInfo.getDownloadDescription());
        }
        task.execute(realDownloadInfo);
    }


    public void cancelDownload(DownloadInfo downloadInfo) {
        cancelActual(getDownloadDescription(downloadInfo));
    }

    public void cancelDownload(DBDownloadInfo dbDownloadInfo) {
        cancelActual(getDownloadDescription(dbDownloadInfo));
    }

    private void cancelActual(String downloadDescription) {
        if (downloadMap == null || downloadMap.isEmpty()) {
            Log.d(TAG, "cancelActual: can not cancel , observerHashMap is empty");
            return;
        }
        if (TextUtils.isEmpty(downloadDescription)) {
            Log.e(TAG, "cancelActual: downloadInfo has no downloadDescription");
            return;
        }
        if (downloadMap.containsKey(downloadDescription)) {
            Log.d(TAG, "cancelActual: observerHashMap remove key " + downloadDescription);
            DownloadController downloadController = downloadMap.get(downloadDescription);
            for(DownloadTask task:downloadController.tasks){//取消请求
                task.cancel();
            }
            downloadMap.remove(downloadDescription);
        }
        descriptionList.remove(downloadDescription);
    }


    public void cancelAllDownload() {
        if (downloadMap == null || downloadMap.isEmpty()) {
            Log.e(TAG, "cancelAllDownload can not cancel , app is killed or observerHashMap is empty");
            return;
        }
        for (Map.Entry<String, DownloadController> next : downloadMap.entrySet()) {
            DownloadController downloadController = next.getValue();
            if (downloadController != null) {
                for(DownloadTask task:downloadController.tasks){//取消请求
                    task.cancel();
                }
            }
        }
        downloadMap.clear();
        descriptionList.clear();
    }


    public void cancelDownload(List<DownloadInfo> downloadInfoList) {
        if (downloadInfoList == null || downloadInfoList.isEmpty()) {
            Log.e(TAG, "cancelDownload: downloadInfoList is null or empty");
            return;
        }
        for (DownloadInfo downloadInfo : downloadInfoList) {
            cancelDownload(downloadInfo);
        }
    }

    private String getDownloadDescription(DownloadInfo downloadInfo) {
        String downloadUrl = downloadInfo.getDownloadUrl();
        String downloadSavePathDir = downloadInfo.getSavePathDir();
        String downloadFileName = downloadInfo.getFileName();
        return getDescription(downloadUrl, downloadSavePathDir, downloadFileName);
    }

    private String getDownloadDescription(DBDownloadInfo dbDownloadInfo) {
        String dbDownloadUrl = dbDownloadInfo.getDownloadUrl();
        String dbDownloadSavePathDir = dbDownloadInfo.getSavePathDir();
        String dbDownloadFileName = dbDownloadInfo.getFileName();
        return getDescription(dbDownloadUrl, dbDownloadSavePathDir, dbDownloadFileName);
    }

    private String getDescription(String url, String filedDir, String fileName) {
        return url + "-" + filedDir + "/" + fileName;
    }


    private void resetDownloadInfo(DBDownloadInfo dbDownloadInfo) {
        dbDownloadInfo.setReadLength(0);
        dbDownloadInfo.setIsDownloaded(0);
        long total = dbDownloadInfo.getTotalLength();
        long threadCount = dbDownloadInfo.getThreadPoolCount();
        long average = total / threadCount;
        long start;
        long end;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < threadCount; i++) {
            start = i * (average + 1);
            end = start + average + 1;
            if (i == threadCount - 1) {
                end = total;
            }
            builder.append(start).append("-").append(end).append("&");
        }
        //更新文件的总长度和各片段的进度
        dbDownloadInfo.setPartInfo(builder.toString());
        downloadInfoDao.update(dbDownloadInfo);
    }

    public void downloadList(Activity activity, final List<DownloadInfo> downloadInfoList, final DownloadListStateListener downloadListStateListener) {
        descriptionList.clear();
        for (DownloadInfo downloadInfo : downloadInfoList) {
            download(activity, downloadInfo, new DownloadStateListener() {
                @Override
                public void onDownloadStart(String description) {
                    if (downloadListStateListener != null) {
                        downloadListStateListener.onDownloadStart(description);
                    }
                }

                @Override
                public void onDownloading(String saveFileName, long progress, long total) {
                    if (downloadListStateListener != null) {
                        downloadListStateListener.onDownloadingList(saveFileName, progress, total);
                    }
                }

                @Override
                public void onDownloadSuccess(String description) {
                    Log.e(TAG, "onDownloadSuccess:-----> " + description);
                    descriptionList.remove(description);
                    if (descriptionList.isEmpty()) {
                        if (downloadListStateListener != null) {
                            downloadListStateListener.onDownloadListSuccess();
                        }
                    }

                }

                @Override
                public void onDownloadError(String msg) {
                    if (downloadListStateListener != null) {
                        downloadListStateListener.onDownloadListError(msg);
                    }
                }
            }, true);
        }
    }


}
