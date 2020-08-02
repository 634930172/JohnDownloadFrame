package com.john.johndownloadframe;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.john.breakpoint.DownloadManager;
import com.john.breakpoint.greendao.util.FileSizeUtil;
import com.john.breakpoint.network.download.DownloadInfo;
import com.john.breakpoint.network.download.DownloadStateListener;

import java.util.List;


/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/9/23 14:58
 * <p/>
 * Description:
 */
public class UploadadAdapter extends BaseAdapter {

    private Activity activity;
    private List<DownloadInfo> downloadInfoList;
    private static final String TAG = "DownloadAdapter";
    private boolean isFirstDownload;

    public UploadadAdapter(Activity activity) {
        this.activity = activity;
    }

    public void startDownload(List<DownloadInfo> downloadInfoList) {
        Log.e(TAG, "startDownload: 刷新列表数据------>" );
        this.downloadInfoList = downloadInfoList;
        isFirstDownload=true;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (downloadInfoList == null || downloadInfoList.isEmpty()) {
            return 0;
        }
        return downloadInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        if (downloadInfoList == null || downloadInfoList.isEmpty()) {
            return null;
        }
        return downloadInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_download_layout, null);
            holder = new ViewHolder();
            holder.progressText = convertView.findViewById(R.id.progress_text);
            holder.start = convertView.findViewById(R.id.item_start);
            holder.stop = convertView.findViewById(R.id.item_stop);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final DownloadInfo downloadInfo = downloadInfoList.get(position);
        if(isFirstDownload){
            Log.e(TAG, "getView: 开始下载----->"+downloadInfo.getFileName() );
            download(downloadInfo, holder);
        }
        holder.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: ------>" + position);
                download(downloadInfo, holder);
            }
        });

        holder.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + position);
                DownloadManager.get().cancelDownload(downloadInfo);

            }
        });
        if(position==downloadInfoList.size()-1){
            isFirstDownload=false;
        }
        return convertView;
    }


    static class ViewHolder {
        TextView progressText;
        Button start, stop;
    }

    //修复列表下载进度回调第一个item不显示的问题
    private void download(final DownloadInfo downloadInfo, final ViewHolder holder) {
        DownloadManager.get().download(activity,downloadInfo, new DownloadStateListener() {

            @Override
            public void onDownloadStart(String description) {
                Log.e(TAG, "onDownloadStart: --->"+description );
            }

            @Override
            public void onDownloading(String saveFileName, long progress, long total) {
                holder.progressText.setText(activity.getString(R.string.current_progress,saveFileName,
                        FileSizeUtil.FormatFileSize(progress), FileSizeUtil.FormatFileSize(total)));
            }

            @Override
            public void onDownloadSuccess(String description) {
                Log.e(TAG, "onDownloadSuccess: ------>"+description+" | "+(Looper.getMainLooper()==Looper.myLooper()));
                holder.progressText.setText(activity.getString(R.string.download_success));
            }

            @Override
            public void onDownloadError(String msg) {

            }
        });
    }


}
