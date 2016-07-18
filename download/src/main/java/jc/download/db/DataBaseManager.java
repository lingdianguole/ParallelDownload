package jc.download.db;

import android.content.Context;

import java.util.List;

import jc.download.impl.Snapshoot;

public class DataBaseManager {

    private ThreadInfoDao mThreadInfoDao;
    private DownloadInfoDao mDownloadInfoDao;

    private static class SingletonHolder {
        static DataBaseManager INSTANCE = new DataBaseManager();
    }

    public static DataBaseManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Context context) {
        mThreadInfoDao = new ThreadInfoDao(context);
        mDownloadInfoDao = new DownloadInfoDao(context);
    }

    public void insert(ThreadInfo threadInfo) {
        mThreadInfoDao.insert(threadInfo);
    }

    public void insert(DownloadInfo downloadInfo) {
        mDownloadInfoDao.insert(downloadInfo);
    }

    public void delete(String key, int threadId) {
        mThreadInfoDao.delete(key, threadId);
    }

    public void delete(String key) {
        mThreadInfoDao.delete(key); /** NOTICE */
        mDownloadInfoDao.delete(key);
    }

    public void update(String key, int threadId, long finished) {
        mThreadInfoDao.update(key, threadId, finished);
    }

    public void update(String key, int status) {
        mDownloadInfoDao.update(key, status);
    }

    public void update(String key, long length, int status) {
        mDownloadInfoDao.update(key, length, status);
    }

    public void update(String key, long finished, int progress, int status) {
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
