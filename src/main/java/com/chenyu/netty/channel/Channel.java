package com.chenyu.netty.channel;

import java.net.SocketAddress;

public interface Channel {

    ChannelId id();

    EventLoop eventLoop();


    Channel parent();


    ChannelConfig config();


    boolean isOpen();


    boolean isRegistered();


    boolean isActive();


    SocketAddress localAddress();


    SocketAddress remoteAddress();


    ChannelFuture closeFuture();


    ChannelFuture close();

  
    void bind(SocketAddress localAddress, ChannelPromise promise);


    void connect(SocketAddress remoteAddress, final SocketAddress localAddress,ChannelPromise promise);

 
    void register(EventLoop eventLoop, ChannelPromise promise);
    
    
    void beginRead();
}
