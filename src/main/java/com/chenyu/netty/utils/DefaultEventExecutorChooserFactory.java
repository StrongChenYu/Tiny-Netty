package com.chenyu.netty.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultEventExecutorChooserFactory implements EventExecutorChooserFactory {
    
    @Override
    public EventExecutorChooser newChooser(EventExecutor[] executors) {
        return null;
    }
    
    
    public static class PowerOfTwoEventExecutorChooser implements EventExecutorChooser {

        private final AtomicInteger idx = new AtomicInteger();
        private final EventExecutor[] eventExecutors;
        
        public PowerOfTwoEventExecutorChooser(EventExecutor[] eventExecutors) {
            this.eventExecutors = eventExecutors;
        }
        
        @Override
        public EventExecutor next() {
            return eventExecutors[idx.incrementAndGet() & eventExecutors.length - 1];
        }
    }
    
    public static class GenericEventExecutorChooser implements  EventExecutorChooser {
        
        private final AtomicInteger idx = new AtomicInteger();
        private final EventExecutor[] eventExecutors;

        public GenericEventExecutorChooser(EventExecutor[] eventExecutors) {
            this.eventExecutors = eventExecutors;
        }

        @Override
        public EventExecutor next() {
            return eventExecutors[idx.incrementAndGet() % eventExecutors.length];
        }
    }
}
