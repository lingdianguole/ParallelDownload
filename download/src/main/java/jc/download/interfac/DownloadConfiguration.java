package jc.download.interfac;

public interface DownloadConfiguration {

    /**
     * @param length File size to be downloaded.
     * @return Best threads count for this downloader.
     */
    int getBestThreadNum(long length);

    /**
     * @return Max threads count for every downloader.
     */
    int getMaxThreadNum();


    void setMaxCapacity(int maxCapacity);

    /**
     * @return Max parallel running downloader count.
     */
    int getMaxCapacity();

    /**
     * @return Buffer size every round read.
     */
    int getBufferSize();

    /**
     * @return Progress refresh interval.
     */
    int getNotifyInterval();
}
