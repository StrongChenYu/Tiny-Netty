package com.chenyu.netty.utils;

import java.util.concurrent.Executor;

public interface EventExecutor extends EventExecutorGroup {

    @Override
    EventExecutor next();
    
    EventExecutorGroup parent();
    
    boolean inEventLoop(Thread thread);
}
