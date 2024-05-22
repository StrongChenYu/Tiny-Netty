package com.chenyu.netty.channel.socket;

import com.chenyu.netty.channel.AbstractNioByteChannel;
import com.chenyu.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class NioSocketChannel extends AbstractNioByteChannel {
    
    @Override
    protected int doReadBytes(ByteBuffer buf) throws Exception {
        return 0;
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void bind(SocketAddress localAddress, ChannelPromise promise) {

    }
}
