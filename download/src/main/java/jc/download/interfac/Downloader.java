package jc.download.interfac;

import jc.download.db.DownloadInfo;

public interface Downloader {

    void start();

    void pause();

    void cancel();

    void destroy();

    boolean isPreparing();

    boolean isRunning();

    boolean isCompleted();

    boolean isFailed();

    boolean isPaused();

    boolean isCanceled();

    boolean isShelved();

    void shelve();

    Key getKey();
    DownloadResponse getResponse();

    void enableProgress(); // Enable progress update, due to sluggish issue when UI render.
    void disableProgress(); // Disable progress update, due to sluggish issue when UI render.

    /** update db */
    void update(DownloadInfo info);
    void updateStatus(String key, int status);
    void updateLength(String key, long length, int status);
    void delete(String key);

}
