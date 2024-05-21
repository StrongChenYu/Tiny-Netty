package com.chenyu.netty.bootstrap;

import com.chenyu.netty.channel.EventLoop;
import com.chenyu.netty.channel.EventLoopGroup;
import com.chenyu.netty.channel.nio.NioEventLoop;
import com.chenyu.netty.utils.concurrent.DefaultPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

public class ServerBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    private ServerSocketChannel serverSocketChannel;

    private static final String DEFAULT_ADDRESS = "localhost";
    private EventLoopGroup parentGroup;
    private EventLoopGroup workerGroup;
    private NioEventLoop nioEventLoop;
    public ServerBootstrap() {

    }
    
    public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup workerGroup) {
        this.parentGroup = parentGroup;
        this.workerGroup = workerGroup;
        return this;
    }
    
    public ServerBootstrap serverSocketChannel(ServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
        return this;
    }

    public DefaultPromise<Object> bind(String host, int inetPort) {
        return bind(new InetSocketAddress(host,inetPort));
    }
    
    public DefaultPromise<Object> bind(int port) {
        return bind(DEFAULT_ADDRESS, port);
    }

    public DefaultPromise<Object> bind(SocketAddress localAddress) {
        return doBind(localAddress);
    }

    private DefaultPromise<Object> doBind(SocketAddress localAddress) {
        nioEventLoop = (NioEventLoop) parentGroup.next().next();
        nioEventLoop.setServerSocketChannel(serverSocketChannel);
        nioEventLoop.setWorkerGroup(workerGroup);
        nioEventLoop.register(serverSocketChannel, nioEventLoop);
        DefaultPromise<Object> defaultPromise = new DefaultPromise<>(nioEventLoop);
        doBind0(localAddress, defaultPromise);
        return defaultPromise;
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
