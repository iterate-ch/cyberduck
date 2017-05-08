package ch.cyberduck.binding;

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

import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.threading.MainAction;

import org.apache.log4j.Logger;
import org.rococoa.ID;

public class ProxyController extends AbstractController {
    private static final Logger log = Logger.getLogger(ProxyController.class);

    private final Proxy proxy = new Proxy(this);

    public ID id() {
        return proxy.id();
    }

    /**
     * Free all locked resources by this controller; also remove me from all observables;
     * marks this controller to be garbage collected as soon as needed
     */
    public void invalidate() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Invalidate controller %s", this));
        }
        proxy.invalidate();
    }

    /**
     * You can use this method to deliver messages to the main thread of your application. The main thread
     * encompasses the application’s main run loop, and is where the NSApplication object receives
     * events. The message in this case is a method of the current object that you want to execute
     * on the thread.
     * <p>
     * Execute the passed <code>Runnable</code> on the main thread also known as NSRunLoop.DefaultRunLoopMode
     *
     * @param runnable The <code>Runnable</code> to run
     * @param wait     Block until execution on main thread exits. A Boolean that specifies whether the current
     *                 thread blocks until after the specified selector is performed on the receiver on the main thread.
     *                 Specify YES to block this thread; otherwise, specify NO to have this method return immediately.
     *                 If the current thread is also the main thread, and you specify YES for this parameter,
     *                 the message is delivered and processed immediately.
     */
    @Override
    public void invoke(final MainAction runnable, final boolean wait) {
        if(!runnable.isValid()) {
            return;
        }
        proxy.invoke(runnable, runnable.lock(), wait);
    }
}