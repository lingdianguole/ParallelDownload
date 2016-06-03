package jc.download.impl;

import jc.download.CallBack;
import jc.download.DownloadException;
import jc.download.interfac.DownloadResponse;
import jc.download.interfac.DownloadStatusDelivery;

public class DownloadResponseImpl implements DownloadResponse {

    private DownloadStatusDelivery delivery;
    private DownloadStatus downloadStatus;

    public DownloadResponseImpl(DownloadStatusDelivery delivery, CallBack callBack) {
        this.delivery = delivery;
        downloadStatus = new DownloadStatus();
        downloadStatus.setCallBack(callBack);
    }

    @Override
    public void start() {
        downloadStatus.setStatus(DownloadStatus.STARTED);
        CallBack callBack = downloadStatus.getCallBack();
        if (callBack != null) {
            callBack.onStarted();
        }
    }
    @Override
    public void connecting() {
        downloadStatus.setStatus(DownloadStatus.CONNECTING);
        post();
    }

    @Override
    public void connectCancel() {
        downloadStatus.setStatus(DownloadStatus.CANCELED);
        post();
    }

    @Override
    public void connectFail(DownloadException e) {
        downloadStatus.setException(e);
        downloadStatus.setStatus(DownloadStatus.FAILED);
        post();
    }

    @Override
    public void connected(long length, boolean isAcceptRanges) {
        downloadStatus.setLength(length);
        downloadStatus.setAcceptRanges(isAcceptRanges);
        downloadStatus.setStatus(DownloadStatus.CONNECTED);
        post();
    }



    @Override
    public void publishProgress(long finished, long length, int percent) {
        downloadStatus.setFinished(finished);
        downloadStatus.setLength(length);
        downloadStatus.setPercent(percent);
        downloadStatus.setStatus(DownloadStatus.PROGRESS);
        post();
    }

    @Override
    public void pause() {
        downloadStatus.setStatus(DownloadStatus.PAUSED);
        post();
    }

    @Override
    public void shelve() {
        downloadStatus.setStatus(DownloadStatus.SHELVED);
        post();
    }

    @Override
    public void cancel() {
        downloadStatus.setStatus(DownloadStatus.CANCELED);
        post();
    }

    @Override
    public void fail(DownloadException e) {
        downloadStatus.setException(e);
        downloadStatus.setStatus(DownloadStatus.FAILED);
        post();
    }

    @Override
    public void complete(String path) {
        downloadStatus.setStatus(DownloadStatus.COMPLETED);
        downloadStatus.setPath(path);
        post();
    }

    private void post() {
        delivery.post(downloadStatus);
    }

    @Override
    public void setCallback(CallBack callback) {
        downloadStatus.setCallBack(callback);
    }
}
