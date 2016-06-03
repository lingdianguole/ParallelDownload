package jc.downloaddemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

import jc.download.CallBack;
import jc.download.DownloadException;
import jc.download.DownloadManager;
import jc.download.impl.DownloadRequest;
import jc.download.impl.DownloadStatus;
import jc.download.impl.Snapshoot;
import jc.download.impl.UrlKeyFactory;
import jc.download.interfac.Key;

public class DownloadActivity extends Activity {

//    private final static String title = "平安好房";
//    private final static String imgUrl = "http://dev.doudou.com/uploads/201605/thumb_212132608b98953402976e3422218354.png";
//    private final static String url = "http://501xm.cache.cheerpic.com/source/chizi/201605/30200-0505.apk";

    private final static String[] titles = {"花椒直播", "平安好房", "陌陌"};
    private final static String[] imgUrls = {
                "http://www.doudou.com/uploads/201605/thumb_e11ff51a1254ebba1f8046480026b4a4.png",
                "http://dev.doudou.com/uploads/201605/thumb_212132608b98953402976e3422218354.png",
                "http://img.zcool.cn/community/033fcfa562dda7432f8755701385102.jpg"};
    private final static String[] urls = {
            "http://apk.doudou.com/upload/hjzb_20160519.apk",
            "http://501xm.cache.cheerpic.com/source/chizi/201605/30200-0505.apk",
            "http://gdown.baidu.com/data/wisegame/23d9d7786bb1276b/momo_760.apk"};

    private int id = 0;

    TextView title_tv;
    ProgressBar progressBar;
    private final static boolean DEBUG = true;
    private final static String TAG = "DownloadActivity";
    Button d_btn;
    long begin, connected, end;

//    public static final int INIT         = 0; // Just create.
//    public static final int STARTED      = 1; // Stated, maybe continued after paused.
//    public static final int CONNECTING   = 2; // Connecting for getting download info, such as: file size, accept ranges?
//    public static final int CONNECTED    = 3; // Got download info.
//    public static final int PROGRESS     = 4; // Downloading.
//    public static final int COMPLETED    = 5; // Complete download.
//    public static final int PAUSED       = 6; // Paused by user.
//    public static final int CANCELED     = 7; // Canceled by user.
//    public static final int FAILED       = 8; // Failed due to exception.

    private final static String INIT_TEXTS[] = {
            "下载",
            "下载",
            "下载",
            "继续",
            "继续",
            "安装",
            "继续",
            "继续",
            "重试",
            "继续"
    };

    private final static String RUNNING_TEXTS[] = {
            "下载",
            "准备中",
            "取消",
            "已连接",
            "暂停",
            "安装",
            "继续",
            "重新下载",
            "重试",
            "待下载"
    };

    private int index;

    private int getStatus(String text) {
        if (TextUtils.isEmpty(text)) return -1;
        for (int i = 0; i < RUNNING_TEXTS.length; i++)  {
            if (text.equals(RUNNING_TEXTS[i])) {
                return i;
            }
        }
        return -1;
    }

    CallBack callback =  new CallBack() {
        @Override
        public void onStarted() {
            index = 1;
            d_btn.setText(RUNNING_TEXTS[index]);
            begin = System.currentTimeMillis();
            if (DEBUG) {
                Log.i(TAG, "onStarted");
            }
        }

        @Override
        public void onConnecting() {
            index = 2;
            d_btn.setText(RUNNING_TEXTS[index]);
            if (DEBUG) {
                Log.i(TAG, "onConnecting");
            }
        }

        @Override
        public void onConnected(long total, boolean isRangeSupport) {
            index = 3;
            d_btn.setText(RUNNING_TEXTS[index]);
            connected = System.currentTimeMillis();
            if (DEBUG) {
                Log.i(TAG, "onConnected, total: " + total + ", isRangeSupport: " + isRangeSupport);
            }
        }

        @Override
        public void onProgress(long finished, long total, int progress) {
            index = 4;
            d_btn.setText(RUNNING_TEXTS[index]);
            progressBar.setProgress(progress);
            if (false) {
                Log.i(TAG, "onProgress, finished: " + finished + ", total: " + total + ", progress: " + progress);
            }
        }

        @Override
        public void onComplete(String path) {
            index = 5;
            d_btn.setText(RUNNING_TEXTS[index]);
            end = System.currentTimeMillis();
            progressBar.setProgress(100);
            if (DEBUG) {
                Log.i(TAG, "onComplete, connect time: " + (connected - begin) + " ms" + ", fetch time: " + (end - connected) + ", path: " + path);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse("file://" + path),"application/vnd.android.package-archive");
            DownloadActivity.this.startActivity(intent);
        }

        @Override
        public void onPause() {
            index = 6;
            d_btn.setText(RUNNING_TEXTS[index]);
            if (DEBUG) {
                Log.i(TAG, "onPause");
            }
        }

        @Override
        public void onCancel() {
            index = 7;
            d_btn.setText(RUNNING_TEXTS[index]);
            if (DEBUG) {
                Log.i(TAG, "onCancel");
            }
        }

        @Override
        public void onFail(DownloadException e) {
            index = 8;
            d_btn.setText(RUNNING_TEXTS[index]);
            if (DEBUG) {
                Log.i(TAG, "onFail, code: " + e.getCode());
            }
        }

        @Override
        public void onShelve() {
            index = 9;
            d_btn.setText(RUNNING_TEXTS[index]);
            if (DEBUG) {
                Log.i(TAG, "onShelve");
            }
        }
    };

    DownloadRequest request;

    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        DownloadManager.getInstance().init(this, null); /** NOTICE */
        title_tv = (TextView)findViewById(R.id.title_tv);
        iv = (ImageView)findViewById(R.id.iv);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        d_btn = (Button)findViewById(R.id.d_btn);
        d_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = getStatus(d_btn.getText().toString());
                switch (status) {
                    case DownloadStatus.INIT:
                        DownloadManager.getInstance().send(request, callback);
                        break;
                    case DownloadStatus.STARTED:
                    case DownloadStatus.CONNECTING:
                        DownloadManager.getInstance().pause(request);
                        break;
                    case DownloadStatus.CONNECTED:
                    case DownloadStatus.PROGRESS:
                        DownloadManager.getInstance().pause(request);
                        break;
                    case DownloadStatus.COMPLETED:
                        Snapshoot snap = DownloadManager.getInstance().getSnapshoot(request.getKey());
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.parse("file://" + snap.path), "application/vnd.android.package-archive");
                        DownloadActivity.this.startActivity(intent);
                        break;
                    case DownloadStatus.PAUSED:
                        DownloadManager.getInstance().send(request, callback);
                        break;
                    case DownloadStatus.CANCELED:
                        DownloadManager.getInstance().send(request, callback);
                        break;
                    case DownloadStatus.FAILED:
                        DownloadManager.getInstance().send(request, callback);
                        break;
                    case DownloadStatus.SHELVED:
                        DownloadManager.getInstance().activate(request, callback);
                        break;
                    default:
                        break;

                }

            }
        });

        findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snapshoot snap = DownloadManager.getInstance().getSnapshoot(request.getKey());
                if (snap != null) {
                    File file = new File(snap.path);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        });

        findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadManager.getInstance().cancel(request);
            }
        });

        findViewById(R.id.next_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadManager.getInstance().setCallback(request, null);
                id = (id + 1) % urls.length;
                initRequest(id);
            }
        });

        initRequest(id);
    }


    private void initRequest(int id) {
        Glide.with(this).load(imgUrls[id]).into(iv);
        Key key = new UrlKeyFactory(urls[id]).build();
        request = new DownloadRequest.Builder(DownloadActivity.this).setTitle(titles[id]).setUrl(urls[id]).setKey(new UrlKeyFactory(urls[id]).build()).build();
        Snapshoot snapshoot;
        if (DownloadManager.getInstance().isRunning(key)) {
            snapshoot = DownloadManager.getInstance().getSnapshoot(key);
            d_btn.setText(RUNNING_TEXTS[snapshoot.status]);
            progressBar.setProgress(snapshoot.progress);
            DownloadManager.getInstance().setCallback(request, callback);
            Log.i(TAG, "Running snapshoot: " + snapshoot);
        } else {
            snapshoot = DownloadManager.getInstance().getSnapshoot(key);
            if (snapshoot != null) {
                Log.i(TAG, "Snapshoot: " + snapshoot);
                d_btn.setText(INIT_TEXTS[snapshoot.status]);
                progressBar.setProgress(snapshoot.progress);
            } else {
                progressBar.setProgress(0);
                d_btn.setText(INIT_TEXTS[0]);
            }
        }
    }
}
