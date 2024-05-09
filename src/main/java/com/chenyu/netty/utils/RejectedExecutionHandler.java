package com.chenyu.netty.utils;

public interface RejectedExecutionHandler {

    void reject(Runnable task, SingleThreadEventExecutor executor);
}
