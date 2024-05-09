package com.chenyu.netty.channel.nio;

import com.chenyu.netty.channel.SingleThreadEventLoop;
import org.apache.log4j.Logger;

import java.nio.channels.Selector;

public class NioEventLoop extends SingleThreadEventLoop {
    
    private static final Logger logger = Logger.getLogger(NioEventLoop.class);
    
    private Selector selector;
    
    public Selector unwrappedSelector() {
        return selector;
    }
}
