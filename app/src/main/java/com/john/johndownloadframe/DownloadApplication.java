package com.john.johndownloadframe;

import android.app.Application;
import com.john.breakpoint.greendao.helper.GreenDaoManager;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/9/27 15:30
 * <p/>
 * Description:
 */
public class DownloadApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        GreenDaoManager.initDatabase(this);
    }
}
