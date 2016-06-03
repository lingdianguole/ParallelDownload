package jc.download.interfac;

import jc.download.DownloadException;

public interface DataFetchListener {

    void onConnecting();

    void onProgress(long finished, long length);

    void onComplete();

    void onPause();

    void onShelve();

    void onCancel();

    void onFail(DownloadException e);
}
