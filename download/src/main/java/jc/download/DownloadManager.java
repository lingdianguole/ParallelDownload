package jc.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import jc.download.db.DataBaseManager;
import jc.download.executor.LifoPriorityThreadPoolExecutor;
import jc.download.impl.DownloadRequest;
import jc.download.impl.DownloadResponseImpl;
import jc.download.impl.DownloaderImpl;
import jc.download.impl.Snapshoot;
import jc.download.interfac.DownloadConfiguration;
import jc.download.interfac.DownloadResponse;
import jc.download.interfac.DownloadStatusDelivery;
import jc.download.interfac.Downloader;
import jc.download.interfac.DownloaderChangedListener;
import jc.download.interfac.Key;


/** To apply this module into your application, should call init() before you call it's function. */
public class DownloadManager implements DownloaderChangedListener {
    private static DownloadManager INSTANCE;
    private ExecutorService connectService;
    private ExecutorService dataFetchService;
    private Context mContext;
    private DataBaseManager dataBaseManager;
    private LruCache<Key, Downloader> lruCache;
    private List<Downloader> shelvedList;
    private DownloadConfiguration mConfig;
    private volatile boolean initialized = false;
    public static DownloadManager getInstance() {
        if (null == INSTANCE) {
            synchronized (DownloadManager.class) {
                if (null == INSTANCE) {
                    INSTANCE = new DownloadManager();
                }
            }
        }
        return INSTANCE;
    }

    // Init is valid at the first time.
    public void init(Context context, DownloadConfiguration config) {
        if (initialized) {
            return;
        }
        initialized = true;
        mContext = context.getApplicationContext();
        if (null == config) {
            this.mConfig = new DefaultDownloadConfiguration();
        } else {
            this.mConfig = config;
        }
        shelvedList = new ArrayList<>();
        lruCache = new LruCache<Key, Downloader>(mConfig.getMaxCapacity()){
            @Override
            protected void entryRemoved(boolean evicted, Key key, Downloader oldValue, Downloader newValue) {
                if (evicted) {
                    oldValue.shelve();
                }
            }
        };
        connectService = new LifoPriorityThreadPoolExecutor(mConfig.getMaxCapacity());
        dataFetchService = new LifoPriorityThreadPoolExecutor(mConfig.getMaxThreadNum() * mConfig.getMaxCapacity());
        dataBaseManager = DataBaseManager.getInstance(mContext);

    }

    @Override
    public void onDestroy(Key key, Downloader downloader) {
        lruCache.remove(key);
        poll();
    }

    @Override
    public void onShelved(Downloader downloader) {
        shelvedList.add(downloader);
    }

    /**
     *
     * @param request
     * @param callBack
     */
    public void send(DownloadRequest request, CallBack callBack) {
        Assert.assertTrue(initialized);
        final Key key = request.getKey();
        Downloader downloader;
        downloader = lruCache.get(key);
        if (downloader == null) {
            DownloadStatusDelivery delivery = new DownloadStatusDeliveryImpl(new Handler(Looper.getMainLooper()));
            DownloadResponse response = new DownloadResponseImpl(delivery, callBack);
            downloader = new DownloaderImpl(request, response, connectService, dataFetchService, dataBaseManager, key.toString(), key, request.getPriority(), mConfig, this);
            lruCache.put(key, downloader);
            downloader.start();
        } else if (downloader.isPreparing()) {
            downloader.start();
        }
    }


    public void setCallback(DownloadRequest request, CallBack callBack) {
        Assert.assertTrue(initialized);
        final Key key = request.getKey();
        Downloader downloader;
        downloader = lruCache.get(key);
        if (downloader != null) {
            downloader.getResponse().setCallback(callBack);
        }

    }

    public void pause(DownloadRequest request) {
        Assert.assertTrue(initialized);
        final Key key = request.getKey();
        Downloader downloader = lruCache.get(key);
        if (downloader != null) {
            if (downloader.isRunning()) {
                downloader.pause();
            }
            lruCache.remove(key);
            poll();
        }
    }

    public void pause(final Key key) {
        Assert.assertTrue(initialized);
        Downloader downloader = lruCache.get(key);
        if (downloader != null) {
            if (downloader.isRunning()) {
                downloader.pause();
            }
            lruCache.remove(key);
            poll();
        }
    }

    public void cancel(DownloadRequest request) {
        Assert.assertTrue(initialized);
        Key key = request.getKey();
        Downloader downloader = lruCache.get(key);
        if (downloader != null) {
            if (downloader.isRunning()) {
                downloader.cancel();
            } else if (downloader.isPreparing()) {

            }
            lruCache.remove(key);
            poll();
        }
    }

    public void cancel(final Key key) {
        Assert.assertTrue(initialized);
        Downloader downloader = lruCache.get(key);
        if (downloader != null) {
            if (downloader.isRunning()) {
                downloader.cancel();
            }
            lruCache.remove(key);
            poll();
        }

    }

    public void pause() {
        Assert.assertTrue(initialized);
        shelvedList.clear();
        Map<Key, Downloader> map = lruCache.snapshot();
        for (Downloader downloader : map.values()) {
            if (downloader != null) {
                if (downloader.isRunning()) {
                    downloader.pause();
                }
            }
        }
    }

    public void disableProgress() {
        Assert.assertTrue(initialized);
        Map<Key, Downloader> map = lruCache.snapshot();
        for (Downloader downloader : map.values()) {
            if (downloader != null) {
                if (downloader.isRunning()) {
                    downloader.disableProgress();
                }
            }
        }
    }

    public void enableProgress() {
        Assert.assertTrue(initialized);
        Map<Key, Downloader> map = lruCache.snapshot();
        for (Downloader downloader : map.values()) {
            if (downloader != null) {
                if (downloader.isRunning()) {
                    downloader.enableProgress();
                }
            }
        }
    }

    public void cancel() {
        Assert.assertTrue(initialized);
        shelvedList.clear();
        Map<Key, Downloader> map = lruCache.snapshot();
        for (Downloader downloader : map.values()) {
            if (downloader != null) {
                if (downloader.isRunning()) {
                    downloader.cancel();
                }
            }
        }
    }

    public Snapshoot getSnapshoot(Key key) {
        Assert.assertTrue(initialized);
        String keyString = key.toString();
        Snapshoot snapshoot = dataBaseManager.getSnapshoot(keyString);
        if (snapshoot != null) {
            File file = new File(snapshoot.path);
            if (!file.exists()) {
                snapshoot.reset();
            }
        }
        return snapshoot;
    }


    public boolean isRunning(Key key) {
        Assert.assertTrue(initialized);
        if (lruCache.get(key) != null) {
            return true;
        }
        for (int i = 0; i < shelvedList.size(); i++) {
            if (key.equals(shelvedList.get(i).getKey())) {
                return true;
            }
        }
        return false;
    }


    private void poll() {
        int capacity = mConfig.getMaxCapacity() - lruCache.size();
        int index = shelvedList.size() - 1;
        synchronized (lruCache) {
            while (index >= 0 && capacity >= 0) {
                Downloader downloader = shelvedList.get(index);
                if (downloader.isShelved()) {
                    shelvedList.remove(index);
                    lruCache.put(downloader.getKey(), downloader);
                    downloader.start();
                    index--;
                    capacity--;
                } else {
                    shelvedList.remove(index--);
                }
            }
        }
    }

    public void activate(Key key, CallBack callBack) {
        Assert.assertTrue(initialized);
        if (lruCache.get(key) != null) {
            return;
        }
        for (int i = 0; i < shelvedList.size(); i++) {
            Downloader downloader = shelvedList.get(i);
            if (downloader.getKey().equals(key)) {
                synchronized (lruCache) {
                    shelvedList.remove(downloader);
                    lruCache.put(key, downloader);
                    downloader.getResponse().setCallback(callBack);
                    downloader.start();
                    break;
                }
            }
        }
    }

    public void activate(DownloadRequest request, CallBack callBack) {
        Assert.assertTrue(initialized);
        activate(request.getKey(), callBack);
    }

}
