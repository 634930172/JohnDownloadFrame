package com.john.johndownloadframe;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.john.breakpoint.DownloadManager;
import com.john.breakpoint.greendao.util.FileSizeUtil;
import com.john.breakpoint.network.download.DownloadInfo;
import com.john.breakpoint.network.download.DownloadListStateListener;
import com.john.breakpoint.network.download.DownloadStateListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/9/23 14:31
 * <p/>
 * Description:
 */
public class UploadActivity extends AppCompatActivity {

    private TextView tts;
    private static final String TAG="UploadActivity";
    private DownloadInfo downloadInfo;
    private File externalFilesDir;
    private List<DownloadInfo> downloadInfoList;
    private List<DownloadInfo> adapterDownloadInfoList;
    private UploadadAdapter uploadAdapter;
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        initView();
        initData();
    }

    private void initView() {
        tts = findViewById(R.id.tts);
        listView=findViewById(R.id.upload_listView);
    }


    private void initData() {
        uploadAdapter = new UploadadAdapter(this);
        listView.setAdapter(uploadAdapter);
        //下载相关信息
        //外部存储的私有目录，应用删除后此文件也会被删除
        externalFilesDir = getExternalFilesDir(null);
        downloadInfo = new DownloadInfo();
        downloadInfo.setFileName("app-download-m.apk");
        downloadInfo.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo.setDownloadUrl("http://10.10.75.244:8087/demo/breakPointDownloadApk4");

        //加载多任务测试数据apk
        downloadInfoList = new ArrayList<>();
        loadDataList();
        //加载列表下载任务测试数据apk
        adapterDownloadInfoList=new ArrayList<>();
        loadAdapterDataList();

    }

    private void loadAdapterDataList() {
        DownloadInfo downloadInfo3 = new DownloadInfo();
        downloadInfo3.setDownloadUrl("http://10.10.75.244:8087/demo/breakPointDownloadApk4");
        downloadInfo3.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo3.setFileName("app-download-m3.apk");
        adapterDownloadInfoList.add(downloadInfo3);

        DownloadInfo downloadInfo4 = new DownloadInfo();
        downloadInfo4.setDownloadUrl("http://10.10.75.244:8087/demo/breakPointDownloadApk4");
        downloadInfo4.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo4.setFileName("app-download-m4.apk");
        adapterDownloadInfoList.add(downloadInfo4);
    }

    private void loadDataList() {
        DownloadInfo downloadInfo1 = new DownloadInfo();
        downloadInfo1.setDownloadUrl("http://10.10.75.244:8087/demo/breakPointDownloadApk4");
        downloadInfo1.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo1.setFileName("app-download-m1.apk");
        downloadInfoList.add(downloadInfo1);

        DownloadInfo downloadInfo2 = new DownloadInfo();
        downloadInfo2.setDownloadUrl("http://10.10.75.244:8087/demo/breakPointDownloadApk4");
        downloadInfo2.setSavePathDir(externalFilesDir.getAbsolutePath());
        downloadInfo2.setFileName("app-download-m2.apk");
        downloadInfoList.add(downloadInfo2);
    }

    /**
     * 开始单任务
     */
    public void startOne(View view) {
        DownloadManager.get().download(this,downloadInfo, new DownloadStateListener() {
            @Override
            public void onDownloadStart(String description) {
                Log.d(TAG, "onDownloadStart: "+description);
                tts.setText(description);
            }

            @Override
            public void onDownloading(String saveFileName, long progress, long total) {
                tts.setText("onDownloading: "+saveFileName+" | "+ progress
                        +" | "+total);
            }

            @Override
            public void onDownloadSuccess(String description) {
                Log.d(TAG, "onDownloadSuccess: "+description);
                tts.setText("onDownloadSuccess: 下载完毕");
            }

            @Override
            public void onDownloadError(String msg) {
                Log.d(TAG, "onDownloadError: "+msg);
                tts.setText("onDownloadError: "+msg);
            }
        });

    }

    /**
     * 停止单任务
     */
    public void stopOne(View view) {
        DownloadManager.get().cancelDownload(downloadInfo);
    }

    /**
     * 开始多任务
     */
    public void startMany(View view){
        DownloadManager.get().downloadList(this,downloadInfoList, new DownloadListStateListener() {
            @Override
            public void onDownloadStart(String description) {
                Log.e(TAG, "onDownloadStart: "+description);
            }

            @Override
            public void onDownloadingList(String saveFileName, long progress, long total) {
                tts.setText("onDownloading: "+saveFileName+" progress "+ FileSizeUtil.FormatFileSize(progress)
                        +" total "+FileSizeUtil.FormatFileSize(total)+" | "+(Looper.getMainLooper()==Looper.myLooper()));
            }

            @Override
            public void onDownloadListSuccess() {
                Log.e(TAG, "onDownloadListSuccess: --->" );
                tts.setText("onDownloadSuccess: 多任务下载完毕");
            }

            @Override
            public void onDownloadListError(String msg) {
                Log.e(TAG, "onDownloadListError: --->" );
            }
        });
    }

    /**
     * 停止多任务
     */
    public void stopMany(View view){
        DownloadManager.get().cancelDownload(downloadInfoList);
    }

    /**
     * 开始列表任务
     */
    public void startList(View view){
        uploadAdapter.startDownload(adapterDownloadInfoList);
    }

    /**
     * 停止列表任务
     */
    public void stopList(View view){
        DownloadManager.get().cancelDownload(adapterDownloadInfoList);
    }

    /**
     * 删除列表文件
     */
    public void clear(View view){
        File[] files = externalFilesDir.listFiles();
        for (File file:files){
            boolean delete = file.delete();
            if(delete){
                Log.e(TAG, "clear: "+file.getName()+" success----->");
            }
        }
    }

}
