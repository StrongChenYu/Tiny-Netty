package com.chenyu.netty;

import com.chenyu.netty.bootstrap.ServerBootstrap;
import com.chenyu.netty.channel.nio.NioEventLoop;
import com.chenyu.netty.channel.nio.NioEventLoopGroup;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

public class ServerTest {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup(2);
        serverBootstrap.group(bossGroup, workGroup).serverSocketChannel(serverSocketChannel);
        serverBootstrap.bind("127.0.0.1", 8080);
    }
    
}
