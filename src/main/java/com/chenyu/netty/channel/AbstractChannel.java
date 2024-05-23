package com.chenyu.netty.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;

public abstract class AbstractChannel implements Channel {

    private final Logger logger = LoggerFactory.getLogger(AbstractChannel.class);
    
    private final Channel parent;
    private final ChannelId id;
    private volatile SocketAddress localAddress;
    private volatile SocketAddress remoteAddress;
    private Throwable initialCloseCause;
    private volatile EventLoop eventLoop;
    private volatile boolean registered;
    private final CloseFuture closeFuture = new CloseFuture(this);


    protected AbstractChannel(Channel parent) {
        this.parent = parent;
        id = newId();
    }

    
    protected AbstractChannel(Channel parent, ChannelId id) {
        this.parent = parent;
        this.id = id;
    }

    @Override
    public final ChannelId id() {
        return id;
    }

    @Override
    public EventLoop eventLoop() {
        EventLoop eventLoop = this.eventLoop;
        if (eventLoop == null) {
            throw new IllegalStateException("channel not registered to an event loop");
        }
        return eventLoop;
    }

    @Override
    public Channel parent() {
        return parent;
    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }
    
    @Override
    public SocketAddress localAddress() {
        return localAddress;
    }

    
    @Override
    public SocketAddress remoteAddress() {
        return null;
    }
    
    @Override
    public ChannelFuture closeFuture() {
        return closeFuture;
    }

    @Override
    public ChannelFuture close() {
        return null;
    }

    protected ChannelId newId() {
        return DefaultChannelId.newInstance();
    }
    
    protected abstract boolean isCompatible(EventLoop loop);

    @Override
    public void register(EventLoop eventLoop, ChannelPromise promise) {
        if (eventLoop == null) {
            throw new NullPointerException("eventLoop");
        }
        
        if (isRegistered()) {
            promise.setFailure(new IllegalStateException("registered to an event loop already"));
            return;
        }

        if (!isCompatible(eventLoop)) {
            promise.setFailure(new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
            return;
        }

        AbstractChannel.this.eventLoop = eventLoop;
        if (eventLoop.inEventLoop(Thread.currentThread())) {
            register0(promise);
        } else {
            try {
                eventLoop.execute(new Runnable() {
                    @Override
                    public void run() {
                        register0(promise);
                    }
                });
            } catch (Throwable t) {
                logger.error("Abstract register error ", t);
                closeFuture.setClosed();
                safeSetFailure(promise, t);
            }
        }
    }

    protected final void safeSetFailure(ChannelPromise promise, Throwable cause) {
        if (!promise.tryFailure(cause)) {
            throw new RuntimeException(cause);
        }
    }

    @Override
    public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
        try {
            doBind(localAddress);
            safeSetSuccess(promise);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void doBind(SocketAddress localAddress) throws Exception;


    private void register0(ChannelPromise promise) {
        try {
            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }
            
            doRegister();
            
            registered = true;
            safeSetSuccess(promise);
            beginRead();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public final void beginRead() {
        //如果是服务端的channel，这里仍然可能为false
        //那么真正注册读事件的时机，就成了绑定端口号成功之后
        if (!isActive()) {
            return;
        }
        try {
            doBeginRead();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    

    protected abstract void doBeginRead() throws Exception;

    private void safeSetSuccess(ChannelPromise promise) {
        if (!promise.trySuccess()) {
            logger.error("Failed to mark a promise as success because it is done already: {}", promise);
        }
    }

    protected abstract void doRegister() throws Exception;


    protected final boolean ensureOpen(ChannelPromise promise) {
        if (isOpen()) {
            return true;
        }
        safeSetFailure(promise, newClosedChannelException(initialCloseCause));
        return false;
    }

    private ClosedChannelException newClosedChannelException(Throwable cause) {
        ClosedChannelException exception = new ClosedChannelException();
        if (cause != null) {
            exception.initCause(cause);
        }
        return exception;
    }

    static final class CloseFuture extends DefaultChannelPromise {

        CloseFuture(AbstractChannel ch) {
            super(ch);
        }

        @Override
        public ChannelPromise setSuccess() {
            throw new IllegalStateException();
        }

        @Override
        public ChannelPromise setFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        @Override
        public boolean trySuccess() {
            throw new IllegalStateException();
        }

        @Override
        public boolean tryFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        boolean setClosed() {
            return super.trySuccess();
        }
    }
    
}
