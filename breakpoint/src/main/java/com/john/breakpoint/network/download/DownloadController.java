package com.john.breakpoint.network.download;

import com.john.breakpoint.network.DownloadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/10/28 16:15
 * <p/>
 * Description:每个下载任务的控制器
 */
public class DownloadController {

    /**
     * 控制每个任务的进度信息
     */
    public ConcurrentHashMap<Integer, String> partInfoMap=new ConcurrentHashMap<>();
    /**
     * 统计任务的完成个数
     */
    public AtomicInteger atomicInteger = new AtomicInteger(0);
    /**
     * 统计任务的总读取字节数
     */
    public AtomicLong atomicLong = new AtomicLong(0);

    /**
     * 任务执行器
     */
    public ExecutorService executorService;

    /**
     * 选取回调进度的线程Tag
     */
    public int readTag;


    public List<DownloadTask> tasks=new ArrayList<>();


}
