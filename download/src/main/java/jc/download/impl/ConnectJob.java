package jc.download.impl;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import jc.download.DownloadException;
import jc.download.executor.Priority;
import jc.download.interfac.ConnectConfig;
import jc.download.interfac.ConnectListener;
import jc.download.interfac.ConnectRunnable;
import jc.download.interfac.ExceptionCode;

public class ConnectJob implements ConnectRunnable {

    private final static String TAG = "ConnectJob";
    private final static boolean DEBUG = true;
    private final String url;
    private final ConnectListener listener;
    private volatile int status;
    Priority priority;
    long start, end;

    public ConnectJob(String url, Priority priority, ConnectListener listener) {
        this.url = url;
        this.priority = priority;
        this.listener = listener;
    }

    public void cancel() {
        status = DownloadStatus.CANCELED;
    }

    @Override
    public void shelve() {
        status = DownloadStatus.SHELVED;
    }

    public boolean isConnecting() {
        return DownloadStatus.CONNECTING == status;
    }

    public boolean isConnected() {
        return DownloadStatus.CONNECTED == status;
    }

    public boolean isCanceled() {
        return DownloadStatus.CANCELED == status;
    }

    public boolean isFailed() {
        return DownloadStatus.FAILED == status;
    }

    public void run() {
        if (DEBUG) {
            start = System.currentTimeMillis();
        }
        status = DownloadStatus.CONNECTING;
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        listener.onConnecting();
        try {
            connect();
        } catch (DownloadException e) {
            handle(e);
        }

    }

    private void connect() throws DownloadException {
        final URL url;
        try {
            url = new URL(this.url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new DownloadException(ExceptionCode.ERR_URL, "Illegal url.");
        }
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(ConnectConfig.CONNECT_TIME_OUT);
            conn.setReadTimeout(ConnectConfig.READ_TIME_OUT);
            conn.setRequestProperty("Range", "bytes=" + 0 + "-");
            final int responseCode = conn.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    parse(conn, false);
                    break;
                case HttpURLConnection.HTTP_PARTIAL:
                    parse(conn, true);
                    break;
                default:
                    throw new DownloadException(ExceptionCode.ERR_CONN_RESPONSE, "Wrong response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(ExceptionCode.ERR_CONN_IO, "IO exception.");
        }

    }

    private void parse(HttpURLConnection conn, boolean acceptRanges) throws DownloadException {
        final long length;
        String contentLength = conn.getHeaderField("Content-Length");
        if (TextUtils.isEmpty(contentLength) || "0".equals(contentLength) || "-1".equals(contentLength)) {
            length = conn.getContentLength();
        } else {
            length = Long.parseLong(contentLength);
        }
        if (length <= 0) {
            throw new DownloadException(ExceptionCode.ERR_CONN_LEN, "Invalid content length");
        }

        checkJobStatus(); // Check if job has been canceled?

        status = DownloadStatus.CONNECTED;
        listener.onConnected(length, acceptRanges);
        if (DEBUG) {
            end = System.currentTimeMillis();
            Log.d(TAG, "Connect costs time: " + (end - start) + " ms");
        }
    }

    private void checkJobStatus() throws DownloadException {
        if (isCanceled()) {
            throw new DownloadException(ExceptionCode.ERR_CONN_CANCEL, "Download paused!");
        }
    }
    private void handle(DownloadException e) {
        int code = e.getCode();
        switch (code) {
            case ExceptionCode.ERR_URL:
            case ExceptionCode.ERR_CONN_IO:
            case ExceptionCode.ERR_CONN_LEN:
            case ExceptionCode.ERR_CONN_RESPONSE:
                synchronized (listener) {
                    status = DownloadStatus.FAILED;
                    listener.onConnectFail(e);
                }
                break;
            case ExceptionCode.ERR_CONN_CANCEL:
                synchronized (listener) {
                    status = DownloadStatus.CANCELED;
                    listener.onConnectCancel();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown error code");

        }
    }

    @Override
    public int getPriority() {
        return priority.ordinal();
    }
}
