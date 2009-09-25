package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.threading.MainActionRegistry;
import ch.cyberduck.ui.AbstractController;
import ch.cyberduck.ui.cocoa.foundation.NSNotificationCenter;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSThread;
import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.internal.AutoreleaseBatcher;
import org.rococoa.internal.OperationBatcher;

/**
 * @version $Id$
 */
public class CDController extends AbstractController {
    private static Logger log = Logger.getLogger(CDController.class);

    /**
     * You need to keep a reference to the returned value for as long as it is
     * active. When it is GCd, it will release the Objective-C proxy.
     */
    private NSObject proxy;

    private ID id;

    public NSObject proxy() {
        return this.proxy(NSObject.class);
    }

    public NSObject proxy(Class<? extends NSObject> type) {
        if(null == proxy) {
            proxy = Rococoa.proxy(this, type);
        }
        return proxy;
    }

    public org.rococoa.ID id() {
        return this.id(NSObject.class);
    }

    public org.rococoa.ID id(Class<? extends NSObject> type) {
        if(null == id) {
            id = this.proxy(type).id();
        }
        return id;
    }

    /**
     * Free all locked resources by this controller; also remove me from all observables;
     * marks this controller to be garbage collected as soon as needed
     */
    protected void invalidate() {
        if(log.isDebugEnabled()) {
            log.debug("invalidate:" + this.toString());
        }
        if(id != null) {
            NSNotificationCenter.defaultCenter().removeObserver(id);
        }
    }

    @Override
    protected void finalize() throws java.lang.Throwable {
        if(log.isTraceEnabled()) {
            log.trace("finalize:" + this.toString());
        }
        super.finalize();
    }

    /**
     * An autorelease pool is used to manage Foundation's autorelease
     * mechanism for Objective-C objects. If you start off a thread
     * that calls Cocoa, there won't be a top-level pool.
     *
     * @return
     */
    @Override
    protected OperationBatcher getBatcher(int size) {
        return AutoreleaseBatcher.forThread(size);
    }

    /**
     * You can use this method to deliver messages to the main thread of your application. The main thread
     * encompasses the application’s main run loop, and is where the NSApplication object receives
     * events. The message in this case is a method of the current object that you want to execute
     * on the thread.
     * <p/>
     * Execute the passed <code>Runnable</code> on the main thread also known as NSRunLoop.DefaultRunLoopMode
     *
     * @param runnable The <code>Runnable</code> to run
     * @param wait     Block until execution on main thread exits. A Boolean that specifies whether the current
     *                 thread blocks until after the specified selector is performed on the receiver on the main thread.
     *                 Specify YES to block this thread; otherwise, specify NO to have this method return immediately.
     *                 If the current thread is also the main thread, and you specify YES for this parameter,
     *                 the message is delivered and processed immediately.
     */
    public void invoke(final MainAction runnable, final boolean wait) {
        if(log.isDebugEnabled()) {
            log.debug("invoke:" + runnable);
        }
        if(this.isMainThread()) {
            log.debug("Already on main thread. Invoke " + runnable + " directly.");
            runnable.run();
            return;
        }
        final MainActionRegistry registry = MainActionRegistry.instance();
        final MainAction main = new MainAction() {
            @Override
            public boolean isValid() {
                return runnable.isValid();
            }

            public void run() {
                final NSAutoreleasePool pool = NSAutoreleasePool.push();
                try {
                    runnable.run();
                }
                finally {
                    //Remove strong reference
                    registry.remove(this);
                    pool.drain();
                }
            }
        };
        //Make sure to keep a strong reference
        registry.add(main);
        //Defer to main thread
        Foundation.runOnMainThread(main, wait);
    }

    /**
     * @return True if the current thread is not a background worker thread
     */
    public boolean isMainThread() {
        return NSThread.isMainThread();
    }
}