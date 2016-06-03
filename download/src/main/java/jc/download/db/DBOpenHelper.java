package jc.download.db;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download.db";

    public DBOpenHelper(Context context) throws NameNotFoundException {
        super(context, DB_NAME, null, context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionCode);
    }

    void createTable(SQLiteDatabase db){
        ThreadInfoDao.createTable(db);
        DownloadInfoDao.createTable(db);
    }

    void dropTable(SQLiteDatabase db){
        ThreadInfoDao.dropTable(db);
        DownloadInfoDao.dropTable(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db);
        createTable(db);
    }
}
