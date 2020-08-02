package com.john.breakpoint;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.PopupWindow;
import android.widget.Toast;


import com.john.breakpoint.greendao.dao.DBDownloadInfoDao;
import com.john.breakpoint.greendao.entity.DBDownloadInfo;
import com.john.breakpoint.greendao.helper.GreenDaoManager;
import com.john.breakpoint.greendao.util.FileSizeUtil;
import com.john.breakpoint.network.AppService;
import com.john.breakpoint.network.download.DownLoadClient;
import com.john.breakpoint.network.download.DownloadInfo;
import com.john.breakpoint.network.download.DownloadListStateListener;
import com.john.breakpoint.network.download.DownloadObserver;
import com.john.breakpoint.network.download.DownloadStateListener;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.greenrobot.greendao.query.Query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;


/**
 * Author: ${John}
 * E-mail: 634930172@qq.com
 * Date: 2017/12/5 0005
 * <p/>
 * Description:断点下载工具类
 */

public class BreakPointManager {

    private static final String TAG = "BreakPointManager";
    private DBDownloadInfoDao downloadInfoDao;
    private HashMap<String, DownloadObserver> observerHashMap;
    private List<String> descriptionList;

    private BreakPointManager() {
        downloadInfoDao = GreenDaoManager.getDaoSession().getDBDownloadInfoDao();
        observerHashMap = new HashMap<>();
        descriptionList = new ArrayList<>();
    }

    /**
     * 调用单例对象
     */
    public static BreakPointManager get() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 创建单例对象
     */
    private static class SingletonHolder {
        static BreakPointManager INSTANCE = new BreakPointManager();
    }


    public void download(final RxAppCompatActivity activity, DownloadInfo preDownloadInfo, final DownloadStateListener downloadListenner){
        download(activity,preDownloadInfo,downloadListenner,false);
    }

    /**
     * 文件下载
     */
    private void download(final RxAppCompatActivity activity, DownloadInfo preDownloadInfo, final DownloadStateListener downloadStateListener,boolean isDownloadList) {
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
        if (observerHashMap.containsKey(downloadDescription)) {
            Log.e(TAG, "download: DownloadObserver is downloading ------>"+downloadDescription);
            return;
        }

        //查找数据库
        Query<DBDownloadInfo> build = downloadInfoDao.queryBuilder().
                where(DBDownloadInfoDao.Properties.DownloadDescription.eq(downloadDescription)).build();
        final DBDownloadInfo dbDownloadInfo = build.unique();
        final DBDownloadInfo realDownloadInfo;
        //如果数据库有此下载记录
        if (dbDownloadInfo != null) {
            realDownloadInfo = dbDownloadInfo;
        } else {
            //如果没有此记录，则将此下载信息插入到数据库
            DBDownloadInfo info = new DBDownloadInfo();
            info.setDownloadUrl(preUrl);
            info.setSavePathDir(preSavePathDir);
            info.setFileName(preFileName);
            info.setDownloadDescription(downloadDescription);
            downloadInfoDao.insert(info);
            realDownloadInfo = downloadInfoDao.queryBuilder()
                    .where(DBDownloadInfoDao.Properties.DownloadDescription
                            .eq(downloadDescription))
                    .build().unique();
        }
        //对将要下载的文件做校验
        if (realDownloadInfo.getIsDownloaded() == 1) {
            File file = new File(realDownloadInfo.getSavePathDir(), realDownloadInfo.getFileName());
            if (file.exists()) {
                Log.e(TAG, "download: this file is downloaded , description is " + realDownloadInfo.getDownloadDescription());
                Toast.makeText(activity, activity.getString(R.string.downloaded, realDownloadInfo.getFileName()), Toast.LENGTH_LONG).show();
                return;
            }
            Log.e(TAG, "download: some error cause db state is 1 but file is deleted--->");
            realDownloadInfo.setReadLength(0);
            realDownloadInfo.setIsDownloaded(0);
            downloadInfoDao.update(realDownloadInfo);
        }
        final long startDownloadIndex = realDownloadInfo.getReadLength();
        //将头部下载信息传给服务端，让其解析
        String range;
        if(startDownloadIndex!=0){
             range = "bytes=" + startDownloadIndex + "-";
        }else {
            range=null;
        }
        Log.e(TAG, "download: range is : " + range);
        //成功后更新数据库的值
        DownloadObserver<ResponseBody> downloadObserver = new DownloadObserver<ResponseBody>() {
            @Override
            public void onDownloadStart() {
                if (downloadStateListener != null) {
                    downloadStateListener.onDownloadStart(realDownloadInfo.getDownloadDescription());
                }
            }

            @Override
            public void onDownloadSuccess(ResponseBody responseBody) {
                //成功后更新数据库的值
                realDownloadInfo.setIsDownloaded(1);
                downloadInfoDao.update(realDownloadInfo);
                cancelDownload(realDownloadInfo);
                if (downloadStateListener != null) {
                    downloadStateListener.onDownloadSuccess(realDownloadInfo.getDownloadDescription());
                }
            }

            @Override
            public void onDownloadError(String msg) {
                Log.e(TAG, "onDownloadError: "+msg);
                cancelDownload(realDownloadInfo);
                if (downloadStateListener != null) {
                    downloadStateListener.onDownloadError(msg);
                }
            }

        };

        observerHashMap.put(realDownloadInfo.getDownloadDescription(), downloadObserver);
        if(isDownloadList){
            descriptionList.add(realDownloadInfo.getDownloadDescription());
        }
        DownLoadClient.getService(AppService.class)
                .download(range, realDownloadInfo.getDownloadUrl())
                .subscribeOn(Schedulers.io())//请求网络 在调度者的io线程
                .observeOn(Schedulers.io()) //指定线程保存文件
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) {
                        saveFile(activity, responseBody, realDownloadInfo, downloadInfoDao, downloadStateListener);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) //在主线程中更新ui
                .compose(activity.<ResponseBody>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(downloadObserver);
    }


    public void cancelDownload(DownloadInfo downloadInfo) {
        cancelActual(getDownloadDescription(downloadInfo));
    }

    private void cancelDownload(DBDownloadInfo dbDownloadInfo) {
        cancelActual(getDownloadDescription(dbDownloadInfo));
    }

    private void cancelActual(String downloadDescription) {
        if (observerHashMap == null || observerHashMap.isEmpty()) {
            Log.d(TAG, "cancelActual: can not cancel , observerHashMap is empty");
            return;
        }
        if (TextUtils.isEmpty(downloadDescription)) {
            Log.e(TAG, "cancelActual: downloadInfo has no downloadDescription");
            return;
        }
        if (observerHashMap.containsKey(downloadDescription)) {
            Log.d(TAG, "cancelActual: observerHashMap remove key "+downloadDescription);
            DownloadObserver downloadObserver = observerHashMap.get(downloadDescription);
            if (downloadObserver != null && !downloadObserver.isDisposed()) {
                downloadObserver.dispose();
            }
            observerHashMap.remove(downloadDescription);
        }
        descriptionList.remove(downloadDescription);
    }


    public void cancelAllDownload() {
        if (observerHashMap == null || observerHashMap.isEmpty()) {
            Log.e(TAG, "cancelAllDownload can not cancel , app is killed or observerHashMap is empty");
            return;
        }
        for (Map.Entry<String, DownloadObserver> next : observerHashMap.entrySet()) {
            DownloadObserver downloadObserver = next.getValue();
            if (downloadObserver != null && !downloadObserver.isDisposed()) {
                downloadObserver.dispose();
            }
        }
        observerHashMap.clear();
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


    private void saveFile(Activity activity, ResponseBody body, final DBDownloadInfo dbDownloadInfo, DBDownloadInfoDao downloadInfoDao, final DownloadStateListener downloadStateListener) {
        InputStream is = null;
        byte[] buf = new byte[1024*4];
        int len;
        FileChannel channel = null;
        RandomAccessFile randomAccessFile = null;
        long sum = 0;
        final long hasReadLen = dbDownloadInfo.getReadLength();
        try {
            final long total = body.contentLength();
            long readLength = dbDownloadInfo.getReadLength();
            //如果读取的长度是0，则说明本次服务器返回的body是全长的
            if (readLength == 0) {
                dbDownloadInfo.setTotalLength(total);
                downloadInfoDao.update(dbDownloadInfo);
                Log.d(TAG, "saveFile: this download length is " + FileSizeUtil.FormatFileSize(total)+"|"+total);
            }
            is = body.byteStream();
            File dir = new File(dbDownloadInfo.getSavePathDir());
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "saveFile: dir cannot make");
                    return;
                }
            }
            File file = new File(dir, dbDownloadInfo.getFileName());
            Log.d(TAG, "want to save file " + file.getAbsolutePath());
            randomAccessFile = new RandomAccessFile(file, "rwd");
            channel = randomAccessFile.getChannel();
            MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE,
                    dbDownloadInfo.getReadLength(), total);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                mappedByteBuffer.put(buf, 0, len);
                final long finalSum = sum;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (downloadStateListener != null) {
                            downloadStateListener.onDownloading(dbDownloadInfo.getFileName(),hasReadLen+ finalSum, dbDownloadInfo.getTotalLength());
                        }
                    }
                });
            }
            Log.d(TAG, "saveFile: file len is " + FileSizeUtil.FormatFileSize(file.length()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            dbDownloadInfo.setReadLength(hasReadLen + sum);
            Log.e(TAG, "saveFile: saving Exception , try to save progress :" + (hasReadLen + sum));
            downloadInfoDao.update(dbDownloadInfo);//更新数据库进度
            cancelDownload(dbDownloadInfo);
        } finally {
            try {
                if (is != null)
                    is.close();
                if (channel != null) {
                    channel.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }

            } catch (IOException e) {
                Log.e("saveFile", e.getMessage());
            }
        }
    }


    public void downloadList(RxAppCompatActivity activity, final List<DownloadInfo> downloadInfoList, final DownloadListStateListener downloadListStateListener) {
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
                    descriptionList.remove(description);
                    if(descriptionList.isEmpty()){
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
            },true);
        }
    }

}
