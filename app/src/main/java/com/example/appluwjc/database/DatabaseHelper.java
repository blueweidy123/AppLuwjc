package com.example.appluwjc.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import com.example.appluwjc.dao.tabDAO;
import com.example.appluwjc.adapters.GridViewAdapter;
import androidx.annotation.Nullable;

import com.example.appluwjc.adapters.GridViewAdapter;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;

    private static final String DATABASE_NAME = "appluwc.db";

//    private static final String TABLE_URL = "URLS";
    private static final String TABS_TABLE = "tabs";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    public void updateUrl(int tabId, String newUrlValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("URL", newUrlValue);
        db.update("+TABS_TABLE+", values, "tabID=?", new String[]{String.valueOf(tabId)});
        db.close();
    }

    public void insertTab(String url, String title, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("URL", url);
        values.put("title", title);
        values.put("date", date);
        long result = db.insert(TABS_TABLE, null, values);
        if (result == -1){
            Toast.makeText(context, "insert tab fail", Toast.LENGTH_SHORT).show();
        }else{
//            Toast.makeText(context, "insert "+url, Toast.LENGTH_SHORT).show();
            tabDAO tabDAO = new tabDAO(context);
            GridViewAdapter adapter = new GridViewAdapter(context, tabDAO.getAllTabs());
            adapter.notifyDataSetChanged();
        }
        db.close();
    }

    public void delete(int id){
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DELETE FROM "+TABS_TABLE+" WHERE tabID = '"+ id +"'");
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CreateTable = "CREATE TABLE IF NOT EXISTS "+TABS_TABLE+" (tabID INTEGER PRIMARY KEY AUTOINCREMENT, URL text NOT NULL, title text NOT NULL, date text NOT NULL)";
        sqLiteDatabase.execSQL(CreateTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABS_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void createUrlTable(){
        SQLiteDatabase database = getWritableDatabase();
        String CreateTable = "CREATE TABLE IF NOT EXISTS \"+TABS_TABLE+\" (tabID INTEGER PRIMARY KEY AUTOINCREMENT, URL text NOT NULL, title text NOT NULL, date text NOT NULL)";
        database.execSQL(CreateTable);
    }

    public void deleteTable(){
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS "+ TABS_TABLE);
    }
}
