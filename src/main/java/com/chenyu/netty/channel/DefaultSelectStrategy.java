package com.chenyu.netty.channel;

import java.util.function.IntSupplier;

public class DefaultSelectStrategy implements SelectStrategy {

    static final SelectStrategy INSTANCE = new DefaultSelectStrategy();

    private DefaultSelectStrategy() { }

    @Override
    public int calculateStrategy(IntSupplier selectSupplier, boolean hasTasks) throws Exception {
        return hasTasks ? selectSupplier.getAsInt() : SelectStrategy.SELECT;
    }
}
