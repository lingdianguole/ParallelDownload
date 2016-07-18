package jc.download.db;

import android.content.ContentValues;

public class ThreadInfo {

    private int id;
    private String key;
    private String url;
    private long start;
    private long end;
    private long finished;

    public ThreadInfo() {
    }

    public ThreadInfo(int id, String key, String url, long finished) {
        this.id = id;
        this.key = key;
        this.url = url;
        this.finished = finished;
    }

    public ThreadInfo(int id, String key, String url, long start, long end, long finished) {
        this.id = id;
        this.key = key;
        this.url = url;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String uri) {
        this.url = uri;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    private final ContentValues contentValues = new ContentValues();


    public ContentValues getContentValues() {
        contentValues.put("id", id);
        contentValues.put("key", key);
        contentValues.put("url", url);
        contentValues.put("start", start);
        contentValues.put("end", end);
        contentValues.put("finished", finished);
        return contentValues;
    }
}
