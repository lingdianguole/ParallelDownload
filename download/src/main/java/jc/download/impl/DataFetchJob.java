package jc.download.impl;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import jc.download.DownloadException;
import jc.download.db.DownloadInfo;
import jc.download.db.ThreadInfo;
import jc.download.executor.Priority;
import jc.download.interfac.Command;
import jc.download.interfac.ConnectConfig;
import jc.download.interfac.DataFetchListener;
import jc.download.interfac.DataFetchRunnable;
import jc.download.interfac.DownloadConfiguration;
import jc.download.interfac.ExceptionCode;

public abstract class DataFetchJob implements DataFetchRunnable {
    private final static boolean DEBUG = true;
    private final static String TAG = "DataFetchJob";
    private DownloadConfiguration config;
    protected final DownloadInfo downloadInfo;
    private final ThreadInfo threadInfo;
    private final DataFetchListener listener;
    private Priority priority;
    private volatile int status;
    private volatile int cmd = Command.INVALID;

    public DataFetchJob(DownloadInfo downloadInfo, ThreadInfo threadInfo, Priority priority, DownloadConfiguration config, DataFetchListener listener) {
        this.downloadInfo = downloadInfo;
        this.threadInfo = threadInfo;
        this.priority = priority;
        this.config = config;
        this.listener = listener;
    }

    public void pause() {
        cmd = Command.PAUSE;
    }

    public void cancel() {
        cmd = Command.CANCEL;
    }

    public void shelve() {
        cmd = Command.SHELVE;
    }

    public boolean isRunning() {
        return DownloadStatus.PROGRESS == status;
    }


    public boolean isShelved() {
        return DownloadStatus.SHELVED == status;
    }

    public boolean isCanceled() {
        return DownloadStatus.CANCELED == status;
    }

    public boolean isFailed() {
        return DownloadStatus.FAILED == status;
    }

    public boolean isCompleted() {
        return DownloadStatus.COMPLETED == status;
    }

    public void run() {
        long start, end;
        if (DEBUG) {
            start = System.currentTimeMillis();
        }
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        insert(threadInfo);
        try {
            status = DownloadStatus.PROGRESS;
            fetch();
            synchronized (listener) {
                status = DownloadStatus.COMPLETED;
                listener.onComplete();
            }
        } catch (DownloadException e) {
            handle(e);
        }
        if (DEBUG) {
            end = System.currentTimeMillis();
            Log.d(TAG, "" + Thread.currentThread().getName() + " runs for " + (end - start) + " ms");
        }
    }

    private void fetch() throws DownloadException {
        final URL url;
        try {
            url = new URL(threadInfo.getUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new DownloadException(ExceptionCode.ERR_FETCH_URL, "Illegal url while fetching.");
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(ConnectConfig.CONNECT_TIME_OUT);
            conn.setReadTimeout(ConnectConfig.READ_TIME_OUT);
            setHttpHeader(getHttpHeaders(threadInfo), conn);
            final int responseCode = conn.getResponseCode();
            if (responseCode == getResponseCode()) {
                fetchData(conn);
            } else {
                throw new DownloadException(ExceptionCode.ERR_FETCH_RESPONSE, "Unknown response code " + responseCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(ExceptionCode.ERR_FETCH_IO, "IO error while connecting when fetching.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }


    }

    private void fetchData(HttpURLConnection conn) throws DownloadException {
        InputStream is = null;
        RandomAccessFile raf = null;
        try {

            try {
                is = conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                throw new DownloadException(ExceptionCode.ERR_FETCH_DATA_IO, "IO error while getting input stream.");
            }

            final long offset = threadInfo.getStart() + threadInfo.getFinished();
            try {
                raf = getFile(downloadInfo.getDir(), downloadInfo.getName(), offset);
            } catch (IOException e) {
                e.printStackTrace();
                throw new DownloadException(ExceptionCode.ERR_FETCH_FILE_IO, "IO error while getting random access file.");
            }

            getData(is, raf);

        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception ignored){}
            try {
                if (raf != null) {
                    raf.close();
                }
            }catch (Exception ignored){}
        }
    }

    /** Maybe need optimize */
    private void getData(InputStream is, RandomAccessFile raf) throws DownloadException  {
        final byte[] buffer = new byte[config.getBufferSize()];
        long last = System.currentTimeMillis();
        long elapsed;
        while (true) {
            checkJobStatus();
            int len;
            try {
                len = is.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                throw new DownloadException(ExceptionCode.ERR_FETCH_READ, "IO error while reading data.");
            }
            if (len == -1) {
                break;
            }
            try {
                raf.write(buffer, 0, len);
                threadInfo.setFinished(threadInfo.getFinished() + len); /** Need update db for a duration */
                synchronized (listener) {
                    downloadInfo.setFinished(downloadInfo.getFinished() + len);
                    elapsed = System.currentTimeMillis() - last;
                    if (elapsed >= config.getNotifyInterval()) {
                        update(threadInfo);
                        last = System.currentTimeMillis();
                        listener.onProgress(downloadInfo.getFinished(), downloadInfo.getLength());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new DownloadException(ExceptionCode.ERR_FETCH_WRITE, "IO error while writing to cache.");
            }
        }
    }

    private void checkJobStatus() throws DownloadException{
        if (Command.CANCEL == cmd) {
            // If need update db ?
            throw new DownloadException(ExceptionCode.ERR_FETCH_CANCEL, "fetch canceled by user.");
        } else if (Command.PAUSE == cmd) {
            cmd = Command.INVALID;
            update(threadInfo);
            throw new DownloadException(ExceptionCode.ERR_FETCH_PAUSE, "fetch paused by user.");
        } else if (Command.SHELVE == cmd) {
            cmd = Command.INVALID;
            update(threadInfo);
            throw new DownloadException(ExceptionCode.ERR_FETCH_SHELVE, "fetch shelved by download manager.");
        }
    }


    private void setHttpHeader(Map<String, String> headers, URLConnection connection) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }
    }

    private void handle(DownloadException e) {
        int code = e.getCode();
        switch (code) {
            case ExceptionCode.ERR_URL:
            case ExceptionCode.ERR_FETCH_URL:
            case ExceptionCode.ERR_FETCH_IO:
            case ExceptionCode.ERR_FETCH_DATA_IO:
            case ExceptionCode.ERR_FETCH_FILE_IO:
            case ExceptionCode.ERR_FETCH_READ:
            case ExceptionCode.ERR_FETCH_WRITE:
                status = DownloadStatus.FAILED;
                listener.onFail(e);
                break;
            case ExceptionCode.ERR_FETCH_CANCEL:
                status = DownloadStatus.CANCELED;
                listener.onCancel();
                break;
            case ExceptionCode.ERR_FETCH_PAUSE:
                status = DownloadStatus.PAUSED;
                listener.onPause();
                break;
            case ExceptionCode.ERR_FETCH_SHELVE:
                status = DownloadStatus.SHELVED;
                listener.onShelve();
                break;
            default:
                throw new IllegalArgumentException("Unknown error code");
        }
    }

    @Override
    public int getPriority() {
        return priority.ordinal();
    }

    protected abstract int getResponseCode();

    /** db operation */
    protected abstract void insert(ThreadInfo info);

    protected abstract void update(ThreadInfo info);

    protected abstract void replace(ThreadInfo newInfo);

    protected abstract void delete(String key, int threadId);

    protected abstract Map<String, String> getHttpHeaders(ThreadInfo info);

    protected abstract RandomAccessFile getFile(File dir, String name, long offset) throws IOException;

    protected abstract String getTag();
}