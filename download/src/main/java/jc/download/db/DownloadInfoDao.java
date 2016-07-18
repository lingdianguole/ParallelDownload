package jc.download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DownloadInfoDao extends  AbstractDao<DownloadInfo>{

    private static final String TABLE_NAME = DownloadInfo.class.getSimpleName();

    private final Object mutex = new Object();

    public DownloadInfoDao(Context context) {
        super(context);
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(_id integer primary key autoincrement, key text, name text, url text, finished long, length long, progress int, status int, path text)");
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_NAME);
    }

    public void insert(DownloadInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        synchronized (mutex) {
            db.insert(TABLE_NAME, null, info.getContentValues()); // If set nullColumnHack null, will insert fail due to empty values.
        }
    }

    public void delete(String key) {
        SQLiteDatabase db = getWritableDatabase();
        synchronized (mutex) {
            db.delete(TABLE_NAME, "key = ?", new String[]{key});
        }
    }

    private final ContentValues contentValues = new ContentValues();

    public void update(String key, long finished, int progress, int status) {
        SQLiteDatabase db = getWritableDatabase();
        synchronized (mutex) {
            contentValues.clear();
            contentValues.put("finished", finished);
            contentValues.put("progress", progress);
            contentValues.put("status", status);
            db.update(TABLE_NAME, contentValues, "key = ?", new String[]{key});
        }
    }

    public void update(String key, long length, int status) {
        SQLiteDatabase db = getWritableDatabase();
        synchronized (mutex) {
            contentValues.clear();
            contentValues.put("length", length);
            contentValues.put("status", status);
            db.update(TABLE_NAME, contentValues, "key = ?", new String[]{key});
        }
    }

    public void update(String key, int status) {
        SQLiteDatabase db = getWritableDatabase();
        synchronized (mutex) {
            contentValues.clear();
            contentValues.put("status", status);
            db.update(TABLE_NAME, contentValues, "key = ?", new String[]{key});
        }
    }

    public DownloadInfo getDownloadInfo(String key) {
        List<DownloadInfo> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "
                        + TABLE_NAME
                        + " where key = ?",
                new String[]{key});
        while (cursor.moveToNext()) {
            DownloadInfo info = new DownloadInfo();
            info.setKey(cursor.getString(cursor.getColumnIndex("key")));
            info.setName(cursor.getString(cursor.getColumnIndex("name")));
            info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            info.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));
            info.setLength(cursor.getLong(cursor.getColumnIndex("length")));
            info.setProgress(cursor.getInt(cursor.getColumnIndex("progress")));
            info.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
            info.setPath(cursor.getString(cursor.getColumnIndex("path")));
            list.add(info);
        }
        cursor.close();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public boolean exists(String key) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "
                        + TABLE_NAME
                        + " where key = ?",
                new String[]{key});
        boolean isExists = cursor.moveToNext();
        cursor.close();
        return isExists;
    }

}
