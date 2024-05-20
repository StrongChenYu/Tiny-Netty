package com.chenyu.netty.utils.concurrent;

import java.util.Arrays;

final class DefaultFutureListeners {


    private GenericFutureListener<? extends Future<?>>[] listeners;
    private int size;
    private int progressiveSize;

    public <V> DefaultFutureListeners(GenericFutureListener<? extends Future<?>> first, GenericFutureListener<? extends Future<?>> second) {
        listeners = new GenericFutureListener[2];
        listeners[0] = first;
        listeners[1] = second;
        size = 2;
        if (first instanceof GenericProgressiveFutureListener) {
            progressiveSize++;
        }
        
        if (second instanceof GenericProgressiveFutureListener) {
            progressiveSize++;
        }
        
    }
    
    public DefaultFutureListeners() {}

    public GenericFutureListener<?>[] listeners() {
        return listeners;
    }

    public int size() {
        return size;
    }

    public int getProgressiveSize() {
        return progressiveSize;
    }

    public <V> void add(GenericFutureListener<? extends Future<?>> l) {
        GenericFutureListener<? extends Future<?>>[] listeners = this.listeners;
        final int size = this.size;
        if (size == listeners.length) {
            this.listeners = listeners = Arrays.copyOf(listeners, size << 1);
        }
        
        listeners[size] = l;
        this.size = size + 1;
        if (l instanceof GenericProgressiveFutureListener) {
            this.progressiveSize++;
        }
    }

    public <V> void remove(GenericFutureListener<? extends Future<? super V>> l) {
        final GenericFutureListener<? extends Future<?>>[] listeners = this.listeners;
        int size = this.size;
        for (int i = 0; i < size; i ++) {
            if (listeners[i] == l) {
                // size - (i+1) 就是 [i+1, size)的元素
                int listenersToMove = size - (i + 1);
                if (listenersToMove > 0) {
                    // 把i+1的元素 移动到i的位置上来 大概意思就是
                    System.arraycopy(listeners, i + 1, listeners, i, listenersToMove);
                }
                listeners[-- size] = null;
                this.size = size;

                if (l instanceof GenericProgressiveFutureListener) {
                    progressiveSize --;
                }
                return;
            }
        }
    }
}
