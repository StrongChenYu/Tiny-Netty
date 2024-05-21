package com.chenyu.netty;

import com.chenyu.netty.bootstrap.ServerBootstrap;
import com.chenyu.netty.channel.nio.NioEventLoop;
import com.chenyu.netty.channel.nio.NioEventLoopGroup;
import com.chenyu.netty.utils.concurrent.DefaultPromise;
import com.chenyu.netty.utils.concurrent.Future;
import com.chenyu.netty.utils.concurrent.GenericFutureListener;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

public class ServerTest {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup(2);
        serverBootstrap.group(bossGroup, workGroup).serverSocketChannel(serverSocketChannel);
        DefaultPromise<Object> bindPromise = serverBootstrap.bind("127.0.0.1", 8080);

        bindPromise.addListener(new GenericFutureListener<Future<? super Object>>() {
            @Override
            public void operationComplete(Future<? super Object> future) throws Exception {
                System.out.println("operation finish");
            }
        });
    }
    
}
