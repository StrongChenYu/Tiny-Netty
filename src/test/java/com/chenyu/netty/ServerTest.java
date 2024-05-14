package com.chenyu.netty;

import com.chenyu.netty.bootstrap.ServerBootstrap;
import com.chenyu.netty.channel.nio.NioEventLoop;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

public class ServerTest {

//    public static void main(String[] args) throws IOException {
//        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//        NioEventLoop boss = new NioEventLoop(serverSocketChannel, null);
//        NioEventLoop worker = new NioEventLoop(serverSocketChannel, null);
//        boss.setWorker(worker);
//        
//        ServerBootstrap serverBootstrap = new ServerBootstrap();
//        serverBootstrap.nioEventLoop(boss);
//        serverBootstrap.serverSocketChannel(serverSocketChannel);
//        
//        serverBootstrap.bind(8080);
//    }
    
}
