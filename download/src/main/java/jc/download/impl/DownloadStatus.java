package jc.download.impl;

import jc.download.CallBack;
import jc.download.DownloadException;

public class DownloadStatus {

    public static final int INIT         = 0; // Just create.
    public static final int STARTED      = 1; // Stated, maybe continued after paused.
    public static final int CONNECTING   = 2; // Connecting for getting download info, such as: file size, accept ranges?
    public static final int CONNECTED    = 3; // Got download info.
    public static final int PROGRESS     = 4; // Downloading.
    public static final int COMPLETED    = 5; // Complete download.
    public static final int PAUSED       = 6; // Paused by user.
    public static final int CANCELED     = 7; // Canceled by user.
    public static final int FAILED       = 8; // Failed due to exception.
    public static final int SHELVED      = 9; // Shelved due to too much requests.


    private int status;
    private long time;
    private long length;
    private long finished;
    private int percent;
    private boolean acceptRanges;
    private String path;
    private DownloadException exception;

    private CallBack callBack;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public boolean isAcceptRanges() {
        return acceptRanges;
    }

    public void setAcceptRanges(boolean acceptRanges) {
        this.acceptRanges = acceptRanges;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public Exception getException() {
        return exception;
    }

    public void setException(DownloadException exception) {
        this.exception = exception;
    }

    public CallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }
}
