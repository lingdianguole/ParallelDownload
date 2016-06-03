package jc.download.db;

import java.io.File;

import jc.download.impl.DownloadStatus;

public class DownloadInfo {
    private String key;
    private String name;
    private String url;
    private long length;
    private long finished;
    private int progress;
    private int status = DownloadStatus.INIT;
    private String path;

    private File dir;
    private boolean acceptRanges;
    private boolean obsolete;

    public DownloadInfo() {
    }

    public DownloadInfo(String key, String name, String url, File dir) {
        this.key = key;
        this.name = name;
        this.url = url;
        this.dir = dir;
        this.path = dir.getAbsolutePath() + "/" + name;
    }

    // Need init if default construct.
    public void init() {
        int index = path.lastIndexOf('/');
        dir = new File(path.substring(0, index));
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getDir() {
        return dir;
    }

    public int getProgress() {
        return progress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isAcceptRanges() {
        return acceptRanges;
    }

    public void setAcceptRanges(boolean acceptRanges) {
        this.acceptRanges = acceptRanges;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public boolean getObsolete() {
        return obsolete;
    }

}
