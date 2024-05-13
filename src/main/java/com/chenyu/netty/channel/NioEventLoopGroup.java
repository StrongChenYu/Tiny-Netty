package com.chenyu.netty.channel;

import com.chenyu.netty.channel.nio.NioEventLoop;

public interface NioEventLoopGroup {
    
    EventLoop next();
}
