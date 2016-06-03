package jc.download.impl;

import android.text.TextUtils;

import junit.framework.Assert;

import jc.download.interfac.Key;
import jc.download.interfac.KeyFactory;

public class UrlKeyFactory implements KeyFactory{

    private String url;

    public UrlKeyFactory(String url) {
        Assert.assertFalse(TextUtils.isEmpty(url));
        this.url = url;
    }
    public Key build() {
        return new UrlKey(url);
    }
}
