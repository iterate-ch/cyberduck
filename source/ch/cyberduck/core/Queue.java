package ch.cyberduck.core;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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
 * Used to queue multiple connections. <code>queue.start()</code> will
 * start the the connections in the order the have been added to me.
 * Useful for actions where the reihenfolge of the taken actions
 * is important, i.e. deleting directories or uploading directories.
 * @version $Id$
 */
public class Queue extends Thread {

    private static Logger log = Logger.getLogger(Queue.class);

    java.util.Vector connections = new java.util.Vector();

    /**
     * @param thread A connection thread
     * @see ch.cyberduck.core.Session
     */
    public void add(Runnable thread) {
        log.debug("[Queue] add()");
        connections.add(thread);
    }

    /**
     * Execute pending connections in the order they have been added to the queue - first added gets first
     * executed.
     */
    public void run() {
        java.util.Iterator i = connections.iterator();
        Thread thread = null;
        while (i.hasNext()) {
            if(thread != null) {
                if(thread.isAlive()) {
                    try {
                        thread.join();
                    }
                    catch(InterruptedException e) {
                        log.debug("[Queue] ERROR: Failed joining last connection.");
                        e.printStackTrace();
                    }
                }
            }
            thread = (Thread)i.next();
            log.debug("[Queue] Starting new thread");
            thread.start();
        }
    }
}
