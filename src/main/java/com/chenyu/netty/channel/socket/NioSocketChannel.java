package com.chenyu.netty.channel.socket;

import com.chenyu.netty.channel.nio.AbstractNioByteChannel;
import com.chenyu.netty.channel.Channel;
import com.chenyu.netty.utils.internal.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class NioSocketChannel extends AbstractNioByteChannel {
    
    private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
    
    private static final Logger logger = LoggerFactory.getLogger(NioSocketChannel.class);
    private static SocketChannel newSocket(SelectorProvider provider) {
        try {
            return provider.openSocketChannel();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open a socket.", e);
        }
    }

    public NioSocketChannel() {
        this(DEFAULT_SELECTOR_PROVIDER);
    }
    
    public NioSocketChannel(SelectorProvider provider) {
        this(newSocket(provider));
    }

    public NioSocketChannel(SocketChannel socket) {
        this(null, socket);
    }

    public NioSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket);
    }

    @Override
    protected SocketChannel javaChannel() {
        return (SocketChannel) super.javaChannel();
    }

    @Override
    protected int doReadBytes(ByteBuffer byteBuf) throws Exception {
        int len = javaChannel().read(byteBuf);
        byte[] buffer = new byte[len];
        byteBuf.flip();
        byteBuf.get(buffer);
        logger.info("Nio Socket Channel receive msg {}", new String(buffer));
        return len;
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        if (localAddress != null) {
            doBind0(localAddress);
        }
        
        boolean success = false;
        try {
            boolean connected = SocketUtils.connect(javaChannel(), remoteAddress);
            if (!connected) {
                selectionKey().interestOps(SelectionKey.OP_CONNECT);
            }
            success = true;
            return connected;
        } finally {
            if (!success) {
                doClose();
            }
        }
    }

    protected void doClose() throws Exception {
        javaChannel().close();
    }

    @Override
    public boolean isActive() {
        SocketChannel socketChannel = javaChannel();
        return socketChannel.isOpen() && socketChannel.isConnected();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws IOException {
        doBind0(localAddress);
    }

    
    private void doBind0(SocketAddress localAddress) throws IOException {
        SocketUtils.bind(javaChannel(), localAddress);
    }
    
    
}
