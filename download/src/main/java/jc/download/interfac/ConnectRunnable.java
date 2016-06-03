package jc.download.interfac;

import jc.download.executor.Prioritized;

public interface ConnectRunnable extends Runnable, Prioritized {

    void cancel();

    void shelve();

    boolean isConnecting();

    boolean isConnected();

    boolean isCanceled();

    boolean isFailed();
}
