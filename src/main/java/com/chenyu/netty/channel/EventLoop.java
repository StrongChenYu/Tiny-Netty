package com.chenyu.netty.channel;

import com.chenyu.netty.utils.concurrent.EventExecutor;
import com.chenyu.netty.utils.concurrent.EventExecutorGroup;

public interface EventLoop extends EventExecutor, EventLoopGroup {

    @Override
    EventExecutorGroup parent();
}
