package com.chenyu.netty.channel.socket;

import com.chenyu.netty.channel.AbstractNioMessageChannel;
import com.chenyu.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.List;

public class NioServerSocketChannel extends AbstractNioMessageChannel {
    
    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        return false;
    }

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        return 0;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void bind(SocketAddress localAddress, ChannelPromise promise) {

    }
}
