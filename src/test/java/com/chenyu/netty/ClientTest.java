package com.chenyu.netty;

import com.chenyu.netty.channel.nio.NioEventLoop;
import com.chenyu.netty.bootstrap.Bootstrap;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ClientTest {

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        NioEventLoop nioEventLoop = new NioEventLoop(null, socketChannel);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.nioEventLoop(nioEventLoop);
        bootstrap.socketChannel(socketChannel);
        
        bootstrap.connect("127.0.0.1", 8080);
    }
}
