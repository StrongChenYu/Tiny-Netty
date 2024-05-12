package com.chenyu.netty.channel.nio;

import com.chenyu.netty.channel.EventLoopTaskQueueFactory;
import com.chenyu.netty.channel.SingleThreadEventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

public class NioEventLoop extends SingleThreadEventLoop {
    
    private static final Logger logger = LoggerFactory.getLogger(NioEventLoop.class);
    
    private final ServerSocketChannel serverSocketChannel;
    private final SocketChannel socketChannel;
    private NioEventLoop worker;
    private Selector selector;
    private final SelectorProvider selectorProvider;

    public NioEventLoop(ServerSocketChannel serverSocketChannel, SocketChannel socketChannel) {
        this(null, SelectorProvider.provider(), null, serverSocketChannel, socketChannel);
    }

    public NioEventLoop(Executor executor, SelectorProvider provider, EventLoopTaskQueueFactory taskQueueFactory, ServerSocketChannel serverSocketChannel, SocketChannel socketChannel) {
        super(executor, taskQueueFactory);

        if (provider == null) {
            throw new NullPointerException("selectorProvider");
        }
        if (serverSocketChannel != null && socketChannel != null) {
            throw new RuntimeException("only one channel can be here! server or client!");
        }
        
        this.selectorProvider = provider;
        this.serverSocketChannel = serverSocketChannel;
        this.socketChannel = socketChannel;
        this.selector = openSelector();
    }
    
    private Selector openSelector() {
        //未包装过的选择器
        final Selector unwrappedSelector;
        try {
            unwrappedSelector = selectorProvider.openSelector();
            return unwrappedSelector;
        } catch (IOException e) {
            throw new RuntimeException("failed to open a new selector", e);
        }
    }

    public Selector unwrappedSelector() {
        return selector;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    protected void run() {
        for(;;) {
            try {
                select();
                
                processSelectKeys();
            } catch (Exception e) {
                logger.error("NioEventLoop run exception ", e);
            } finally {
                runAllTasks();
            }
        }
    }

    private void select() throws IOException {
        Selector selector = this.selector;
        for (;;) {
            int selectKeys = selector.select(3000);

            // 只要满足有事件发生，或者队列中有任务就跳出循环
            if (selectKeys != 0 || hasTasks()) {
                break;
            }
        }
    }
    
    private void processSelectKeys() throws IOException {
        processSelectedKeysPlain(selector.selectedKeys());
    }

    private void processSelectedKeysPlain(Set<SelectionKey> selectionKeys) throws IOException {
        if (selectionKeys.isEmpty()) {
            return;
        }

        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        for (;;) {
            SelectionKey key = iterator.next();
            iterator.remove();
            
            processSelectKey(key);
            if (!iterator.hasNext()) {
                break;
            }
        }
    }

    private void processSelectKey(SelectionKey key) throws IOException {
        if (serverSocketChannel != null) {
            // 这里说明是ServerSocketChannel
            if (key.isAcceptable()) {
                // todo:  key.channel(); 这里返回的是什么？ 是ServerSocketChannel呢还是 SocketChannel
                SocketChannel channel = serverSocketChannel.accept();
                channel.configureBlocking(false);
                
                worker.registerRead(channel, worker);
                channel.write(ByteBuffer.wrap("socketChannel online".getBytes()));
                logger.info("accept new connection from socketChannel {}", channel);
            } 
            
            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int len = channel.read(byteBuffer);
                if (len == -1) {
                    logger.info("channel read end");
                    return;
                }
                byteBuffer.flip();
                byte[] temp = new byte[len];
                byteBuffer.get(temp);
                
                logger.info("accept info from channel info {}", new String(temp));
            }
        }
        
        if (socketChannel != null) {
            // 这里说明是socketChannel
            if (key.isConnectable()) {
                if (socketChannel.finishConnect()) {
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }
            
            if (key.isReadable()) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int len = socketChannel.read(byteBuffer);
                if (len == -1) {
                    logger.info("channel read end");
                }
                
                byteBuffer.flip();
                
                byte[] temp = new byte[len];
                byteBuffer.get(temp);

                logger.info("accept info from channel info {}", new String(temp));
            }
        }
    }

    public void setWorker(NioEventLoop worker) {
        this.worker = worker;
    }
}
