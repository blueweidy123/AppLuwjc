package com.example.appluwjc.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.appluwjc.database.DatabaseHelper;
import com.example.appluwjc.models.Tab;

import java.util.ArrayList;

public class tabDAO extends DatabaseHelper{
    private static final String TABS_TABLE = "tabs";

    public tabDAO(@Nullable Context context) {
        super(context);
    }

    public ArrayList<Tab> getAllTabs() {
        ArrayList<Tab> tabList = new ArrayList<Tab>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABS_TABLE+" order by date desc", null);

        if (cursor.moveToFirst()) {
            do {
                int tabID = cursor.getInt(0);
                String url = cursor.getString(1);
                String title = cursor.getString(2);
                String date = cursor.getString(3);

//                Tab tab = new Tab(tabID, url, date);
                Tab tab = new Tab(tabID, title, url, date);
                tabList.add(tab);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return tabList;
    }


}
