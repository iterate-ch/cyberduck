package ch.cyberduck.core.threading;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class BackgroundActionRegistry extends AbstractActionRegistry<BackgroundAction> {
    private static Logger log = Logger.getLogger(BackgroundActionRegistry.class);

    private static BackgroundActionRegistry instance = null;

    private static final Object lock = new Object();

    public static BackgroundActionRegistry instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new BackgroundActionRegistry();
            }
            return instance;
        }
    }

    private BackgroundAction current;

    /**
     * @return The currently running background action. Null if none is currently running.
     */
    public BackgroundAction getCurrent() {
        return current;
    }

    @Override
    public boolean add(final BackgroundAction action) {
        action.addListener(new BackgroundActionListener() {
            public void start(BackgroundAction action) {
                current = action;
            }

            public void cancel(BackgroundAction action) {
                remove(action);
            }

            public void stop(BackgroundAction action) {
                current = null;
                action.removeListener(this);
                remove(action);
                synchronized(block) {
                    block.notify();
                }
            }
        });
        return super.add(action);
    }

    private BackgroundActionRegistry() {
        ;
    }

    private final Object block = new Object();

    /**
     * Blocks the calling thread until all background actions have completed
     */
    public void block() {
        while(!this.isEmpty()) {
            synchronized(block) {
                try {
                    log.info("Waiting for all background actions to complete...");
                    block.wait();
                }
                catch(InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        }
        log.info("All background actions completed");
    }
}
