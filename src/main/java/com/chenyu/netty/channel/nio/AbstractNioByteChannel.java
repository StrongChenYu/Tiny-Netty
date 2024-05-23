package com.chenyu.netty.channel.nio;

import com.chenyu.netty.channel.Channel;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public abstract class AbstractNioByteChannel extends AbstractNioChannel {

    protected AbstractNioByteChannel(Channel parent, SelectableChannel ch) {
        super(parent, ch, SelectionKey.OP_READ);
    }

    @Override
    public final void read() {
        //暂时用最原始简陋的方法处理
        ByteBuffer byteBuf = ByteBuffer.allocate(1024);
        try {
            doReadBytes(byteBuf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract int doReadBytes(ByteBuffer buf) throws Exception;

}
