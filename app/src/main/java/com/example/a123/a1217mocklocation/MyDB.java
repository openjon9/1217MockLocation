package com.example.a123.a1217mocklocation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 123 on 2018/2/24.
 */

public class MyDB extends SQLiteOpenHelper {
    private static final String TB_NAME = "mocklocation";
    private static final String CREATEDB = "create table if not exists " + TB_NAME + "(_id integer primary key autoincrement, markpoint text not null, lat text not null, lot text not null)";

    public MyDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATEDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
            onCreate(db);
        }
    }
}
