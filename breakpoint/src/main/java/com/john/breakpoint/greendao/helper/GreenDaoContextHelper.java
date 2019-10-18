package com.john.breakpoint.greendao.helper;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;


import com.john.breakpoint.greendao.util.DBPathUtil;

import java.io.File;


/**
 * Author: John
 * E-mail：634930172@qq.com
 * Date: 2018/2/2 9:27
 * Description:设置数据库存储路径
 */

public class GreenDaoContextHelper extends ContextWrapper {

    private Context context;
    private String dbDir;
    public GreenDaoContextHelper(Context context,String dbDir) {
        super(context);
        this.context = context;
        this.dbDir=dbDir;
    }

    @Override
    public File getDatabasePath(String name) {
        File dbFile= DBPathUtil.getIndividualCacheDirectory(context,dbDir,name);
        if(dbFile!=null){
            return dbFile;
        }else {
            return super.getDatabasePath(name);
        }

    }


    /**
     * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
    }

    /**
     * Android 4.0会调用此方法获取数据库。
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
    }

}