package com.chenyu.netty.channel;

import com.chenyu.netty.utils.NettyRuntime;
import com.chenyu.netty.utils.concurrent.DefaultThreadFactory;
import com.chenyu.netty.utils.concurrent.EventExecutor;
import com.chenyu.netty.utils.concurrent.EventExecutorChooserFactory;
import com.chenyu.netty.utils.concurrent.MultiThreadEventExecutorGroup;
import com.chenyu.netty.utils.internal.SystemPropertyUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public abstract class MultiThreadEventLoopGroup extends MultiThreadEventExecutorGroup implements EventLoopGroup {

    private static final int DEFAULT_EVENT_LOOP_THREADS;
    
    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    }
    
    protected MultiThreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
        super(nThreads, executor, args);
    }

    protected MultiThreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
    }

    protected MultiThreadEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory,
                                        Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, chooserFactory, args);
    }


    @Override
    protected ThreadFactory newDefaultThreadFactory() {
        return new DefaultThreadFactory(getClass(), Thread.MAX_PRIORITY);
    }

    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }

    @Override
    protected abstract EventExecutor newChild(Executor executor, Object... args);
}
