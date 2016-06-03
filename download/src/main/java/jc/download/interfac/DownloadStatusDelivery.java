package jc.download.interfac;

import jc.download.impl.DownloadStatus;

public interface DownloadStatusDelivery {

    void post(DownloadStatus status);
}
