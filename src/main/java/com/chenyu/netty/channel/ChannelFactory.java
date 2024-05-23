package com.chenyu.netty.channel;



public interface ChannelFactory<T extends Channel> {


    T newChannel();
}
