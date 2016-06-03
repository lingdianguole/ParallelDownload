package jc.download.interfac;

import jc.download.executor.Prioritized;

public interface DataFetchRunnable extends Runnable, Prioritized {

    void cancel();

    void pause();

    void shelve();

    boolean isRunning();

    boolean isCompleted();

    boolean isCanceled();

    boolean isFailed();

    boolean isShelved();
}
