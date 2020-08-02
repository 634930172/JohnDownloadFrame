package com.john.johndownloadframe;

import android.app.Application;
import com.john.breakpoint.greendao.helper.GreenDaoManager;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/9/27 15:30
 * <p/>
 * Description:
 */
public class MyApplication extends Application {

    public static MyApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
        GreenDaoManager.initDatabase(this);
        //bugly 测试时true
        CrashReport.initCrashReport(getApplicationContext(), "a9a7c439c8", true);
    }
}
