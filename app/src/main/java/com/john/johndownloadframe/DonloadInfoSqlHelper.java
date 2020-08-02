package com.john.johndownloadframe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

/**
 * Author: John
 * E-mail: 634930172@qq.com
 * Date: 2019/10/29 19:18
 * <p/>
 * Description:
 */
public class DonloadInfoSqlHelper extends SQLiteOpenHelper {


    public DonloadInfoSqlHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
