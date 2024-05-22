package com.chenyu.netty.channel;

import com.chenyu.netty.channel.nio.NioEventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public abstract class AbstractNioChannel extends AbstractChannel {

    private final Logger logger = LoggerFactory.getLogger(AbstractChannel.class);

    private final SelectableChannel ch;
    protected final int readInterestOp;
    volatile SelectionKey selectionKey;
    boolean readPending;

    AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
        super(parent);
        this.ch = ch;
        this.readInterestOp = readInterestOp;
        
        try {
            ch.configureBlocking(false);
        } catch (IOException e) {
            try {
                ch.close();
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            throw new RuntimeException("Failed to enter non-blocking mode.", e);
        }
    }

    @Override
    public boolean isOpen() {
        return ch.isOpen();
    }

    protected SelectableChannel javaChannel() {
        return ch;
    }

    @Override
    public NioEventLoop eventLoop() {
        return (NioEventLoop) super.eventLoop();
    }

    protected SelectionKey selectionKey() {
        assert selectionKey != null;
        return selectionKey;
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof NioEventLoop;
    }

    @Override
    protected void doRegister() throws Exception {
        selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
    }

    @Override
    protected void doBeginRead() throws Exception {
        final SelectionKey selectionKey = this.selectionKey;
        if (!selectionKey.isValid()) {
            return;
        }
        final int interestOps = selectionKey.interestOps();
        if ((interestOps & readInterestOp) == 0) {
            selectionKey.interestOps(interestOps | readInterestOp);
        }
    }

    @Override
    public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        try {
            boolean doConnect = doConnect(remoteAddress, localAddress);
            if (!doConnect) {
                promise.trySuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception;


    protected abstract void read();
}
