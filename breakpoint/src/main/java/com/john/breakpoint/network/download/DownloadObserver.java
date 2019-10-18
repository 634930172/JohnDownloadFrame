package com.john.breakpoint.network.download;

import android.util.Log;


import io.reactivex.observers.DisposableObserver;



/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2018/1/15 0015 16:02
 * <p/>
 * Description:文件回调类
 */
public abstract class DownloadObserver<T> extends DisposableObserver<T> {

    private static final String TAG = "DownloadObserver";
    private long mStartCurrentLong;


    @Override
    public void onStart() {
        super.onStart();
        mStartCurrentLong = System.currentTimeMillis();
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
        long callBackTime = System.currentTimeMillis() - mStartCurrentLong;
        Log.e(TAG, "download success , callback time： " + callBackTime + " ms");
    }



    public abstract void onDownloadStart();

    public abstract void onDownloadSuccess(T t);

    public abstract void onDownloadError(String msg);

}
