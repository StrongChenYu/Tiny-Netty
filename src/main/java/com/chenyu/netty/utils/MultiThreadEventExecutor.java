package com.chenyu.netty.utils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadEventExecutor extends AbstractEventExecutorGroup {
    
    private final EventExecutor[] children;
    private final Set<EventExecutor> readonlyChildren;
    private final AtomicInteger terminatedChildren = new AtomicInteger;
    private final EventExecutorChooserFactory.EventExecutorChooser chooser;
    
    @Override
    public EventExecutor next() {
        return null;
    }
}
