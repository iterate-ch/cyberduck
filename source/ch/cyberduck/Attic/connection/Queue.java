package ch.cyberduck.connection;

/*
 *  ch.cyberduck.connection.TransferAction.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.Cyberduck;

/**
 * Used to queue multiple connections. <code>queue.start()</code> will
 * start the the connections in the order the have been added to me.
 * Useful for actions where the reihenfolge of the taken actions
 * is important, i.e. deleting directories or uploading directories.
 * @version $Id$
 */
public class Queue extends Thread {
    
    java.util.Vector connections = new java.util.Vector();

    /**
     * @param thread A connection thread
     * @see ch.cyberduck.connection.Session
     */
    public void add(Runnable thread) {
        Cyberduck.DEBUG("[Queue] add()");
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
                        Cyberduck.DEBUG("[Queue] ERROR: Failed joining last connection.");
                        e.printStackTrace();
                    }
                }
            }
            thread = (Thread)i.next();
            Cyberduck.DEBUG("[Queue] Starting new thread");
            thread.start();
        }
    }
}
