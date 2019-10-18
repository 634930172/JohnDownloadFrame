package com.john.breakpoint.greendao.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


import com.john.breakpoint.greendao.dao.DBDownloadInfoDao;
import com.john.breakpoint.greendao.dao.DaoMaster;

import org.greenrobot.greendao.database.Database;

/**
 * Author: John
 * E-mail：634930172@qq.com
 * Date: 2018/2/2 10:20
 * Description:数据库管理类
 */

public class GreenDaoOpenHelper extends DaoMaster.OpenHelper {


    public GreenDaoOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //将要转移的表添加进去
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                DaoMaster.createAllTables(db, ifNotExists);
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                DaoMaster.dropAllTables(db, ifExists);
            }//中间新加了StudentDao
        }, DBDownloadInfoDao.class);
    }
}

