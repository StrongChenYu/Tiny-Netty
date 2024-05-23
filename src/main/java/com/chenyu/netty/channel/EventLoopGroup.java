package com.chenyu.netty.channel;

import com.chenyu.netty.utils.concurrent.EventExecutorGroup;

public interface EventLoopGroup extends EventExecutorGroup {

    @Override
    EventLoop next();
    
    ChannelFuture register(Channel channel);


    ChannelFuture register(ChannelPromise promise);
}
