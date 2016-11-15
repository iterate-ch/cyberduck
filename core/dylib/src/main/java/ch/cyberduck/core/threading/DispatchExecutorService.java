package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.ObjCObject;
import org.rococoa.Rococoa;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSArray;
import org.rococoa.cocoa.foundation.NSAutoreleasePool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * DispatchExecutorService runs tasks by passing them to Grand Central Dispatch.
 * Presently, every <code>DispatchExecutorService</code> creates its own
 * <code>NSOperationQueue</code> underneath.
 *
 * @author Andrew Thompson (lordpixel@mac.com)
 */
public class DispatchExecutorService extends AbstractExecutorService {
    /**
     * An Object-C selector representing a run() method
     */
    private static final Selector RUN_SELECTOR = Foundation.selector("run");
    /**
     * A permission meaning one thread can modify another
     */
    private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");

    /**
     * Represents the states this ExecutorService can be in
     */
    private enum State {
        RUNNING, SHUTDOWN, TERMINATED
    }

    /**
     * The underlying operation queue used to schedule tasks
     */
    private final NSOperationQueue queue;
    /**
     * The current state of this ExecutorService
     */
    private volatile State state = State.RUNNING;
    /**
     * A lock used to manage the state of the ExecutorService during the shutdown process
     */
    private final ReentrantLock shutdownLock = new ReentrantLock();
    /**
     * A signal condition used during the shutdown process
     */
    private final Condition shutdownCondition = shutdownLock.newCondition();
    /**
     * A Map used both to retain proxies for running tasks - so they are not collected - and to
     * support the shutdownNow() method
     */
    private final Map<ID, InvocationFutureTask<?>> tasks = new ConcurrentHashMap<ID, InvocationFutureTask<?>>();

    /**
     * Construct a new instance of the code <code>ExecutorService</code>, with its own underlying <code>NSOperationQueue</code>
     */
    public DispatchExecutorService() {
        queue = NSOperationQueue.CLASS.alloc().init();
    }

    public void shutdown() {
        SecurityManager sm = System.getSecurityManager();
        if(sm != null) {
            sm.checkPermission(shutdownPerm);
        }
        try {
            shutdownLock.lock();
            state = State.SHUTDOWN;
            terminateIfDone(queue.operationCount().intValue() == 0);
        }
        finally {
            shutdownLock.unlock();
        }
    }

    public List<Runnable> shutdownNow() {
        SecurityManager sm = System.getSecurityManager();
        if(sm != null) {
            sm.checkPermission(shutdownPerm);
        }
        return doWithAutoreleasePool(new Callable<List<Runnable>>() {
            public List<Runnable> call() {
                try {
                    shutdownLock.lock();
                    state = State.SHUTDOWN;
                    NSArray queuedTasks = queue.operations();
                    List<Runnable> result = new ArrayList<Runnable>(queuedTasks.count());
                    for(int i = 0; i < queuedTasks.count(); i++) {
                        NSOperation o = Rococoa.cast(queuedTasks.objectAtIndex(i), NSOperation.class);
                        InvocationFutureTask<?> task = tasks.get(o.id());
                        if(task != null && !(o.isFinished() || o.isCancelled())) {
                            result.add(task.getOriginalRunnable());
                        }
                    }
                    queue.cancelAllOperations();
                    tasks.clear();
                    terminateIfDone(queue.operationCount().intValue() == 0);
                    return result;
                }
                finally {
                    shutdownLock.unlock();
                }
            }
        });
    }

    public boolean isShutdown() {
        return state != State.RUNNING;
    }

    public boolean isTerminated() {
        return state == State.TERMINATED;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long wait = unit.toNanos(timeout);
        shutdownLock.lock();
        try {
            while(!isTerminated()) {
                if(wait <= 0) {
                    return false;
                }
                wait = shutdownCondition.awaitNanos(wait);
                terminateIfDone(queue.operationCount().intValue() == 0);
            }
            return true;
        }
        finally {
            shutdownLock.unlock();
        }
    }

    public void execute(Runnable command) {
        try {
            shutdownLock.lock();
            if(state != State.RUNNING) {
                throw new RejectedExecutionException("Executor is not running");
            }
            else {
                InvocationFutureTask<?> task = command instanceof InvocationFutureTask<?> ?
                        (InvocationFutureTask<?>) command :
                        (InvocationFutureTask<?>) newTaskFor(command, null);
                queue.addOperation(task.getInvocationOperation());
            }
        }
        finally {
            shutdownLock.unlock();
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new InvocationFutureTask<T>(runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return super.newTaskFor(callable);
    }

    /**
     * A future task that creates an NSInvocationOperation to run the Runnable
     * or Callable it is created with.
     *
     * @param <V> the type of the result of the task
     */
    private class InvocationFutureTask<V> extends FutureTask<V> {
        /**
         * The NSInvocationOperation that will be enqueued and executed
         */
        private final NSInvocationOperation invocation;
        /**
         * A reference to the Java proxy object created to allow Objective C
         * to callback methods on this class. It must be held because a crash
         * will occur if the Java proxy is collected and Objective-C attempts
         * to use it.
         */
        private final ObjCObject proxy;
        /**
         * The original runnable submitted by the caller of the ExecutorService, when a method
         * that takes a Runnable is used. Conversely, if a Callable is submitted for execution
         * then this is simply set to <code>this</code>.
         * This is ultimately for the shutdownNow() method - we try to return the original
         * submitted objects whenever possible.
         */
        private final Runnable originalRunnable;

        /**
         * Create an new invocation based future task to run the given <code>Runnable</code>
         *
         * @param r      the <code>Runnable</code> to run
         * @param result the result to return when the <code>Runnable</code> completes
         */
        public InvocationFutureTask(Runnable r, V result) {
            super(r, result);
            originalRunnable = r;
            proxy = Rococoa.proxy(this);
            invocation = createInvocation(proxy);
            tasks.put(invocation.id(), this);
        }

        /**
         * Create an new invocation based future task to run the given <code>Callable</code>
         *
         * @param callable the <code>Callable</code> to run
         */
        public InvocationFutureTask(Callable<V> callable) {
            super(callable);
            originalRunnable = this;
            proxy = Rococoa.proxy(this);
            invocation = createInvocation(proxy);
            tasks.put(invocation.id(), this);
        }

        private NSInvocationOperation createInvocation(final ObjCObject toInvoke) {
            return doWithAutoreleasePool(new Callable<NSInvocationOperation>() {
                public NSInvocationOperation call() {
                    NSInvocationOperation result = NSInvocationOperation.CLASS.alloc();
                    //when the NSOperationQueue executes the NSInvocationOperation, run() is
                    //called on this object.
                    result = result.initWithTarget_selector_object(toInvoke.id(), RUN_SELECTOR, null);
                    return result;
                }
            });
        }

        /**
         * Get the <code>NSInvocationOperation</code> created to run this task.
         *
         * @return the invocation operation that will be used to run this tak
         */
        public NSInvocationOperation getInvocationOperation() {
            return invocation;
        }

        /**
         * Get the original <code>Runnable</code> used to create this task,
         * will simply return <code>this</code> if the task was creaed with a
         * <code>Callable</code> instead.
         *
         * @return the original Runnable
         */
        public Runnable getOriginalRunnable() {
            return originalRunnable;
        }

        @Override
        public void run() {
            try {
                super.run();
            }
            finally {
                tasks.remove(invocation.id());
                if(state == State.SHUTDOWN) {
                    //i.e. this is the last item on the queue
                    terminateIfDone(queue.operationCount().intValue() <= 1);
                }
            }
        }
    }

    /**
     * If the ExecutorService has been shutdown and the work queue is empty,
     * transition to the <code>TERMINATED</code> state.
     *
     * @param queueEmpty pass true if the queue is currently empty.
     */
    private void terminateIfDone(boolean queueEmpty) {
        try {
            shutdownLock.lock();
            if(state == State.SHUTDOWN && queueEmpty) {
                shutdownCondition.signalAll();
                queue.setSuspended(true);
                state = State.TERMINATED;
            }
        }
        finally {
            shutdownLock.unlock();
        }
    }

    /**
     * Perform some code with an autorelease pool in place.
     *
     * @param <R>      the type returned when <code>callable</code> is run
     * @param callable the Callable code to run with a pool in place
     * @return the result of running the Callable
     */
    private static <R> R doWithAutoreleasePool(Callable<R> callable) {
        NSAutoreleasePool pool = null;
        try {
            pool = NSAutoreleasePool.new_();
            return callable.call();
        }
        catch(Exception e) {
            throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
        }
        finally {
            if(pool != null) {
                pool.drain();
            }
        }
    }
}
