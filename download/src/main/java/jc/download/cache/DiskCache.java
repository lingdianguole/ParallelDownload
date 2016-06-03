package jc.download.cache;

import java.io.File;

public interface DiskCache {
    interface Factory {

        /** 250 MB of cache. */
        int DEFAULT_DISK_CACHE_SIZE = 250 * 1024 * 1024;
        String DEFAULT_DISK_CACHE_DIR = "download";

        /**
         * Returns a new disk cache, or {@code null} if no disk cache could be created.
         */
        DiskCache build();
    }

    File getCacheDirectory();
}
