package jc.download.interfac;

public interface DownloaderChangedListener {
    void onDestroy(Key key, Downloader downloader);
    void onShelved(Downloader downloader);
}
