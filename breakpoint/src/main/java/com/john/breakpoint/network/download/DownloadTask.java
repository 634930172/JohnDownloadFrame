package com.john.breakpoint.network.download;

import android.util.Log;

import com.john.breakpoint.greendao.dao.DBDownloadInfoDao;
import com.john.breakpoint.greendao.entity.DBDownloadInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.ResponseBody;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/10/22 10:07
 * <p/>
 * Description:
 */
public class DownloadTask implements Runnable {

    private long start;
    private long end;
    private DBDownloadInfo dbDownloadInfo;
    private ResponseBody body;
    private DBDownloadInfoDao dbDownloadInfoDao;
    private InputStream inputStream;
    private static final String TAG = "DownloadTask";


    public DownloadTask(long start, long end, DBDownloadInfo dbDownloadInfo, InputStream inputStream, DBDownloadInfoDao dbDownloadInfoDao) {
        this.start = start;
        this.end = end;
        this.dbDownloadInfo = dbDownloadInfo;
        this.body = body;
        this.dbDownloadInfoDao = dbDownloadInfoDao;
        this.inputStream=inputStream;
    }


    @Override
    public void run() {
        long c=System.currentTimeMillis();
        Log.d(TAG, "DownloadTask: period "+start+"-"+end+"|"+Thread.currentThread().getName());
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
        byte[] buf = new byte[1024 * 4];
        int len;
        long sum = 0;
        try {
            randomAccessFile = new RandomAccessFile(file, "rwd");
            channel = randomAccessFile.getChannel();
            MappedByteBuffer mappedByteBuffer = null;
            mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE,
                    start, end-start);
            while ((len = inputStream.read(buf)) != -1) {
                sum += len;
                mappedByteBuffer.put(buf, 0, len);
            }
            Log.e(TAG, "run:  period "+start+"-"+end+" spend time---->"+(System.currentTimeMillis()-c));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "run: "+e.getMessage());
        }finally {
            try {
//                if (is != null)
//                    is.close();
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
}
