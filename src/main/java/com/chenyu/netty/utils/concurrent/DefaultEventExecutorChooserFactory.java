package com.chenyu.netty.utils.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultEventExecutorChooserFactory implements EventExecutorChooserFactory {

    public static final DefaultEventExecutorChooserFactory INSTANCE = new DefaultEventExecutorChooserFactory();

    private DefaultEventExecutorChooserFactory() {}
    
    @Override
    public EventExecutorChooser newChooser(EventExecutor[] executors) {
        if (isPowerOfTwo(executors.length)) {
            return new PowerOfTwoEventExecutorChooser(executors);
        } else {
            return new GenericEventExecutorChooser(executors);
        }
    }

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
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
