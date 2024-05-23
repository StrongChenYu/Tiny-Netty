package com.chenyu.netty.channel.nio;

import com.chenyu.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNioMessageChannel extends AbstractNioChannel {

    private final Logger logger = LoggerFactory.getLogger(AbstractNioMessageChannel.class);

    boolean inputShutdown;
    private final List<Object> readBuf = new ArrayList<Object>();

    protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
        super(parent, ch, readInterestOp);
    }

    @Override
    protected void doBeginRead() throws Exception {
        if (inputShutdown) {
            return;
        }
        super.doBeginRead();
    }

    protected abstract int doReadMessages(List<Object> buf) throws Exception;

    // 可以理解为serverSocketChannel接口的处理
    @Override
    protected void read() {
        assert eventLoop().inEventLoop(Thread.currentThread());

        boolean closed = false;
        Throwable exception = null;

        try {
            do {
                int localRead = doReadMessages(readBuf);
                if (localRead == 0) {
                    break;
                }
            } while (true);
        } catch (Throwable t) {
            exception = t;
        }
        
        int size = readBuf.size();
        for (int i = 0; i < size; i ++) {
            readPending = false;
            Channel child = (Channel) readBuf.get(i);
            logger.info("receive child {}", child);
        }

        readBuf.clear();
        if (exception != null) {
            throw new RuntimeException(exception);
        }
    }
}
