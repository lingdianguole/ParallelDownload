package jc.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ThreadInfoDao extends AbstractDao<ThreadInfo> {

    private static final String TABLE_NAME = ThreadInfo.class.getSimpleName();

    public ThreadInfoDao(Context context) {
        super(context);
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(_id integer primary key autoincrement, id integer, key text, url text, start long, end long, finished long)");
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_NAME);
    }

    public void insert(ThreadInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("insert into "
                        + TABLE_NAME
                        + "(id, key, url, start, end, finished) values(?, ?, ?, ?, ?, ?)",
                new Object[]{info.getId(), info.getKey(), info.getUrl(), info.getStart(), info.getEnd(), info.getFinished()});
    }

    public void delete(String key) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "
                        + TABLE_NAME
                        + " where key = ?",
                new Object[]{key});
    }

    public void delete(String key, int threadId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "
                        + TABLE_NAME
                        + " where key = ? and id = ?",
                new Object[]{key, threadId});
    }

    public void update(String key, int threadId, long finished) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update "
                        + TABLE_NAME
                        + " set finished = ?"
                        + " where key = ? and id = ? ",
                new Object[]{finished, key, threadId});
    }

    public List<ThreadInfo> getThreadInfos(String key) {
        List<ThreadInfo> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from "
                        + TABLE_NAME
                        + " where key = ? order by id desc",
                new String[]{key});
        while (cursor.moveToNext()) {
            ThreadInfo info = new ThreadInfo();
            info.setId(cursor.getInt(cursor.getColumnIndex("id")));
            info.setKey(cursor.getString(cursor.getColumnIndex("key")));
            info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            info.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
            info.setStart(cursor.getLong(cursor.getColumnIndex("start")));
            info.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public boolean exists(String key, int threadId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "
                        + TABLE_NAME
                        + " where key = ? and id = ?",
                new String[]{key, threadId + ""});
        boolean isExists = cursor.moveToNext();
        cursor.close();
        return isExists;
    }

}
