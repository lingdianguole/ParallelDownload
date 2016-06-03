package jc.download.impl;


import android.content.Context;
import android.text.TextUtils;

import junit.framework.Assert;

import java.io.File;

import jc.download.cache.DiskCache;
import jc.download.cache.ExternalDiskCache;
import jc.download.executor.Priority;
import jc.download.interfac.Key;


public class DownloadRequest {

    private String url;

    private File mCacheDir;

    private String mTitle;

    private String mDescription;

    private Priority priority;

    private Key key;

    private String name; // File name.

    private DownloadRequest(Key key, String url, String name, File dir, String title, String description, Priority priority) {
        this.key = key;
        this.url = url;
        this.name = name;
        this.mCacheDir = dir;
        this.mTitle = title;
        this.mDescription = description;
        this.priority = priority;
    }

    public String getUrl() {
        return url;
    }

    File getCacheDir() {
        return mCacheDir;
    }


    public String getName() {
        return name;
    }

    public Key getKey() {
        return key;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getTitle() {
        return mTitle;
    }

    public static class Builder {

        private String url;

        private String mTitle;

        private String name;

        private String mDescription;

        private Priority priority;

        private Key key;

        private DiskCache.Factory factory ;

        public Builder(Context context) {
            factory = new ExternalDiskCache.ExternalDiskCacheFactory(context);
            priority = Priority.NORMAL;
        }

        /**MUST*/
        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.mDescription = description;
            return this;
        }

        public Builder setPriority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public Builder setDiskCacheFactory(DiskCache.Factory factory) {
            this.factory = factory;
            return this;
        }

        /**MUST*/
        public Builder setKey(Key key) {
            this.key = key;
            return this;
        }

        public DownloadRequest build() {
            Assert.assertTrue(key != null && !TextUtils.isEmpty(url) && factory != null);
            if (TextUtils.isEmpty(name)) {
                int index = url.lastIndexOf('/');
                name = url.substring(index + 1, url.length());
            }
            DownloadRequest request = new DownloadRequest(key, url, name, factory.build().getCacheDirectory(), mTitle, mDescription, priority);
            return request;
        }
    }
}
