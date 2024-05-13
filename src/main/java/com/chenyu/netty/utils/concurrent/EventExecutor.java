package com.chenyu.netty.utils.concurrent;

public interface EventExecutor extends EventExecutorGroup {

    @Override
    EventExecutor next();
    
    EventExecutorGroup parent();
    
    boolean inEventLoop(Thread thread);
}
