package com.chenyu.netty.utils.concurrent;

final class DefaultFutureListeners {


    private GenericFutureListener<? extends Future<?>>[] listeners;
    private int size;
    
    public GenericFutureListener<?>[] listeners() {
        return listeners;
    }

    public int size() {
        return size;
    }
}
