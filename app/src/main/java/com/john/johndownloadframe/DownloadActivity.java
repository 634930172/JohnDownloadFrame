package com.john.johndownloadframe;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.john.johndownloadframe.network.AppService;
import com.john.johndownloadframe.network.MainConfig;
import com.john.johndownloadframe.network.download.DownLoadClient;
import com.john.johndownloadframe.network.download.DownloadObserver;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/9/23 14:31
 * <p/>
 * Description:
 */
public class DownloadActivity extends RxAppCompatActivity {

    private static final String TAG="DownloadActivity";
    private ListView listView;
    private DownloadAdapter downloadAdapter;
    private List<String> fileNames;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        listView = findViewById(R.id.download_listView);
    }

    private void initData() {
        MainConfig mainConfig=getIntent().getParcelableExtra("config");
        Log.e(TAG, "initData: "+mainConfig.getName()+"---"+mainConfig.getAge());
        downloadAdapter = new DownloadAdapter(this);
        listView.setAdapter(downloadAdapter);

        String fileName = "app-download";
        fileNames = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            fileNames.add(fileName+"_"+i + ".apk");
        }

    }

    private void initEvent() {

    }


    public void startDownload(View view) {
        downloadAdapter.startDownload(fileNames);

    }


    public void stopDownload(View view) {
        Log.d(TAG, "stopDownload: ");
    }


    private void start(){
        String fileName = "app-download.apk";
        final File externalFilesDir;//外部存储的私有目录，应用删除后此文件也会被删除
        externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir == null) {
            return;
        }
        String URL2 = "http://94.191.50.122/demo/testDownload";
        download(DownLoadClient.getService(AppService.class).download(URL2),new DownloadObserver<ResponseBody>(this,externalFilesDir.toString(), fileName) {
            @Override
            public void onDownloadStart() {
                Log.e(TAG, "onDownloadStart--->"+(Looper.myLooper()==Looper.getMainLooper()) );
            }

            @Override
            public void onDownloading(long progress, long total) {
                Log.e(TAG, "progress: " + FileSizeUtil.FormatFileSize(progress) +
                        "  total: " + FileSizeUtil.FormatFileSize(total) +
                        " | "+(Looper.myLooper()==Looper.getMainLooper()));
            }

            @Override
            public void onDownloadSuccess(ResponseBody responseBody) {
                Log.e(TAG, "onDownloadSuccess:  | "+(Looper.myLooper()==Looper.getMainLooper()));
            }

            @Override
            public void onDownloadError(String msg) {
                Log.e(TAG, "onDownloadError: "+msg+" | "+(Looper.myLooper()==Looper.getMainLooper()));
            }

        });
    }

    /**
     * 文件下载
     * @param downloadObserver  下载观察者类
     */
    private void download(Observable<ResponseBody> downloadObservable, final DownloadObserver<ResponseBody> downloadObserver) {
        downloadObservable.subscribeOn(Schedulers.io())//请求网络 在调度者的io线程
                .observeOn(Schedulers.io()) //指定线程保存文件
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) {
                        downloadObserver.saveFile(responseBody);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) //在主线程中更新ui
                .compose(this.<ResponseBody>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(downloadObserver);
    }

}
