package jc.download.executor;

import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LifoPriorityThreadPoolExecutor extends ThreadPoolExecutor {
    private static final String TAG = "LifoPriority";
    private final AtomicInteger ordering = new AtomicInteger();
    private final UncaughtThrowableStrategy uncaughtThrowableStrategy;
    private final boolean LIFO;
    public enum UncaughtThrowableStrategy {
        /**
         * Silently catches and ignores the uncaught throwables.
         */
        IGNORE,
        /**
         * Logs the uncaught throwables using {@link #TAG} and {@link Log}.
         */
        LOG {
            @Override
            protected void handle(Throwable t) {
                if (Log.isLoggable(TAG, Log.ERROR)) {
                    Log.e(TAG, "Request threw uncaught throwable", t);
                }
            }
        },
        /**
         * Rethrows the uncaught throwables to crash the app.
         */
        THROW {
            @Override
            protected void handle(Throwable t) {
                super.handle(t);
                throw new RuntimeException(t);
            }
        };

        protected void handle(Throwable t) {
            // Ignore.
        }
    }

    public LifoPriorityThreadPoolExecutor(int poolSize) {
        this(poolSize, true);
    }

    public LifoPriorityThreadPoolExecutor(int poolSize, boolean LIFO) {
        this(poolSize, LIFO, UncaughtThrowableStrategy.LOG);
    }


    public LifoPriorityThreadPoolExecutor(int poolSize, boolean LIFO, UncaughtThrowableStrategy uncaughtThrowableStrategy) {
        this(poolSize, poolSize, 0, TimeUnit.MILLISECONDS, new DefaultThreadFactory(), LIFO,
                uncaughtThrowableStrategy);
    }

    public LifoPriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAlive, TimeUnit timeUnit,
                                          ThreadFactory threadFactory, boolean LIFO, UncaughtThrowableStrategy uncaughtThrowableStrategy) {
        super(corePoolSize, maximumPoolSize, keepAlive, timeUnit, new PriorityBlockingQueue<Runnable>(), threadFactory);
        this.uncaughtThrowableStrategy = uncaughtThrowableStrategy;
        this.LIFO = LIFO;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        if (LIFO) {
            return new LoadTask<T>(runnable, value, ordering.getAndIncrement());
        } else {
            return new FIFOLoadTask<T>(runnable, value, ordering.getAndIncrement());
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            Future<?> future = (Future<?>) r;
            if (future.isDone() && !future.isCancelled()) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    uncaughtThrowableStrategy.handle(e);
                } catch (ExecutionException e) {
                    uncaughtThrowableStrategy.handle(e);
                }
            }
        }
    }

    public static class DefaultThreadFactory implements ThreadFactory {
        int threadNum = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            final Thread result = new Thread(runnable, "lifo-pool-thread-" + threadNum) {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    super.run();
                }
            };
            threadNum++;
            return result;
        }
    }

    // Visible for testing.
    static class LoadTask<T> extends FutureTask<T> implements Comparable<LoadTask<?>> {
        private final int priority;
        private final int order;

        public LoadTask(Runnable runnable, T result, int order) {
            super(runnable, result);
            if (!(runnable instanceof Prioritized)) {
                throw new IllegalArgumentException("FifoPriorityThreadPoolExecutor must be given Runnables that "
                        + "implement Prioritized");
            }
            priority = ((Prioritized) runnable).getPriority();
            this.order = order;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object o) {
            if (o instanceof LoadTask) {
                LoadTask<Object> other = (LoadTask<Object>) o;
                return order == other.order && priority == other.priority;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = priority;
            result = 31 * result + order;
            return result;
        }

        @Override
        public int compareTo(LoadTask<?> loadTask) {
            int result = priority - loadTask.priority;
            if (result == 0) {
                result =  loadTask.order - order;  // LIFO.
            }
            return result;
        }
    }

    // Visible for testing.
    static class FIFOLoadTask<T> extends FutureTask<T> implements Comparable<LoadTask<?>> {
        private final int priority;
        private final int order;

        public FIFOLoadTask(Runnable runnable, T result, int order) {
            super(runnable, result);
            if (!(runnable instanceof Prioritized)) {
                throw new IllegalArgumentException("FifoPriorityThreadPoolExecutor must be given Runnables that "
                        + "implement Prioritized");
            }
            priority = ((Prioritized) runnable).getPriority();
            this.order = order;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object o) {
            if (o instanceof LoadTask) {
                LoadTask<Object> other = (LoadTask<Object>) o;
                return order == other.order && priority == other.priority;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = priority;
            result = 31 * result + order;
            return result;
        }

        @Override
        public int compareTo(LoadTask<?> loadTask) {
            int result = priority - loadTask.priority;
            if (result == 0) {
                result = order - loadTask.order;  // FIFO.
            }
            return result;
        }
    }
}
