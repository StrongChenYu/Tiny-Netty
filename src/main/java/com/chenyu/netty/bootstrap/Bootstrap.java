package com.chenyu.netty.bootstrap;

import com.chenyu.netty.channel.nio.NioEventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Bootstrap {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    private NioEventLoop nioEventLoop;

    private SocketChannel socketChannel;

    public Bootstrap() {

    }

    public Bootstrap nioEventLoop(NioEventLoop nioEventLoop) {
        this.nioEventLoop = nioEventLoop;
        return this;
    }

    public Bootstrap socketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        return this;
    }
    
    public Bootstrap nioEventLoop(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        return this;
    }
    
    

    public void connect(String host, int port) {
        doConnect(new InetSocketAddress(host, port));
    }

    private void doConnect(InetSocketAddress socketAddress) {
        nioEventLoop.register(socketChannel, this.nioEventLoop);

        doConnect0(socketAddress);
    }

    private void doConnect0(InetSocketAddress socketAddress) {
        nioEventLoop.execute(() -> {
            try {
                socketChannel.connect(socketAddress);
            } catch (Exception e) {
                logger.error("socketChannel connect error {}", socketAddress, e);
            }
        });
    }


}
