package com.chenyu.netty.test;

import com.chenyu.netty.bootstrap.ServerBootstrap;
import com.chenyu.netty.channel.nio.NioEventLoop;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

public class ServerTest {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        NioEventLoop nioEventLoop = new NioEventLoop(serverSocketChannel, null);
        
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.nioEventLoop(nioEventLoop);
        serverBootstrap.serverSocketChannel(serverSocketChannel);
        
        serverBootstrap.bind(8080);
    }
}
