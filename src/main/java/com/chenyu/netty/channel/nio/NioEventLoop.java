package com.chenyu.netty.channel.nio;

import com.chenyu.netty.channel.EventLoopGroup;
import com.chenyu.netty.channel.EventLoopTaskQueueFactory;
import com.chenyu.netty.channel.SelectStrategy;
import com.chenyu.netty.channel.SingleThreadEventLoop;
import com.chenyu.netty.utils.concurrent.RejectedExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class NioEventLoop extends SingleThreadEventLoop {
    
    private static final Logger logger = LoggerFactory.getLogger(NioEventLoop.class);
    
    private EventLoopGroup workerGroup;
    private static int index = 0;
    private int id = 0;
    private ServerSocketChannel serverSocketChannel;
    private SocketChannel socketChannel;
    private Selector selector;
    private final SelectorProvider selectorProvider;
    private SelectStrategy selectStrategy;    
    
    
    NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
                        SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler,
                        EventLoopTaskQueueFactory queueFactory) {
        super(parent, executor, false, newTaskQueue(queueFactory), newTaskQueue(queueFactory), rejectedExecutionHandler);

        if (selectorProvider == null) {
            throw new NullPointerException("selectorProvider");
        }
        if (strategy == null) {
            throw new NullPointerException("selectStrategy");
        }
        
        this.selectorProvider = selectorProvider;
        this.selector = openSelector();
        selectStrategy = strategy;
        id = index;
        
        logger.info("create new NioEventLoop index {}", index);
    }

    public void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public void setServerSocketChannel(ServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
    
    private static Queue<Runnable> newTaskQueue(EventLoopTaskQueueFactory queueFactory) {
        if (queueFactory == null) {
            return new LinkedBlockingQueue<>();
        }
        return queueFactory.newTaskQueue(DEFAULT_MAX_PENDING_TASKS);
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
                SocketChannel channel = serverSocketChannel.accept();
                channel.configureBlocking(false);
                
                NioEventLoop nioEventLoop = (NioEventLoop) workerGroup.next();
                nioEventLoop.setSocketChannel(channel);
                nioEventLoop.register(channel, nioEventLoop);

                logger.info("accept new connection from socketChannel {}", channel);
                
                channel.write(ByteBuffer.wrap("socketChannel online".getBytes()));
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
            return;
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
    
    
}
