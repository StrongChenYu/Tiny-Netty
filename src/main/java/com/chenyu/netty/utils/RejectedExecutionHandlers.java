package com.chenyu.netty.utils;


import java.util.concurrent.RejectedExecutionException;

/**
 * @author chen yu
 * @since 2024.5.9
 */
public class RejectedExecutionHandlers {

    private static final RejectedExecutionHandler REJECT = (task, executor) -> {
        throw new RejectedExecutionException();
    };

    private RejectedExecutionHandlers() {

    }

    public static RejectedExecutionHandler reject() {
        return REJECT;
    }
}
