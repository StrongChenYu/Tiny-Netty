package com.chenyu.netty.bootstrap;

import com.chenyu.netty.channel.*;
import com.chenyu.netty.channel.nio.NioEventLoop;
import com.chenyu.netty.utils.concurrent.DefaultPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

public class ServerBootstrap<C extends Channel> {

    private static final Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    private ServerSocketChannel serverSocketChannel;

    private static final String DEFAULT_ADDRESS = "localhost";
    private EventLoopGroup parentGroup;
    private EventLoopGroup workerGroup;
    private NioEventLoop nioEventLoop;
    
    private volatile ChannelFactory<? extends Channel> channelFactory;

    public ServerBootstrap() {

    }
    
    public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup workerGroup) {
        this.parentGroup = parentGroup;
        this.workerGroup = workerGroup;
        return this;
    }
    
    public ServerBootstrap channel(Class<? extends C> channelClass) {
        this.channelFactory = new ReflectiveChannelFactory<C>(channelClass);
        return this;
    }

    public ChannelFuture bind(String host, int inetPort) {
        return bind(new InetSocketAddress(host,inetPort));
    }
    
    public ChannelFuture bind(int port) {
        return bind(DEFAULT_ADDRESS, port);
    }

    public ChannelFuture bind(SocketAddress localAddress) {
        return doBind(localAddress);
    }

    private ChannelFuture doBind(SocketAddress localAddress) {
        final ChannelFuture regFuture = initAndRegister();
        Channel channel = regFuture.channel();
        
        
    }

    final ChannelFuture initAndRegister() {
        
    }

    private void doBind0(SocketAddress localAddress,  DefaultPromise<Object> promise) {
        nioEventLoop.execute(() -> {
            try {
                serverSocketChannel.bind(localAddress);
                Thread.sleep(3000);
                promise.setSuccess(null);
            } catch (Exception e) {
                logger.error("doBind0 error for address {}", localAddress, e);
            }
        });
    }

}
