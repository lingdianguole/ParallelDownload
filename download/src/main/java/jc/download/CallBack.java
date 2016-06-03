package jc.download;

/**
 * CallBack of download status
 */
public interface CallBack {

    void onStarted();

    void onConnecting();

    void onConnected(long total, boolean isRangeSupport);

    void onProgress(long finished, long total, int progress);

    void onComplete(String path);

    void onPause();

    void onCancel();

    void onFail(DownloadException e);

    void onShelve();
}
