package jc.download.impl;

public class Snapshoot {
    public int status;
    public int progress;
    public long length;
    public String path;

    @Override
    public String toString() {
       return new StringBuilder().append("status: ").append(status).append(", progress: ").append(progress).append(", length: ").append(length).append(", path: ").append(path).toString();
    }

    public void reset() {
        status = DownloadStatus.INIT;
        progress = 0;
    }
}
