package com.chenyu.netty.utils.concurrent;

import com.chenyu.netty.channel.EventLoopTaskQueueFactory;
import com.chenyu.netty.utils.internal.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class SingleThreadEventExecutor implements EventExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventExecutor.class);

    private static final int ST_NOT_START = 1;
    private static final int ST_START = 2;
    private volatile int state = ST_NOT_START;
    protected static final int DEFAULT_MAX_PENDING_TASKS = Integer.MAX_VALUE;
    private final Queue<Runnable> taskQueue;
    private volatile Thread thread;
    private Executor executor;
    private volatile boolean interrupted;
    private final RejectedExecutionHandler rejectedExecutionHandler;
    private static final AtomicIntegerFieldUpdater<SingleThreadEventExecutor> STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "state");

    private EventExecutorGroup parent;
    private boolean addTaskWakeUp;
    protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor,
                                        boolean addTaskWakeUp, Queue<Runnable> taskQueue,
                                        RejectedExecutionHandler rejectedExecutionHandler) {
        this.parent = parent;
        this.addTaskWakeUp = addTaskWakeUp;
        this.executor = executor;
        this.taskQueue = ObjectUtil.checkNotNull(taskQueue, "taskQueue");
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }
    
    private Queue<Runnable> newTaskQueue(int defaultMaxPendingTasks) {
        return new LinkedBlockingQueue<>(defaultMaxPendingTasks);
    }

    abstract protected void run();

    @Override
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task");
        }

        //把任务提交到任务队列中
        addTask(task);
        //启动单线程执行器中的线程
        startThread();
    }

    private void startThread() {
        if (state == ST_NOT_START) {
            if (STATE_UPDATER.compareAndSet(this, ST_NOT_START, ST_START)) {
                // set success
                boolean success = false;
                try {
                    doStartThread();
                    success = true;
                } finally {
                    if (!success) {
                        logger.error("SingleThreadEventExecutor doStartThread error");
                        STATE_UPDATER.compareAndSet(this, ST_START, ST_NOT_START);
                    }
                }
            }
        }
    }

    private void doStartThread() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                thread = Thread.currentThread();
                if (interrupted) {
                    thread.interrupt();
                }
                
                SingleThreadEventExecutor.this.run();
                logger.info("SingleThreadEventExecutor run end not expect");
            }
        });
    }
    
    private void addTask(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        //如果添加失败，执行拒绝策略
        if (!offerTask(task)) {
            reject(task);
        }
    }

    private void reject(Runnable task) {
        rejectedExecutionHandler.reject(task, this);
    }

    final boolean offerTask(Runnable task) {
        return taskQueue.offer(task);
    }

    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }

    protected boolean hasTasks() {
        if (taskQueue.isEmpty()) {
            logger.info("SingleThreadEventExecutor has no task!");
            return false;
        } else {
            return true;
        }
    }

    protected void runAllTasks() {
        runAllTasksFrom(taskQueue);
    }

    protected void runAllTasksFrom(Queue<Runnable> taskQueue) {
        //从任务对立中拉取任务,如果第一次拉取就为null，说明任务队列中没有任务，直接返回即可
        Runnable task = pollTaskFrom(taskQueue);
        if (task == null) {
            return;
        }
        for (;;) {
            //执行任务队列中的任务
            safeExecute(task);
            //执行完毕之后，拉取下一个任务，如果为null就直接返回
            task = pollTaskFrom(taskQueue);
            if (task == null) {
                return;
            }
        }
    }

    private Runnable pollTaskFrom(Queue<Runnable> taskQueue) {
        return taskQueue.poll();
    }

    private void safeExecute(Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            logger.warn("A task raised an exception. Task: {}", task, t);
        }
    }

    protected static void reject() {
        throw new RejectedExecutionException("event executor terminated");
    }

    /**
     * 中断当前线程
     */
    protected void interruptThread() {
        Thread currentThread = thread;
        if (currentThread == null) {
            interrupted = true;
        } else {
            //中断线程并不是直接让该线程停止运行，而是提供一个中断信号
            //也就是标记，想要停止线程仍需要在运行流程中结合中断标记来判断
            currentThread.interrupt();
        }
    }


    @Override
    public void shutdownGracefully() {

    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void awaitTermination(Integer integer, TimeUnit timeUnit) throws InterruptedException{

    }
}
