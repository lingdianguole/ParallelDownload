package jc.download;

import android.os.Handler;

import java.util.concurrent.Executor;
import jc.download.impl.DownloadStatus;
import jc.download.interfac.DownloadStatusDelivery;

public class DownloadStatusDeliveryImpl implements DownloadStatusDelivery {

    private Executor poster;

    public DownloadStatusDeliveryImpl(final Handler handler) {
        poster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    @Override
    public void post(DownloadStatus status) {
        poster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    private static class DownloadStatusDeliveryRunnable implements Runnable {
        private final DownloadStatus mDownloadStatus;
        private final CallBack mCallBack;

        public DownloadStatusDeliveryRunnable(DownloadStatus downloadStatus) {
            this.mDownloadStatus = downloadStatus;
            this.mCallBack = mDownloadStatus.getCallBack();
        }

        @Override
        public void run() {
            if (mCallBack == null) return;
            switch (mDownloadStatus.getStatus()) {
                case DownloadStatus.CONNECTING:
                    mCallBack.onConnecting();
                    break;
                case DownloadStatus.CONNECTED:
                    mCallBack.onConnected(mDownloadStatus.getLength(), mDownloadStatus.isAcceptRanges());
                    break;
                case DownloadStatus.PROGRESS:
                    mCallBack.onProgress(mDownloadStatus.getFinished(), mDownloadStatus.getLength(), mDownloadStatus.getPercent());
                    break;
                case DownloadStatus.COMPLETED:
                    mCallBack.onComplete(mDownloadStatus.getPath());
                    break;
                case DownloadStatus.PAUSED:
                    mCallBack.onPause();
                    break;
                case DownloadStatus.CANCELED:
                    mCallBack.onCancel();
                    break;
                case DownloadStatus.FAILED:
                    mCallBack.onFail((DownloadException) mDownloadStatus.getException());
                    break;
                case DownloadStatus.SHELVED:
                    mCallBack.onShelve();
                    break;
            }
        }
    }
}
