package jc.download.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.Map;

import jc.download.db.DownloadInfo;
import jc.download.db.ThreadInfo;
import jc.download.executor.Priority;
import jc.download.interfac.DataFetchListener;
import jc.download.interfac.DownloadConfiguration;

public class SingleDataFetchJob extends  DataFetchJob {
    public SingleDataFetchJob(DownloadInfo downloadInfo, ThreadInfo threadInfo, Priority priority, DownloadConfiguration config, DataFetchListener listener) {
        super(downloadInfo, threadInfo, priority, config, listener);
    }

    @Override
    protected void insert(ThreadInfo info) {
        // do nothing.
    }

    @Override
    protected int getResponseCode() {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected void update(ThreadInfo info) {
        // do nothing.
    }

    @Override
    protected void delete(String key, int threadId) {
        // do nothing.
    }

    @Override
    protected void replace(ThreadInfo newInfo) {
        // do nothing.
    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) {
        return null;
    }

    @Override
    protected RandomAccessFile getFile(File dir, String name, long offset) throws IOException {
        File file = new File(dir, name);
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(0);
        return raf;
    }

    @Override
    protected String getTag() {
        return this.getClass().getSimpleName();
    }
}
