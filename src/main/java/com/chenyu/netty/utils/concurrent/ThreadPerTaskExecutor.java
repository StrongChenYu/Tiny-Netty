package com.chenyu.netty.utils.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class ThreadPerTaskExecutor implements Executor {

    private Logger logger = LoggerFactory.getLogger(ThreadPerTaskExecutor.class);

    private ThreadFactory threadFactory;

    public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(Runnable command) {
        threadFactory.newThread(command).start();
        logger.info("ThreadPerTaskExecutor create new Thread");
    }
}
