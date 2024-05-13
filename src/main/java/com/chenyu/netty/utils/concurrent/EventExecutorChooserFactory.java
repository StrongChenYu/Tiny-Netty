package com.chenyu.netty.utils.concurrent;

public interface EventExecutorChooserFactory {
    
    EventExecutorChooser newChooser(EventExecutor[] executors);
    
    interface EventExecutorChooser {
        
        EventExecutor next();
    }
}
