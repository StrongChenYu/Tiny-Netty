package com.chenyu.netty.utils.concurrent;

public interface RejectedExecutionHandler {

    void reject(Runnable task, SingleThreadEventExecutor executor);
}
