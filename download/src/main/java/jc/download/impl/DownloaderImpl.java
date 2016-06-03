package jc.download.impl;

import android.util.Log;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import jc.download.DownloadException;
import jc.download.db.DataBaseManager;
import jc.download.db.DownloadInfo;
import jc.download.db.ThreadInfo;
import jc.download.executor.Priority;
import jc.download.interfac.Command;
import jc.download.interfac.ConnectListener;
import jc.download.interfac.DataFetchListener;
import jc.download.interfac.DownloadConfiguration;
import jc.download.interfac.DownloadResponse;
import jc.download.interfac.Downloader;
import jc.download.interfac.DownloaderChangedListener;
import jc.download.interfac.ExceptionCode;
import jc.download.interfac.Key;

/** 1. Connect for getting download info.
 *  2. Fetch data。 */
public class DownloaderImpl implements Downloader, ConnectListener, DataFetchListener {
    private final static String TAG = "DownloaderImpl";
    private final static boolean DEBUG = true;
    private ExecutorService connectService;
    private ExecutorService dataFetchService;
    private DataBaseManager dataBaseManager;
    private DownloadInfo downloadInfo;
    private ConnectJob connectJob;
    private List<DataFetchJob> dataFetchJobList;
    private String key;
    private Key mKey;
    private DownloaderChangedListener listener;
    private DownloadRequest request;
    private DownloadResponse response;
    private int status;
    private Priority priority;
    private DownloadConfiguration mConfig;
    private int cmd = Command.INVALID;
    long start, connect, complete;

    public DownloaderImpl(DownloadRequest request,
                          DownloadResponse response,
                          ExecutorService connectService,
                          ExecutorService dataFetchService,
                          DataBaseManager dataBaseManager,
                          String key,
                          Key mKey,
                          Priority priority,
                          DownloadConfiguration config,
                          DownloaderChangedListener listener) {
        this.request = request;
        this.response = response;
        this.connectService = connectService;
        this.dataFetchService = dataFetchService;
        this.dataBaseManager = dataBaseManager;
        this.key = key;
        this.mKey = mKey;
        this.priority = priority;
        this.mConfig = config;
        this.listener = listener;

        downloadInfo = dataBaseManager.getDownloadInfo(key);
        if (downloadInfo == null) {
            downloadInfo = new DownloadInfo(key, request.getName(), request.getUrl(), request.getCacheDir());
            dataBaseManager.insert(downloadInfo);
        } else {
            downloadInfo.init(); // From db, need init.
        }
        dataFetchJobList = new LinkedList<>();
    }


    @Override
    public boolean isPreparing() {
        return DownloadStatus.INIT == status;
    }

    @Override
    public boolean isRunning() {
        return DownloadStatus.STARTED == status
                || DownloadStatus.CONNECTING    == status
                || DownloadStatus.CONNECTED     == status
                || DownloadStatus.PROGRESS      == status;
    }

    @Override
    public void start() {
        // Can skip connect step, but maybe cache's length is not the newest length.
//        if (downloadInfo.getStatus() >= DownloadStatus.CONNECTED) {
//            status = downloadInfo.getStatus();
//            fetch(downloadInfo.getLength(), true);
//        } else {
            if (DEBUG) {
                start = System.currentTimeMillis();
            }
            if (Command.PAUSE == cmd) {
                cmd = Command.INVALID;
                status = DownloadStatus.PAUSED;
                downloadInfo.setStatus(status);
                updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
                response.pause();
                destroy();
                return;
            } else if (Command.CANCEL == cmd){
                cmd = Command.INVALID;
                status = DownloadStatus.CANCELED;
                downloadInfo.setStatus(status);
                updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
                response.cancel();
                destroy();
            }else if (Command.SHELVE == cmd){
                cmd = Command.INVALID;
                status = DownloadStatus.SHELVED;
                downloadInfo.setStatus(status);
                updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
                response.shelve(); // Has been removed from LruCache.
                listener.onShelved(this);
            }
            status = DownloadStatus.STARTED;
            downloadInfo.setStatus(status);
            updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
            response.start();
            connect();
//        }
    }


    @Override
    public void pause() {
        if (isPreparing()) {
            cmd = Command.PAUSE;
        } else {
            if (connectJob != null) {
                connectJob.cancel();
            }
            for (DataFetchJob job : dataFetchJobList) {
                job.pause();
            }
        }
    }

    @Override
    public void cancel() {
        if (isPreparing()) {
            cmd = Command.CANCEL;
        } else {
            if (connectJob != null) {
                connectJob.cancel();
            }
            for (DataFetchJob job : dataFetchJobList) {
                job.cancel();
            }
        }
    }


    @Override
    public void shelve() {
        if (isPreparing()) {
            cmd = Command.SHELVE;
        } else {
            if (connectJob != null) {
                connectJob.shelve();
            }
            for (DataFetchJob job : dataFetchJobList) {
                job.shelve();
            }
        }
    }

    @Override
    public void destroy() {
        listener.onDestroy(mKey, this); // Tell download manager.
    }

    private void connect() {
        connectJob = new ConnectJob(request.getUrl(), priority, this);
        connectService.submit(connectJob);
    }

    @Override
    public void onConnecting() {
        status = DownloadStatus.CONNECTING;
        downloadInfo.setStatus(status);
        updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
        response.connecting();
    }

    @Override
    public void onConnected(long length, boolean isAcceptRanges) {
        if (DEBUG) {
            connect = System.currentTimeMillis();
        }
        status = DownloadStatus.CONNECTED;
        downloadInfo.setStatus(status);
        downloadInfo.setLength(length);
        updateLength(downloadInfo.getKey(), downloadInfo.getLength(), downloadInfo.getStatus());
        response.connected(length, isAcceptRanges);
        downloadInfo.setAcceptRanges(isAcceptRanges);
        downloadInfo.setLength(length);
        fetch(length, isAcceptRanges);
    }

    @Override
    public void onConnectCancel() {
        status = DownloadStatus.CANCELED;
        downloadInfo.setStatus(status);
        updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
        response.connectCancel();
        destroy();
    }

    @Override
    public void onConnectFail(DownloadException e) {
        status = DownloadStatus.CANCELED;
        downloadInfo.setStatus(status);
        updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
        response.connectFail(e);
        destroy();
    }

    @Override
    public void onProgress(long finished, long length) {
        status = DownloadStatus.PROGRESS;
        final int percent = (int)(finished * 100 / length);
        downloadInfo.setStatus(status);
        downloadInfo.setProgress(percent);
        update(downloadInfo);
        response.publishProgress(finished, length, percent);
    }

    @Override
    public void onCancel() {
        if (isCanceled()) {
            status = DownloadStatus.CANCELED;
            downloadInfo.setStatus(status);
            updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
            response.cancel();
            destroy();
        }

    }

    @Override
    public void onFail(DownloadException e) {
        if (isFailed()) {
            status = DownloadStatus.FAILED;
            downloadInfo.setStatus(status);
            updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
            response.fail(e);
            destroy();
        }
    }

    @Override
    public void onPause() {
        if (isPaused()) {
            status = DownloadStatus.PAUSED;
            downloadInfo.setStatus(status);
            updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
            response.pause();
            destroy();
        }
    }

    @Override
    public void onShelve() {
        if (isShelved()) {
            status = DownloadStatus.SHELVED;
            downloadInfo.setStatus(status);
            updateStatus(downloadInfo.getKey(), downloadInfo.getStatus());
            response.shelve();
            listener.onShelved(this);
        }
    }


    @Override
    public void onComplete() {
        if (isCompleted()) {
            if (verify()) {
                status = DownloadStatus.COMPLETED;
                downloadInfo.setStatus(status);
                downloadInfo.setProgress(100);
                update(downloadInfo);
                response.complete(downloadInfo.getPath());
                destroy();
            } else {
                status = DownloadStatus.FAILED;
                downloadInfo.setStatus(status);
                downloadInfo.setProgress(0);
                downloadInfo.setFinished(0);
                update(downloadInfo);
                response.fail(new DownloadException(ExceptionCode.ERR_AT_VERIFY, "Corrupted file."));
                destroy();
            }
            if (DEBUG) {
                complete = System.currentTimeMillis();
                Log.d(TAG, "Connect costs time: " + (connect - start) + " ms, " + "download costs time: " + (complete - connect) + " ms");
            }
        }
    }

    @Override
    public boolean isCanceled() {
        for (int i = 0; i < dataFetchJobList.size(); i++) {
            if (dataFetchJobList.get(i).isRunning()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isFailed() {
        for (int i = 0; i < dataFetchJobList.size(); i++) {
            if (dataFetchJobList.get(i).isRunning()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isCompleted() {
        for (int i = 0; i < dataFetchJobList.size(); i++) {
            if (!dataFetchJobList.get(i).isCompleted()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isPaused() {
        for (int i = 0; i < dataFetchJobList.size(); i++) {
            if (dataFetchJobList.get(i).isRunning()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isShelved() {
        for (int i = 0; i < dataFetchJobList.size(); i++) {
            if (!dataFetchJobList.get(i).isShelved()) {
                return false;
            }
        }
        return true;
    }

    private void fetch(long length, boolean isAcceptRanges) {
        dataFetchJobList.clear();
        if (isAcceptRanges) {
            List<ThreadInfo> threadInfos = getParallelThreadInfos(length);
            long finished = 0;
            for (ThreadInfo threadInfo : threadInfos) {
                finished += threadInfo.getFinished();
                dataFetchJobList.add(new ParallelDataFetchJob(downloadInfo, threadInfo, dataBaseManager, priority, mConfig, this));
            }
            downloadInfo.setFinished(finished);
        } else {
            ThreadInfo threadInfo = getSingleThreadInfo();
            dataFetchJobList.add(new SingleDataFetchJob(downloadInfo, threadInfo, priority, mConfig, this));
        }

        for (int i = 0; i < dataFetchJobList.size(); i++) {
            dataFetchService.submit(dataFetchJobList.get(i)); // Submit this job.
        }
    }

    private boolean verify() {
        long expectedLength = downloadInfo.getLength();
        File file = getCacheFile();
        long realSize = file.length();
        if (realSize >= expectedLength) {
            return true;
        }
        return false;
    }

    private File getCacheFile() {
        File file = new File(downloadInfo.getDir().getAbsolutePath() + "/" + downloadInfo.getName());
        return file;
    }

    private boolean isCacheExist(File dir, String name) {
        try {
            File file = new File(dir.getAbsolutePath() + "/" + name);
            if (file.exists()) {
                return true;
            }
            return false;
        }catch (Exception e) {
            return false;
        }
    }

    private void create(List<ThreadInfo> threadInfos, long length) {
        final int threadNum = mConfig.getBestThreadNum(length);
        final long average = length / threadNum;
        for (int i = 0; i < threadNum; i++) {
            final long start = average * i;
            final long end;
            if (i == threadNum - 1) { /**NOTICE:: End of last thread is length */
                end = length;
            } else {
                end = start + average - 1;
            }
            ThreadInfo threadInfo = new ThreadInfo(i, key, request.getUrl(), start, end, 0);
            threadInfos.add(threadInfo);
        }
    }

    private List getParallelThreadInfos(long length) {
        final List<ThreadInfo> threadInfos = dataBaseManager.getThreadInfos(key);
        if (threadInfos.isEmpty()) {
            create(threadInfos, length);
        } else if ((threadInfos.get(0).getEnd() != length) || (!isCacheExist(downloadInfo.getDir(), downloadInfo.getName()))) {
            downloadInfo.setObsolete(true);
            threadInfos.clear();
            create(threadInfos, length);
        }
        return threadInfos;
    }

    private ThreadInfo getSingleThreadInfo() {
        ThreadInfo threadInfo = new ThreadInfo(0, key, request.getUrl(), 0);
        return threadInfo;
    }


    @Override
    public void update(DownloadInfo info) {
        dataBaseManager.update(info.getKey(), info.getLength(), info.getProgress(), info.getStatus());
    }

    @Override
    public void updateStatus(String key, int status) {
        dataBaseManager.update(key, status);
    }

    @Override
    public void updateLength(String key, long length, int status) {
        dataBaseManager.update(key, length, status);
    }

    @Override
    public void delete(String key) {
        dataBaseManager.delete(key);
    }

    @Override
    public Key getKey() {
        return mKey;
    }

    @Override
    public DownloadResponse getResponse() {
        return response;
    }
}
