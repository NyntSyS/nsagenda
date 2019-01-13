package com.dam.nestor_samuel.nsagenda;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class DatabaseAcceleroDodge extends SQLiteOpenHelper {

    private String mainTable = "CREATE TABLE RECORDS (" +
            "nick TEXT, " +
            "puntuacion INT)";

    public DatabaseAcceleroDodge(@Nullable Context context, @Nullable String name,
                    @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(mainTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
