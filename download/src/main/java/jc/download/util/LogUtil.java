package jc.download.util;

import android.util.Log;

//import jc.download.BuildConfig;

public class LogUtil {

    public final static String TAG = "DownloadActivity";

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }


    public static void d(String tag, String msg) {
//        if (BuildConfig.isDebug) {
            Log.d(tag, msg);
//        }
    }

    public static void d(String msg) {
//        if (BuildConfig.isDebug) {
            Log.d(TAG, msg);
//        }
    }
}
