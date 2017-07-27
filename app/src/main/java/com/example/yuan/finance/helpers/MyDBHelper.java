package com.example.yuan.finance.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YUAN on 2017/04/12.
 */

public class MyDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expense.db";
    private static final int DATABASE_VERSION = 1;
    final String TABLE_NAME = "expense";
    final String _ID = "_id";
    final String DATE = "date";
    final String AMOUNT = "amount";
    final String COMMENT = "comment";

    private static MyDBHelper mInstance;
    public static MyDBHelper getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new MyDBHelper(context);
        }
        return mInstance;
    }

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "  + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DATE + " DATE NOT NULL, " +
            AMOUNT + " INTEGER NOT NULL, " +
            COMMENT    + " TEXT);";

        db.execSQL(CREATE_TABLE);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
