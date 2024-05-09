package com.chenyu.netty.channel;

import com.chenyu.netty.channel.nio.NioEventLoop;
import com.chenyu.netty.utils.DefaultThreadFactory;
import com.chenyu.netty.utils.SingleThreadEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executor;

public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventLoop.class);
    
    protected static final int DEFAULT_MAX_PENDING_TASKS = Integer.MAX_VALUE;


    public SingleThreadEventLoop(Executor executor, EventLoopTaskQueueFactory queueFactory) {
        super(executor, queueFactory, new DefaultThreadFactory());
    }

    @Override
    protected boolean hasTasks() {
        return super.hasTasks();
    }
    
    public void register(ServerSocketChannel channel, NioEventLoop nioEventLoop) {
        if (inEventLoop(Thread.currentThread())) {
            register0(channel, nioEventLoop);
        } else {
            nioEventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    register0(channel, nioEventLoop);
                    logger.info("nioEventLoop register channel in thread:{}",Thread.currentThread().getName());
                }
            });
        }
    }

    private void register0(ServerSocketChannel channel, NioEventLoop nioEventLoop) {
        try {
            channel.configureBlocking(false);
            channel.register(nioEventLoop.unwrappedSelector(), SelectionKey.OP_CONNECT);
        } catch (Exception e) {
            logger.error("SingleThreadEventLoop register0 error for channel {} and nioEventLoop {}", channel, nioEventLoop, e);
        }
    }
}
