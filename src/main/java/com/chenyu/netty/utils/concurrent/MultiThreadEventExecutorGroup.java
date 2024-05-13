package com.chenyu.netty.utils.concurrent;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MultiThreadEventExecutorGroup extends AbstractEventExecutorGroup {
    
    private final EventExecutor[] children;
    private final Set<EventExecutor> readonlyChildren;
    private final AtomicInteger terminatedChildren = new AtomicInteger();
    private final EventExecutorChooserFactory.EventExecutorChooser chooser;
    
    protected MultiThreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        this(nThreads, threadFactory == null ? null : new ThreadPerTaskExecutor(threadFactory), args);
    }
    
    protected MultiThreadEventExecutorGroup(int nThreads, Executor executor, Object... args) {
        this(nThreads, executor, DefaultEventExecutorChooserFactory.INSTANCE, args);
    }
    
    protected MultiThreadEventExecutorGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args) {
        if (nThreads <= 0) {
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        }
        
        if (executor == null) {
            executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
        }
        
        children = new EventExecutor[nThreads];
        for (int i = 0; i < nThreads; i++) {
            boolean success = false;
            try {
                children[i] = newChild(executor, args);
                success = true;
            } catch (Exception e) {
                throw new IllegalStateException("failed to create a child event loop", e);
            } finally {
                if (!success) {
                    for (int j = 0; j < i; j++) {
                        // 把之前的释放掉
                        children[i].shutdownGracefully();
                    }
                    
                    for (int j = 0; j < i; j++) {
                        EventExecutor e = children[i];
                        
                        try {
                            while (!e.isTerminated()) {
                                e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }
        
        chooser = chooserFactory.newChooser(children);
        
        // 生成一个只读的EventExecutor列表
        Set<EventExecutor> childrenSet = new LinkedHashSet<>(children.length);
        Collections.addAll(childrenSet, children);
        readonlyChildren = Collections.unmodifiableSet(childrenSet);
    }

    @Override
    public EventExecutor next() {
        return chooser.next();
    }

    protected ThreadFactory newDefaultThreadFactory() {
        return new DefaultThreadFactory(getClass());
    }

    protected abstract EventExecutor newChild(Executor executor, Object... args) throws Exception;

    @Override
    public void shutdownGracefully() {
        for (EventExecutor child : children) {
            child.shutdownGracefully();
        }
    }

    public final int executorCount() {
        return children.length;
    }
}
