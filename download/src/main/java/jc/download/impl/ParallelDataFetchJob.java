package jc.download.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import jc.download.db.DataBaseManager;
import jc.download.db.DownloadInfo;
import jc.download.db.ThreadInfo;
import jc.download.executor.Priority;
import jc.download.interfac.DataFetchListener;
import jc.download.interfac.DownloadConfiguration;
import jc.download.util.Util;

public class ParallelDataFetchJob extends  DataFetchJob {

    private DataBaseManager mDBManager;

    public ParallelDataFetchJob(DownloadInfo downloadInfo, ThreadInfo threadInfo, DataBaseManager dbManager, Priority priority, DownloadConfiguration config, DataFetchListener listener) {
        super(downloadInfo, threadInfo, priority, config, listener);
        this.mDBManager = dbManager;
    }


    @Override
    protected void insert(ThreadInfo info) {
        if (!mDBManager.exists(info.getKey(), info.getId())) {
            mDBManager.insert(info);
        } else if (downloadInfo.getObsolete()) {
            replace(info);
        }
    }

    @Override
    protected void delete(String key, int threadId) {
        mDBManager.delete(key, threadId);
    }

    @Override
    protected void replace(ThreadInfo newInfo) {
        mDBManager.delete(newInfo.getKey(), newInfo.getId());
        mDBManager.insert(newInfo);
    }

    @Override
    protected int getResponseCode() {
        return HttpURLConnection.HTTP_PARTIAL;
    }

    @Override
    protected void update(ThreadInfo info) {
        mDBManager.update(info.getKey(), info.getId(), info.getFinished());
    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) {
        Map<String, String> headers = new HashMap<>();
        long start = info.getStart() + info.getFinished();
        long end = info.getEnd();
        headers.put("Range", "bytes=" + start + "-" + end);
        return headers;
    }

    @Override
    protected RandomAccessFile getFile(File dir, String name, long offset) throws IOException {
        File file = new File(dir, name);
        Util.chmod(file.getAbsolutePath());
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(offset);
        return raf;
    }


    @Override
    protected String getTag() {
        return this.getClass().getSimpleName();
    }
}
