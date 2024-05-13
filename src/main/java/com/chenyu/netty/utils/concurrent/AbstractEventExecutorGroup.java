package com.chenyu.netty.utils.concurrent;

import java.util.concurrent.TimeUnit;

public abstract class AbstractEventExecutorGroup implements EventExecutorGroup {
    
    @Override
    public void shutdownGracefully() {

    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void awaitTermination(Integer integer, TimeUnit timeUnit) throws InterruptedException {

    }

    @Override
    public void execute(Runnable command) {
        next().execute(command);
    }
}
