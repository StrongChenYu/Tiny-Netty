package com.chenyu.netty.utils.concurrent;

import com.chenyu.netty.utils.concurrent.Future;
import com.chenyu.netty.utils.concurrent.GenericFutureListener;

public interface FutureListener<V> extends GenericFutureListener<Future<V>> {
    
}
