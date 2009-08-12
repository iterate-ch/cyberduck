package ch.cyberduck.ui;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;

import org.apache.log4j.Logger;
import org.rococoa.internal.OperationBatcher;

/**
 * @version $Id$
 */
public abstract class AbstractController implements Controller {
    private static Logger log = Logger.getLogger(AbstractController.class);

    public void invoke(MainAction runnable) {
        this.invoke(runnable, false);
    }

    /**
     * Will queue up the <code>BackgroundAction</code> to be run in a background thread. Will be executed
     * as soon as no other previous <code>BackgroundAction</code> is pending.
     * Will return immediatly but not run the runnable before the lock of the runnable is acquired.
     *
     * @param runnable The runnable to execute in a secondary Thread
     * @see java.lang.Thread
     * @see ch.cyberduck.core.threading.BackgroundAction#lock()
     */
    public void background(final BackgroundAction runnable) {
        log.debug("background:" + runnable);
        runnable.init();
        // Start background task
        new Thread("Background") {
            @Override
            public void run() {
                // Synchronize all background threads to this lock so actions run
                // sequentially as they were initiated from the main interface thread
                synchronized(runnable.lock()) {
                    log.info("Acquired lock for background runnable:" + runnable);

                    final OperationBatcher autorelease = AbstractController.this.getBatcher();
                    try {
                        if(runnable.prepare()) {
                            // Execute the action of the runnable
                            runnable.run();
                        }
                    }
                    finally {
                        // Increase the run counter
                        runnable.finish();
                        // Invoke the cleanup on the main thread to let the action synchronize the user interface
                        invoke(new DefaultMainAction() {
                            public void run() {
                                runnable.cleanup();
                            }
                        });
                        autorelease.operate();
                        log.info("Releasing lock for background runnable:" + runnable);
                    }
                }
            }
        }.start();
        log.info("Started background runnable:" + runnable);
    }

    private final OperationBatcher b = new OperationBatcher(0) {
        @Override
        protected void operation() {
            ;
        }

        @Override
        protected void reset() {
            ;
        }
    };

    /**
     * @return
     */
    protected OperationBatcher getBatcher() {
        return b;
    }
}
