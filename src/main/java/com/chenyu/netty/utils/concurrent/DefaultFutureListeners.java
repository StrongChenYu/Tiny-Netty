package com.chenyu.netty.utils.concurrent;

final class DefaultFutureListeners {


    private GenericFutureListener<? extends Future<?>>[] listeners;
    private int size;

    public <V> DefaultFutureListeners(GenericFutureListener<?> listeners, GenericFutureListener<? extends Future<? super V>> listener) {
        
    }
    
    public DefaultFutureListeners() {}

    public GenericFutureListener<?>[] listeners() {
        return listeners;
    }

    public int size() {
        return size;
    }

    public <V> void add(GenericFutureListener<? extends Future<? super V>> listener) {
        
    }

    public <V> void remove(GenericFutureListener<? extends Future<? super V>> listener) {
        
        
    }
}
