package com.john.johndownloadframe;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


import com.john.breakpoint.greendao.util.FileSizeUtil;
import com.john.breakpoint.BreakPointManager;
import com.john.breakpoint.network.download.DownloadInfo;
import com.john.breakpoint.network.download.DownloadListStateListener;
import com.john.breakpoint.network.download.DownloadStateListener;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/9/23 14:31
 * <p/>
 * Description:
 */
public class DownloadActivity extends RxAppCompatActivity {

    private static final String TAG = "DownloadActivity";
    private ListView listView;
    private DownloadAdapter downloadAdapter;
    private List<DownloadInfo> downloadInfoList;
    private List<DownloadInfo> adapterDownloadInfoList;
    private TextView progress_text;
    private File externalFilesDir;//外部存储的私有目录，应用删除后此文件也会被删除
    private DownloadInfo downloadInfo;
    private TextView current_progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initView();
        initData();
    }

    private void initView() {
        listView = findViewById(R.id.download_listView);
        progress_text = findViewById(R.id.progress_text);
        current_progress = findViewById(R.id.current_progress);
    }

    private void initData() {
        downloadAdapter = new DownloadAdapter(this);
        listView.setAdapter(downloadAdapter);
        downloadInfoList = new ArrayList<>();
        adapterDownloadInfoList = new ArrayList<>();
        //下载相关信息
        externalFilesDir = getExternalFilesDir(null);
        //一张图片
        downloadInfo = new DownloadInfo();
        downloadInfo.setFileName("app-download.mp4");
        downloadInfo.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo.setDownloadUrl("http://v2admin.app.eyes3d.com.cn/3d_demo/demo_01.mp4");
        //加载多任务测试数据apk
        loadDataList();
        //加载列表下载任务测试数据apk
        loadAdapterDataList();
    }

    private void loadDataList() {
        DownloadInfo downloadInfo1 = new DownloadInfo();
        downloadInfo1.setDownloadUrl("http://94.191.50.122/demo/breakPointDownloadApk");
        downloadInfo1.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo1.setFileName("app-download-m1.apk");
        downloadInfoList.add(downloadInfo1);

        DownloadInfo downloadInfo2 = new DownloadInfo();
        downloadInfo2.setDownloadUrl("http://94.191.50.122/demo/breakPointDownloadApk2");
        downloadInfo2.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo2.setFileName("app-download-m2.apk");
        downloadInfoList.add(downloadInfo2);
    }


    private void loadAdapterDataList() {
        DownloadInfo downloadInfo3 = new DownloadInfo();
        downloadInfo3.setDownloadUrl("http://94.191.50.122/demo/breakPointDownloadApk3");
        downloadInfo3.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo3.setFileName("app-download-m3.apk");
        adapterDownloadInfoList.add(downloadInfo3);

        DownloadInfo downloadInfo4 = new DownloadInfo();
        downloadInfo4.setDownloadUrl("http://94.191.50.122/demo/breakPointDownloadApk4");
        downloadInfo4.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo4.setFileName("app-download-m4.apk");
        adapterDownloadInfoList.add(downloadInfo4);
    }


    private void dealDownload(DownloadInfo downloadInfo) {
        BreakPointManager.get().download(this, downloadInfo, new DownloadStateListener() {

            @Override
            public void onDownloadStart(String description) {
                Log.e(TAG, "onDownloadStart: description-->" + description);
            }

            @Override
            public void onDownloading(String saveFileName, long progress, long total) {
                progress_text.setText(getString(R.string.current_progress, saveFileName,
                        FileSizeUtil.FormatFileSize(progress), FileSizeUtil.FormatFileSize(total)));
            }

            @Override
            public void onDownloadSuccess(String description) {
                Log.e(TAG, "onDownloadSuccess:  | " + (Looper.myLooper() == Looper.getMainLooper()));
                progress_text.setText(getString(R.string.download_success));
            }

            @Override
            public void onDownloadError(String msg) {
                Log.e(TAG, "onDownloadError: " + msg + " | " + (Looper.myLooper() == Looper.getMainLooper()));
            }
        });

    }

    /**
     * 开始单任务
     */
    public void startDownload(View view) {
        dealDownload(downloadInfo);
    }

    /**
     * 暂停单任务
     */
    public void stopDownload(View view) {
        BreakPointManager.get().cancelDownload(downloadInfo);
    }

    /**
     * 开始多任务
     */
    public void startManyDownload(View view) {
        BreakPointManager.get().downloadList(this, downloadInfoList, new DownloadListStateListener() {
            @Override
            public void onDownloadStart(String description) {
                Log.d(TAG, "onDownloadStart: " + description);
            }

            @Override
            public void onDownloadingList(String saveFileName, long progress, long total) {
                current_progress.setText(getString(R.string.current_progress, saveFileName,
                        FileSizeUtil.FormatFileSize(progress), FileSizeUtil.FormatFileSize(total)));
            }

            @Override
            public void onDownloadListSuccess() {
                Log.d(TAG, "onDownloadListSuccess: ------->");
                current_progress.setText(getString(R.string.many_download_end));
            }

            @Override
            public void onDownloadListError(String msg) {
                Log.d(TAG, "onDownloadListError: " + msg);
                progress_text.setText(msg);
            }
        });

    }

    /**
     * 暂停多任务
     */
    public void stopManyDownload(View view) {
        BreakPointManager.get().cancelDownload(downloadInfoList);
    }

    /**
     * 开始列表任务
     */
    public void startListDownload(View view) {
        downloadAdapter.startDownload(adapterDownloadInfoList);
    }

    /**
     * 暂停列表任务
     */
    public void stopListDownload(View view) {
        BreakPointManager.get().cancelDownload(adapterDownloadInfoList);
    }


    /**
     * 页面结束时取消下载
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果没有引用lifecycle 则页面结束时调用此方法
        //BreakPointManager.get().cancelAllDownload();
    }
}
