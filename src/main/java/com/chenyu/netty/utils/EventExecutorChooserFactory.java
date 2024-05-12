package com.chenyu.netty.utils;

public interface EventExecutorChooserFactory {
    
    EventExecutorChooser newChooser(EventExecutor[] executors);
    
    interface EventExecutorChooser {
        
        EventExecutor next();
    }
}
