package com.program.mynotebook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/06/02.
 *
 */

public class ListData extends SQLiteOpenHelper {

    public static final String TABLE_NAME_NOTES = "notes";
    public static final String TABLE_COLUMN_NAME_ID = "_id";
    public static final String TABLE_COLUMN_NAME_TIME = "time";
    public static final String TABLE_COLUMN_NAME_TITLE = "title";
    public static final String TABLE_COLUMN_NAME_CONTENT = "note";
    public static final String TABLE_COLUMN_NAME_MEDIA_PATH = "path";



    //我们只需要传递进来一个context就好了
    public ListData(Context context) {
        super(context, "notes", null, 1);
    }

    //当第一次打开数据库，表不存在时调用，以创建表
    //创建两行表
    //第一张表 保存notes信息
    //第二张表 保存note对应的media信息 （图片、视频） 【可加入音频】
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_NOTES + "(" + TABLE_COLUMN_NAME_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + TABLE_COLUMN_NAME_TITLE
                + " TEXT NOT NULL DEFAULT \"\"," + TABLE_COLUMN_NAME_CONTENT
                + " TEXT NOT NULL DEFAULT \"\"," + TABLE_COLUMN_NAME_MEDIA_PATH
                + " TEXT NOT NULL DEFAULT \"\"," + TABLE_COLUMN_NAME_TIME
                + " TEXT NOT NULL DEFAULT \"\"" + ")");
    }

    //更新时执行
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
