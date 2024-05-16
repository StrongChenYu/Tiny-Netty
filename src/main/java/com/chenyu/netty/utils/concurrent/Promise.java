package com.chenyu.netty.utils.concurrent;

public interface Promise<V> extends Future<V> {
    
    Promise<V> setSuccess(V result);
    
    boolean trySuccess(V result);
    
    Promise<V> setFailure(Throwable cause);
    
    boolean tryFailure(Throwable cause);
    
    boolean setUncancellable();

    @Override
    Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);

    @Override
    Future<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listener);

    @Override 
    Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);

    @Override
    Future<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

    @Override
    Promise<V> await() throws InterruptedException;

    @Override
    Promise<V> awaitUninterruptibly();

    @Override
    Promise<V> sync() throws InterruptedException;

    @Override
    Promise<V> syncUninterruptibly();
}
