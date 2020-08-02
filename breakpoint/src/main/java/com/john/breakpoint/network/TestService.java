package com.john.breakpoint.network;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/11/4 18:09
 * <p/>
 * Description:
 */
public class TestService extends Service {

    private static final String TAG="TestService";

//    public TestService() {
//        super("TestService");
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ----->"+(Looper.myLooper()==Looper.getMainLooper()) );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: "+(Looper.myLooper()==Looper.getMainLooper()) );
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: "+(Looper.myLooper()==Looper.getMainLooper())  );
        return null;
    }

//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        Log.e(TAG, "onHandleIntent: "+(Looper.myLooper()==Looper.getMainLooper()) );
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: "+(Looper.myLooper()==Looper.getMainLooper()) );
    }
}
