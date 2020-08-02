package com.john.breakpoint.network;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.john.breakpoint.DownloadManager;
import com.john.breakpoint.greendao.dao.DBDownloadInfoDao;
import com.john.breakpoint.greendao.entity.DBDownloadInfo;
import com.john.breakpoint.network.download.DownloadController;
import com.john.breakpoint.network.download.DownloadStateListener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/10/23 14:48
 * <p/>
 * Description:
 */
public class StartTask extends AsyncTask<DBDownloadInfo, Void, DBDownloadInfo> {

    private static final String TAG = "StartTask";
    private DBDownloadInfoDao dbDownloadInfoDao;
    private DownloadController controller;
    private Handler handler;
    private DownloadStateListener downloadStateListener;
    private Activity activity;
    private ExecutorService executorService;
    public StartTask(Activity activity,ExecutorService executorService,DBDownloadInfoDao downloadInfoDao, DownloadController controller, Handler handler
                    , DownloadStateListener downloadStateListener){
        this.activity=activity;
        this.executorService=executorService;
        this.dbDownloadInfoDao=downloadInfoDao;
        this.handler=handler;
        this.controller=controller;
        this.downloadStateListener=downloadStateListener;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "onPreExecute:---> ");
    }

    @Override
    protected DBDownloadInfo doInBackground(DBDownloadInfo... dbDownloadInfoList) {
        DBDownloadInfo dbDownloadInfo = dbDownloadInfoList[0];
        long total;
        HttpURLConnection conn = null;
        try {
            String downloadUrl = dbDownloadInfo.getDownloadUrl();
            URL url = new URL(downloadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg," +
                    " image/pjpeg, application/x-shockwave-flash, application/xaml+xml, " +
                    "application/vnd.ms-xpsdocument, application/x-ms-xbap," +
                    " application/x-ms-application, application/vnd.ms-excel, " +
                    "application/vnd.ms-powerpoint, application/msword, */*");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Referer", downloadUrl);
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2;" +
                    " Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30;" +
                    " .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();
            if (conn.getResponseCode() == 200) {
                total = conn.getContentLength();
                dbDownloadInfo.setTotalLength(total);
            } else {
                throw new RuntimeException("server no response ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            DownloadManager.get().cancelDownload(dbDownloadInfo);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return dbDownloadInfo;
    }


    @Override
    protected void onPostExecute(DBDownloadInfo dbDownloadInfo) {
        super.onPostExecute(dbDownloadInfo);
        int threadPoolCount=dbDownloadInfo.getThreadPoolCount();
        long total=dbDownloadInfo.getTotalLength();
        //对文件进行分段
        StringBuilder builder=new StringBuilder();
        long average = total / threadPoolCount;
        long start;
        long end;
        for (int i = 0; i < threadPoolCount; i++) {
            start = i * (average + 1);
            end = start + average + 1;
            if (i == threadPoolCount - 1) {
                end = total;
            }
            builder.append(start).append("-").append(end).append("&");
            //开始下载任务
            controller.partInfoMap.put(i,start+"-"+end);
            controller.atomicInteger.incrementAndGet();
            DownloadTask task = new DownloadTask(activity,i,start, end, dbDownloadInfo,
                    dbDownloadInfoDao,controller,handler,downloadStateListener);
            controller.tasks.add(task);
            if(!executorService.isShutdown()){
                executorService.execute(task);
            }else {
                Log.e(TAG, "onPostExecute: controller.executorService is shutdown----->" );
            }
        }
        //此处更新了文件的总长度和各片段的进度
        String parts=builder.toString();
        Log.e(TAG, "初次分段信息："+dbDownloadInfo.getFileName()+" | "+parts );
        dbDownloadInfo.setPartInfo(parts);
        dbDownloadInfoDao.update(dbDownloadInfo);
    }
}
