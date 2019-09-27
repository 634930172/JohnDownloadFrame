package com.john.johndownloadframe.network.download;

import android.util.Log;

import com.john.johndownloadframe.FileSizeUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.observers.DisposableObserver;
import okhttp3.ResponseBody;


/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2018/1/15 0015 16:02
 * <p/>
 * Description:文件回调类
 */
public abstract class DownloadObserver<T> extends DisposableObserver<T> {

    private static final String TAG="DownloadObserver";
    private String fileDir;
    private String fileName;
    private RxAppCompatActivity activity;
    private long mStartCurrentLong;

    public DownloadObserver(RxAppCompatActivity activity, String fileDir, String fileName) {
        this.fileDir=fileDir;
        this.fileName=fileName;
        this.activity=activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        mStartCurrentLong= System.currentTimeMillis();
        onDownloadStart();
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable e) {
        onDownloadError(e.getMessage());
    }

    @Override
    public void onNext(T t) {
        onDownloadSuccess(t);
        long callBackTime= System.currentTimeMillis()-mStartCurrentLong;
        Log.e(TAG,"download success , callback time： "+callBackTime+" ms");
    }

    public void saveFile(ResponseBody body) {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        try {
            final long total=body.contentLength();
            long sum=0;
            is = body.byteStream();
            File dir = new File(fileDir);
            if (!dir.exists()) {
                if(!dir.mkdirs()){
                    Log.e(TAG, "saveFile: dir cannot make" );
                    return;
                }
            }
            File file = new File(dir, fileName);
            Log.d(TAG, "saveFile: path is "+file.getAbsolutePath());
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum+=len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onDownloading(finalSum,total);
                    }
                });
            }
            fos.flush();
            Log.d(TAG, "saveFile: file len is "+ FileSizeUtil.FormatFileSize(file.length()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (fos != null)
                    fos.close();

            } catch (IOException e) {
                Log.e("saveFile", e.getMessage());
            }
        }
    }

    public abstract void onDownloadStart();
    public abstract void onDownloading(long progress,long total);
    public abstract void onDownloadSuccess(T t);
    public abstract void onDownloadError(String msg);

}
