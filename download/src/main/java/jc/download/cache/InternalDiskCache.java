package jc.download.cache;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

import jc.download.util.Util;

public class InternalDiskCache implements DiskCache {

    private Context context;
    private String cacheName;

    private InternalDiskCache(Context context, String cacheName) {
        this.context = context;
        this.cacheName = cacheName;
    }

    public static class InternalDiskCacheFactory implements DiskCache.Factory {

        private Context context;
        public InternalDiskCacheFactory(Context context) {
            this.context = context;
        }

        @Override
        public DiskCache build() {
            return new InternalDiskCache(context, Factory.DEFAULT_DISK_CACHE_DIR);
        }
    }


    @Override
    public File getCacheDirectory() {
        File cacheDirectory = context.getCacheDir();
        if (cacheDirectory == null) {
            return null;
        }
        if (!TextUtils.isEmpty(cacheName)) {
            File file = new File(cacheDirectory, cacheName);
            if (!file.exists()) {
                file.mkdir();
            }
            Util.chmod(file.getAbsolutePath());
            return file;
        }

        return cacheDirectory;
    }
}
