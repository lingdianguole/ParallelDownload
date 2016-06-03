package jc.download.impl;

import android.text.TextUtils;

import jc.download.interfac.Key;
import jc.download.util.Util;

public class UrlKey implements Key {

    String url;
    private String stringKey;

    public UrlKey(String url) {
        if (TextUtils.isEmpty(url)) throw new IllegalArgumentException("url can't be null.");
        this.url = url;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UrlKey that = (UrlKey)o;

        if (!url.equals(that.url)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        return result;
    }

    @Override
    public String toString(){
        if (stringKey == null) {
            stringKey = Util.byteArrayToHex(url.getBytes());
        }
        return stringKey;
    }
}
