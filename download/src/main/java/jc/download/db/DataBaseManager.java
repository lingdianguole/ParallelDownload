package jc.download.db;

import android.content.Context;

import java.util.List;

import jc.download.impl.Snapshoot;

public class DataBaseManager {
    private static DataBaseManager INSTANCE;
    private final ThreadInfoDao mThreadInfoDao;
    private final DownloadInfoDao mDownloadInfoDao;

    public static DataBaseManager getInstance(Context context) {
        if (null == INSTANCE) {
            synchronized (DataBaseManager.class) {
                if (null == INSTANCE) {
                    INSTANCE = new DataBaseManager(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private DataBaseManager(Context context) {
        mThreadInfoDao = new ThreadInfoDao(context);
        mDownloadInfoDao = new DownloadInfoDao(context);
    }

    public synchronized void insert(ThreadInfo threadInfo) {
        mThreadInfoDao.insert(threadInfo);
    }

    public synchronized void insert(DownloadInfo downloadInfo) {
        mDownloadInfoDao.insert(downloadInfo);
    }

    public synchronized void delete(String key, int threadId) {
        mThreadInfoDao.delete(key, threadId);
    }

    public synchronized void delete(String key) {
        mThreadInfoDao.delete(key); /** NOTICE */
        mDownloadInfoDao.delete(key);
    }

    public synchronized void update(String key, int threadId, long finished) {
        mThreadInfoDao.update(key, threadId, finished);
    }

    public synchronized void update(String key, int status) {
        mDownloadInfoDao.update(key, status);
    }

    public synchronized void update(String key, long length, int status) {
        mDownloadInfoDao.update(key, length, status);
    }

    public synchronized void update(String key, long finished, int progress, int status) {
        mDownloadInfoDao.update(key, finished, progress, status);
    }

    public List<ThreadInfo> getThreadInfos(String key) {
        return mThreadInfoDao.getThreadInfos(key);
    }

    public DownloadInfo getDownloadInfo(String key) {
        return mDownloadInfoDao.getDownloadInfo(key);
    }


    public Snapshoot getSnapshoot(String key) {
        DownloadInfo downloadInfo = mDownloadInfoDao.getDownloadInfo(key);
        Snapshoot snapshoot = null;
        if (downloadInfo != null) {
            snapshoot  = new Snapshoot();
            snapshoot.status = downloadInfo.getStatus();
            snapshoot.length = downloadInfo.getLength();
            snapshoot.progress = downloadInfo.getProgress();
            snapshoot.path = downloadInfo.getPath();
        }
        return snapshoot;
    }

    public boolean exists(String key, int threadId) {
        return mThreadInfoDao.exists(key, threadId);
    }

    public boolean exists(String key) {
        return mDownloadInfoDao.exists(key);
    }
}
