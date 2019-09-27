package com.john.johndownloadframe;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.john.johndownloadframe.network.AppService;
import com.john.johndownloadframe.network.download.DownLoadClient;
import com.john.johndownloadframe.network.download.DownloadObserver;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/9/23 14:58
 * <p/>
 * Description:
 */
public class DownloadAdapter extends BaseAdapter {

    private RxAppCompatActivity activity;
    private List<String> urlList;
    private static final String TAG="DownloadAdapter";
    private File externalFilesDir;//外部存储的私有目录，应用删除后此文件也会被删除

    public DownloadAdapter(RxAppCompatActivity activity) {
        this.activity = activity;
        this.externalFilesDir=this.activity.getExternalFilesDir(null);
    }

    public void startDownload(List<String> urlList) {
        this.urlList = urlList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (urlList == null || urlList.isEmpty()) {
            return 0;
        }
        return urlList.size();
    }

    @Override
    public Object getItem(int position) {
        if (urlList == null || urlList.isEmpty()) {
            return null;
        }
        return urlList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView==null){
            convertView= LayoutInflater.from(activity).inflate(R.layout.item_download_layout,null);
            holder=new ViewHolder();
            holder.progressText=convertView.findViewById(R.id.progress_text);
            holder.start=convertView.findViewById(R.id.item_start);
            holder.stop=convertView.findViewById(R.id.item_stop);
            convertView.setTag(holder);
        }else {
           holder= (ViewHolder) convertView.getTag();
        }
        String fileName=urlList.get(position);
        String URL = "http://94.191.50.122/demo/testDownload";
        download(DownLoadClient.getService(AppService.class).download(URL),
                new DownloadObserver<ResponseBody>(activity,externalFilesDir.toString(), fileName) {
            @Override
            public void onDownloadStart() {
                Log.e(TAG, "onDownloadStart--->"+(Looper.myLooper()==Looper.getMainLooper()) );
            }

            @Override
            public void onDownloading(long progress, long total) {
                Log.e(TAG, "progress: " + FileSizeUtil.FormatFileSize(progress) +
                        "  total: " + FileSizeUtil.FormatFileSize(total) +
                        " | "+(Looper.myLooper()==Looper.getMainLooper()));
                holder.progressText.setText(activity.getString(R.string.current_progress,FileSizeUtil.FormatFileSize(progress)));

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

        return convertView;
    }


    static class ViewHolder {
        TextView progressText;
        Button start, stop;
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
                .compose(activity.<ResponseBody>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(downloadObserver);
    }

}
