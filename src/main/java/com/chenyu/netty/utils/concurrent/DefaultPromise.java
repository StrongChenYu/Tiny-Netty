package com.chenyu.netty.utils.concurrent;

import com.chenyu.netty.utils.internal.ObjectUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

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
        this.executor = ObjectUtil.checkNotNull(executor, "executor");
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
        return false;
    }

    @Override
    public Promise<V> setFailure(Throwable cause) {
        return null;
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        return false;
    }

    @Override
    public boolean setUncancellable() {
        return false;
    }

    @Override
    public Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener) {
        return null;
    }

    @Override
    public Future<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listener) {
        return null;
    }

    @Override
    public Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener) {
        return null;
    }

    @Override
    public Future<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners) {
        return null;
    }

    @Override
    public Promise<V> await() throws InterruptedException {
        return null;
    }

    @Override
    public Promise<V> awaitUninterruptibly() {
        return null;
    }

    @Override
    public Promise<V> sync() throws InterruptedException {
        return null;
    }

    @Override
    public Promise<V> syncUninterruptibly() {
        return null;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return false;
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return false;
    }

    @Override
    public V getNow() {
        return null;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }
}
