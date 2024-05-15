package com.chenyu.netty.bootstrap;

import com.chenyu.netty.channel.EventLoop;
import com.chenyu.netty.channel.EventLoopGroup;
import com.chenyu.netty.channel.nio.NioEventLoop;
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

    public void bind(String host, int inetPort) {
        bind(new InetSocketAddress(host,inetPort));
    }
    
    public void bind(int port) {
        bind(DEFAULT_ADDRESS, port);
    }

    public void bind(SocketAddress localAddress) {
        doBind(localAddress);
    }

    private void doBind(SocketAddress localAddress) {
        nioEventLoop = (NioEventLoop) parentGroup.next().next();
        nioEventLoop.setServerSocketChannel(serverSocketChannel);
        nioEventLoop.setWorkerGroup(workerGroup);
        nioEventLoop.register(serverSocketChannel, nioEventLoop);
        doBind0(localAddress);
    }

    private void doBind0(SocketAddress localAddress) {
        nioEventLoop.execute(() -> {
            try {
                serverSocketChannel.bind(localAddress);
            } catch (Exception e) {
                logger.error("doBind0 error for address {}", localAddress);
            }
        });
    }

}
