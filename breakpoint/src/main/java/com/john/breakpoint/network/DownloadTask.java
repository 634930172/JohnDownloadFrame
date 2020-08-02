package com.john.breakpoint.network;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.john.breakpoint.DownloadManager;
import com.john.breakpoint.greendao.dao.DBDownloadInfoDao;
import com.john.breakpoint.greendao.entity.DBDownloadInfo;
import com.john.breakpoint.greendao.util.FileSizeUtil;
import com.john.breakpoint.network.download.DownloadController;
import com.john.breakpoint.network.download.DownloadStateListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;


/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/10/23 16:36
 * <p/>
 * Description:
 */
public class DownloadTask implements Runnable {

    private long start;
    private long end;
    private static final String TAG = "DownloadTask";
    private DBDownloadInfo dbDownloadInfo;
    private DBDownloadInfoDao dbDownloadInfoDao;
    private DownloadController controller;
    private int part;
    private Handler handler;
    private DownloadStateListener downloadStateListener;
    private Activity activity;
    private HttpURLConnection conn = null;
    public DownloadTask(Activity activity, int part, long start, long end, DBDownloadInfo dbDownloadInfo,
                        DBDownloadInfoDao dbDownloadInfoDao, DownloadController controller,
                        Handler handler, DownloadStateListener downloadStateListener) {
        this.activity = activity;
        this.start = start;
        this.end = end;
        this.dbDownloadInfo = dbDownloadInfo;
        this.dbDownloadInfoDao = dbDownloadInfoDao;
        this.controller = controller;
        this.part = part;
        this.handler = handler;
        this.downloadStateListener = downloadStateListener;
    }


    @Override
    public void run() {
        try {
            String downloadUrl = dbDownloadInfo.getDownloadUrl();
            URL url = new URL(downloadUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000 * 60 * 5);//连接下载超时时间设置为5分钟
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
            //设置此片段返回的信息
            conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
            conn.connect();
            if (conn.getResponseCode() == 206) {
                download(conn.getInputStream());
            } else {
                throw new RuntimeException("server no response ");
            }
        } catch (IOException e) {
            e.printStackTrace();
            DownloadManager.get().cancelDownload(dbDownloadInfo);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    private   void download(InputStream inputStream) {
        long c=System.currentTimeMillis();
        File dir = new File(dbDownloadInfo.getSavePathDir());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "saveFile: dir cannot make");
                return;
            }
        }
        File file = new File(dir, dbDownloadInfo.getFileName());
        RandomAccessFile randomAccessFile = null;
        FileChannel channel = null;
        int len;
        byte[] buf = new byte[1024 * 4];
        long sum = 0;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
//            randomAccessFile.seek(start);
            channel = randomAccessFile.getChannel();
            MappedByteBuffer mappedByteBuffer;
            mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE,
                    start, end - start);
            while ((len = inputStream.read(buf)) != -1) {
                sum += len;
                mappedByteBuffer.put(buf, 0, len);
//                randomAccessFile.write(buf,0,len);
                controller.atomicLong.addAndGet(len);
                //只有标记的线程才回调信息 防止主线程阻塞
                if (controller.readTag == part) {
                    if (controller.atomicLong.get() < dbDownloadInfo.getTotalLength()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (downloadStateListener != null) {
                                    downloadStateListener.onDownloading(dbDownloadInfo.getFileName(), controller.atomicLong.get(), dbDownloadInfo.getTotalLength());
                                }
                            }
                        });

                        //                        Message message = handler.obtainMessage();
                        //                        message.what=3;
                        //                        Bundle bundle=new Bundle();
                        //                        bundle.putString("filename",dbDownloadInfo.getFileName());
                        //                        bundle.putLong("progress",controller.atomicLong.get());
                        //                        bundle.putLong("total",dbDownloadInfo.getTotalLength());
                        //                        message.setData(bundle);
                        //                        handler.sendMessage(message);

                        //                        handler.post(new Runnable() {
                        //                            @Override
                        //                            public void run() {
                        ////                                if (downloadStateListener != null) {
                        ////                                    downloadStateListener.onDownloading(dbDownloadInfo.getFileName(), controller.atomicLong.get(), dbDownloadInfo.getTotalLength());
                        ////                                }
                        //                            }
                        //                        });
                    }
                }
            }
            //保留数据
            long hasRead = start + sum;
            controller.partInfoMap.put(part, hasRead + "-" + end);
            controller.atomicInteger.decrementAndGet();
            if (controller.atomicInteger.get() == 0) {
                Log.e(TAG, "download: 任务结束 记录状态 --->" + dbDownloadInfo.getFileName()+ "|" + part + "|" + Thread.currentThread().getName());
                StringBuilder builder = new StringBuilder();
                boolean isFinish = true;
                for (Map.Entry<Integer, String> next : controller.partInfoMap.entrySet()) {
                    String partInfo = next.getValue();
                    String[] startEnd = partInfo.split("-");
                    String start = startEnd[0];
                    String end = startEnd[1];
                    if (!start.equals(end)) {
                        isFinish = false;
                    }
                    builder.append(part).append("&");
                }
                dbDownloadInfo.setPartInfo(builder.toString());
                Log.e(TAG, "download: isFinish----->" + isFinish);
                if (isFinish) {
                    //下载完成 更新数据库
                    dbDownloadInfo.setIsDownloaded(1);
                    controller.partInfoMap.clear();
                    //发送下载完成的消息
                    //                    Message message = handler.obtainMessage();
                    //                    message.what = 1;
                    //                    message.obj = dbDownloadInfo.getDownloadDescription();
                    //                    handler.sendMessage(message);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (downloadStateListener != null) {
                                downloadStateListener.onDownloadSuccess(dbDownloadInfo.getDownloadDescription());
                            }
                        }
                    });

                    Log.e(TAG, "download: 下载完成----->" + dbDownloadInfo.getFileName()+" 用时 "+(System.currentTimeMillis()-c)+" ms");
                } else {
                    //还有部分任务没有下载完成 更新数据库
                    Log.e(TAG, "download: 更新进度" + dbDownloadInfo.getFileName() + ", 已读取 " + FileSizeUtil.FormatFileSize(controller.atomicLong.get()));
                }
                DownloadManager.get().cancelDownload(dbDownloadInfo);
                dbDownloadInfo.setReadLength(controller.atomicLong.get());
                dbDownloadInfoDao.update(dbDownloadInfo);
            } else {
                Log.e(TAG, "download: 读取结束--->" + dbDownloadInfo.getFileName() + "|" + part + "|" + Thread.currentThread().getName());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "download: FileNotFoundException" + e.getMessage());
        } catch (IOException e) {
            //保留数据
            Log.e(TAG, "线程异常: " + dbDownloadInfo.getFileName() + " | " + part + " | " + Thread.currentThread().getName()+" reason "+e.getMessage());
            long hasRead = start + sum;
            controller.partInfoMap.put(part, hasRead + "-" + end);
            controller.atomicInteger.decrementAndGet();
            if (controller.atomicInteger.get() == 0) {
                StringBuilder builder = new StringBuilder();
                for (Map.Entry<Integer, String> next : controller.partInfoMap.entrySet()) {
                    String partInfo = next.getValue();
                    builder.append(partInfo).append("&");
                }
                dbDownloadInfo.setPartInfo(builder.toString());
                dbDownloadInfo.setReadLength(controller.atomicLong.get());
                Log.e(TAG, " download:IO异常统计完毕:" + dbDownloadInfo.getFileName() + " , 已读取 " + FileSizeUtil.FormatFileSize(controller.atomicLong.get()));
                dbDownloadInfoDao.update(dbDownloadInfo);
                DownloadManager.get().cancelDownload(dbDownloadInfo);
            }
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (channel != null) {
                    channel.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void cancel(){
        if(conn!=null){
            conn.disconnect();
        }
    }

}
