package com.chenyu.netty.utils.concurrent;

import com.chenyu.netty.utils.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultThreadFactory.class);

    private static final AtomicInteger POOL_ID = new AtomicInteger();

    private final AtomicInteger nextId = new AtomicInteger();
    private final String prefix;
    private final boolean daemon;
    private final int priority;
    protected final ThreadGroup threadGroup;

    public DefaultThreadFactory() {
        this(DefaultThreadFactory.class);
    }

    public DefaultThreadFactory(Class<?> poolType) {
        this(poolType, false, Thread.NORM_PRIORITY);
    }

    public DefaultThreadFactory(Class<?> poolType, boolean daemon, int normPriority) {
        this(toPoolName(poolType), daemon, normPriority);
    }
    
    public DefaultThreadFactory(Class<?> poolType, int priority) {
        this(poolType, false, priority);
    }

    public DefaultThreadFactory(String poolName, boolean daemon, int priority) {
        this(poolName, daemon, priority, System.getSecurityManager() == null ?
                Thread.currentThread().getThreadGroup() : System.getSecurityManager().getThreadGroup());
    }

    public DefaultThreadFactory(String poolName, boolean daemon, int priority, ThreadGroup threadGroup) {
        if (poolName == null) {
            throw new NullPointerException("poolName");
        }

        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(
                    "priority: " + priority + " (expected: Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY)");
        }

        //给属性赋值
        prefix = poolName + '-' + POOL_ID.incrementAndGet() + '-';
        this.daemon = daemon;
        this.priority = priority;
        this.threadGroup = threadGroup;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, prefix + nextId.incrementAndGet());
        logger.info("thread create error {}", thread);
        try {
            if (thread.isDaemon() != daemon) {
                thread.setDaemon(daemon);
            }

            if (thread.getPriority() != priority) {
                thread.setPriority(priority);
            }

        } catch (Exception e) {
            logger.error("set thread property error {}", thread, e);
        }

        return thread;
    }


    /**
     * 得到线程池的名字
     * @param poolType
     * @return
     */
    public static String toPoolName(Class<?> poolType) {
        if (poolType == null) {
            throw new NullPointerException("poolType");
        }

        String poolName = StringUtil.simpleClassName(poolType);
        switch (poolName.length()) {
            case 0:
                return "unknown";
            case 1:
                return poolName.toLowerCase(Locale.US);
            default:
                if (Character.isUpperCase(poolName.charAt(0)) && Character.isLowerCase(poolName.charAt(1))) {
                    return Character.toLowerCase(poolName.charAt(0)) + poolName.substring(1);
                } else {
                    return poolName;
                }
        }
    }
}
