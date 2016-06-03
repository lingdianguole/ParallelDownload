package jc.download;

import jc.download.interfac.DownloadConfiguration;

public class DefaultDownloadConfiguration implements DownloadConfiguration {
    private final static int NOTIFY_INTERVAL = 200; // 200 ms
    private final static int BUFFER_SIZE = 250 * 1024; // 250 KB
    private static final int DEFAULT_MAX_THREAD_NUMBER = 4;
    private final int cores;
    private final static int[] THREAD_NUM = {
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8
    };

    private final static int[] FILE_SIZE = {
            2 * 1024 * 1024,
            4 * 1024 * 1024,
            8 * 1024 * 1024,
            16 * 1024 * 1024,
            32 * 1024 * 1024,
            64 * 1024 * 1024,
            128 * 1024 * 1024,
            256 * 1024 * 1024,
    };

    private int maxThreadNum;

    public DefaultDownloadConfiguration() {
        cores = Runtime.getRuntime().availableProcessors();
        maxThreadNum = Math.max(cores, DEFAULT_MAX_THREAD_NUMBER);
    }


    public int getMaxThreadNum() {
        return maxThreadNum;
    }

    public int getBestThreadNum(long length) {
        int i;
        for (i = 0; i < FILE_SIZE.length; i++) {
            if (length < FILE_SIZE[i]) {
                return Math.min(THREAD_NUM[i], Math.min(cores, maxThreadNum));
            }
        }
        return Math.min(THREAD_NUM[i - 1], Math.min(cores, maxThreadNum));
    }

    private int maxCapacity = 4; // Default support 4 downloader running at the same time.

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    @Override
    public int getBufferSize() {
        return BUFFER_SIZE;
    }

    @Override
    public int getNotifyInterval() {
        return NOTIFY_INTERVAL;
    }
}
