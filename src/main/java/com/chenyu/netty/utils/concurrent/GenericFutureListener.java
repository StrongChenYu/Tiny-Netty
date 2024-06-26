package com.chenyu.netty.utils.concurrent;

import java.util.EventListener;

public interface GenericFutureListener<F extends Future<?>> extends EventListener {
    
    void operationComplete(F future) throws Exception;
}
