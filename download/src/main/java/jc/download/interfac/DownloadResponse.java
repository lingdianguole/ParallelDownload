package jc.download.interfac;

import jc.download.CallBack;
import jc.download.DownloadException;

public interface DownloadResponse {

    /** Response from connect step */
    void start();

    void connecting();

    void connected(long length, boolean isAcceptRanges);

    void connectFail(DownloadException e);

    void connectCancel();

    /** Response from data fetch step */
    void publishProgress(long finished, long length, int percent);

    void complete(String path);

    void pause();

    void cancel();

    void shelve();

    void fail(DownloadException e);

    void setCallback(CallBack callback);
}
