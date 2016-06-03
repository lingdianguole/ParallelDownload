package jc.download.interfac;

import jc.download.DownloadException;

public interface ConnectListener {

    void onConnecting();

    /**
     * @param length Total length of content.
     * @param isAcceptRanges If downloader work can be posted to several threads.
     */
    void onConnected(long length, boolean isAcceptRanges);

    void onConnectCancel();

    void onConnectFail(DownloadException e);
}
