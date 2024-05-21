package com.chenyu.netty.utils.concurrent;

import com.chenyu.netty.utils.internal.StringUtil;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.chenyu.netty.utils.internal.ObjectUtil.checkNotNull;

public class DefaultPromise<V> extends AbstractFuture<V> implements Promise<V> {
    
    private EventExecutor executor;
    private volatile Object result;
    private static final AtomicReferenceFieldUpdater<DefaultPromise, Object> RESULT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.class, Object.class, "result");
    private static final Object SUCCESS = new Object();
    private static final Object UNCANCELLABLE = new Object();
    private Object listeners;
    private short waiters;
    private boolean notifyingListener;
    
    public DefaultPromise(EventExecutor executor) {
        this.executor = checkNotNull(executor, "executor");
    }
    
    public DefaultPromise() {
        this.executor = null;
    }
    
    
    
    protected EventExecutor executor() {
        return executor;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public Throwable cause() {
        return null;
    }

    @Override
    public Promise<V> setSuccess(V result) {
        if (setSuccess0(result)) {
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }

    private boolean setSuccess0(V result) {
        return setValue0(result == null ? SUCCESS : result);
    }

    private boolean setValue0(Object result) {
        if (RESULT_UPDATER.compareAndSet(this, null, result) ||
                RESULT_UPDATER.compareAndSet(this, UNCANCELLABLE, result)) {
            if (checkNotifyWaiters()) {
                notifyListeners();
            }
            return true;
        }
        return false;
    }

    private synchronized boolean checkNotifyWaiters() {
        if (waiters > 0) {
            notifyAll();
        }
        return listeners != null;
    }

    private void notifyListeners() {
        EventExecutor executor = executor();
        if (executor().inEventLoop(Thread.currentThread())) {
            notifyListenersNow();
        }
        
        safeExecute(executor, new Runnable() {
            @Override
            public void run() {
                notifyListenersNow();
            }
        });
    }

    private void notifyListenersNow() {
        Object listeners;
        synchronized (this) {
            // 有通知器 或者没有监听者
            if (notifyingListener || this.listeners == null) {
                return;
            }
            
            // 这里设置 其实就是一个标志位
            notifyingListener = true;
            listeners = this.listeners;
            
            // 这里直接置为null 防止其他线程使用
            this.listeners = null;
        }
        
        for (;;) {
            if (listeners instanceof DefaultFutureListeners) {
                notifyListeners0((DefaultFutureListeners) listeners);
            } else {
                notifyListener0(this, (GenericFutureListener<?>) listeners);
            }
            
            synchronized (this) {
                
                // 这里是神奇的多线程
                if (this.listeners == null) {
                    notifyingListener = false;
                    return;
                }
                
                listeners = this.listeners;
                this.listeners = null;
            }
        }
    }

    private void notifyListener0(Future future, GenericFutureListener listeners) {
        try {
            listeners.operationComplete(future);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void notifyListeners0(DefaultFutureListeners listeners) {
        GenericFutureListener<?>[] a = listeners.listeners();
        int size = listeners.size();
        for (int i = 0; i < size; i++) {
            notifyListener0(this, a[i]);
        }
    }

    private void safeExecute(EventExecutor executor, Runnable runnable) {
        try {
            executor.execute(runnable);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to submit a listener notification task. Event loop shut down?", t);
        }
    }

    @Override
    public boolean trySuccess(V result) {
        return setSuccess0(result);
    }

    @Override
    public Promise<V> setFailure(Throwable cause) {
        if (setFailure0(cause)) {
            return this;
        }

        throw new IllegalStateException("complete already: " + this, cause);
    }

    private boolean setFailure0(Throwable cause) {
        return setValue0(new CauseHolder(checkNotNull(cause, "cause")));
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        return setFailure0(cause);
    }

    @Override
    public boolean setUncancellable() {
        if (RESULT_UPDATER.compareAndSet(this, null, UNCANCELLABLE)) {
            return true;
        }
        
        Object result = this.result;
        return !isDone0(result) || !isCancelled0(result);
    }

    private static boolean isCancelled0(Object result) {
        return result instanceof CauseHolder && ((CauseHolder) result).cause instanceof CancellationException;
    }


    private boolean isDone0(Object result) {
        return result != null && result != UNCANCELLABLE;
    }

    @Override
    public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener) {
        checkNotNull(listener, "listener");
        
        synchronized (this) {
            addListener0(listener);
        }
        
        if (isDone()) {
            notifyListeners();
        }
        
        return this;
    }

    private void addListener0(GenericFutureListener<? extends Future<? super V>> listener) {
        if (this.listeners == null) {
            this.listeners = listener;
        } else if (this.listeners instanceof DefaultFutureListeners) {
            ((DefaultFutureListeners) this.listeners).add(listener);
        } else {
            this.listeners = new DefaultFutureListeners((GenericFutureListener<?>)this.listeners, listener);
        }
    }

    @Override
    public Promise<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners) {
        checkNotNull(listeners, "listeners");
        synchronized (this) {
            for (GenericFutureListener<? extends Future<? super V>> listener : listeners) {
                if (listener == null) {
                    break;
                }
                
                addListener0(listener);
            }
        }
        
        if (isDone()) {
            notifyListeners();
        }
        
        return this;
    }

    @Override
    public Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener) {
        checkNotNull(listener, "listener");
        synchronized (this) {
            removeListener0(listener);
        }
        
        return this;
    }

    private void removeListener0(GenericFutureListener<? extends Future<? super V>> listener) {
        if (this.listeners instanceof DefaultFutureListeners) {
            ((DefaultFutureListeners) this.listeners).remove(listener);
        } else {
            this.listeners = null;
        }
    }

    @Override
    public Promise<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners) {
        checkNotNull(listeners, "listeners");
        synchronized (this) {
            for (GenericFutureListener<? extends Future<? super V>> listener : listeners) {
                if (listener == null) {
                    break;
                }

                removeListener0(listener);
            }
        }

        
        return this;
    }

    @Override
    public Promise<V> await() throws InterruptedException {
        if (isDone()) {
            return this;
        }
        
        if (Thread.interrupted()) {
            throw new InterruptedException(toString());
        }
        
        // 一个线程运行到这里的时候就说明不正常了
        checkDeadLock();
        
        synchronized (this) {
            while (!isDone()) {
                incWaiters();
                try {
                    wait();
                } finally {
                    decWaiters();
                }
            }
        }
        
        return this;
    }

    private void decWaiters() {
        --waiters;
    }

    private void incWaiters() {
        if (waiters == Short.MAX_VALUE) {
            throw new IllegalStateException("too many waiters: " + this);
        }
        ++waiters;
    }

    //???? 这里怎么检测的死锁
    protected void checkDeadLock() {
        EventExecutor e = executor();
        if (e != null && e.inEventLoop(Thread.currentThread())) {
            throw new BlockingOperationException(toString());
        }
    }

    @Override
    public Promise<V> awaitUninterruptibly() {
        if (isDone()) {
            return this;
        }
        
        checkDeadLock();
        
        boolean interrupted = false;
        synchronized (this) {
            incWaiters();
            try {
                await();
            } catch (Exception e) {
                interrupted = true;
            } finally {
                decWaiters();
            }
        }
                
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        
        return this;
    }

    @Override
    public Promise<V> sync() throws InterruptedException {
        await();
        rethrowIfFailed();
        return this;
    }

    private void rethrowIfFailed() {
        Throwable cause = cause();
        if (cause == null) {
            return;
        }
    }

    @Override
    public Promise<V> syncUninterruptibly() {
        awaitUninterruptibly();
        rethrowIfFailed();
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return await0(unit.toNanos(timeout), true);
    }

    private boolean await0(long timeoutNanos, boolean interrupble) throws InterruptedException {
        if (isDone()) {
            return true;
        }
        
        if (timeoutNanos <= 0) {
            return isDone();
        }
        
        // 可以被中断 并且线程的标志位设置为了true
        if (interrupble && Thread.interrupted()) {
            throw new InterruptedException(toString());
        }
        
        checkDeadLock();
        
        long startTime = System.nanoTime();
        long waitTime = timeoutNanos;
        boolean interrupt = false;
        try {
            for (;;) {
                synchronized (this) {
                    if (isDone()) {
                        return true;
                    }

                    incWaiters();

                    try {
                        wait(waitTime / 1000000, (int) (waitTime) % 1000000);
                    } catch (InterruptedException e) {
                        if (interrupble) {
                            throw e;
                        } else {
                            interrupt = true;
                        }
                    } finally {
                        decWaiters();
                    }
                }

                if (isDone()) {
                    return true;
                } else {
                    waitTime = timeoutNanos - (System.nanoTime() - startTime);
                    if (waitTime <= 0) {
                        return isDone();
                    }
                }
            }
        } finally {
            if (interrupt) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return await0(TimeUnit.MICROSECONDS.toNanos(timeoutMillis), true);
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        try {
            return await0(unit.toNanos(timeout), false);
        } catch (InterruptedException e) {
            throw new InternalError();
        }
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return awaitUninterruptibly(timeoutMillis, TimeUnit.MICROSECONDS);
    }

    @Override
    public V getNow() {
        Object result = this.result;
        if (result instanceof CauseHolder || result == SUCCESS || result == UNCANCELLABLE) {
            return null;
        }
        return (V) result;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (RESULT_UPDATER.get(this) == null
                && RESULT_UPDATER.compareAndSet(this, null, new CauseHolder(new CancellationException()))) {
            if (checkNotifyWaiters()) {
                notifyListeners();
            }
            
            return true;
        }
        
        return false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled0(result);
    }

    @Override
    public boolean isDone() {
        return isDone0(result);
    }

    private static final class CauseHolder {

        final Throwable cause;

        CauseHolder(Throwable cause) {
            this.cause = cause;
        }
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    protected StringBuilder toStringBuilder() {
        StringBuilder buf = new StringBuilder(64)
                .append(StringUtil.simpleClassName(this))
                .append('@')
                .append(Integer.toHexString(hashCode()));

        Object result = this.result;
        if (result == SUCCESS) {
            buf.append("(success)");
        } else if (result == UNCANCELLABLE) {
            buf.append("(uncancellable)");
        } else if (result instanceof CauseHolder) {
            buf.append("(failure: ")
                    .append(((CauseHolder) result).cause)
                    .append(')');
        } else if (result != null) {
            buf.append("(success: ")
                    .append(result)
                    .append(')');
        } else {
            buf.append("(incomplete)");
        }

        return buf;
    }
}
