/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.connection;

import org.apache.log4j.Logger;


/**
 * A synchronized object to manage the channels window space
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class ChannelDataWindow {
    private static Logger log = Logger.getLogger(ChannelDataWindow.class);
    long windowSpace = 0;

    /**
     * The constructor
     */
    public ChannelDataWindow() {
    }

    /**
     * Returns the current window space
     *
     * @return the total number of bytes available for the remote computer to
     *         send
     */
    public synchronized long getWindowSpace() {
        return windowSpace;
    }

    /**
     * Consumes window space
     *
     * @param count the number of bytes to consume
     */
    public synchronized long consumeWindowSpace(int count) {
        if (windowSpace<count) {
            waitForWindowSpace(count);
        }

        windowSpace -= count;

        return windowSpace;
    }

    /**
     * Increases the window space
     *
     * @param count the number of bytes to add
     */
    public synchronized void increaseWindowSpace(long count) {
        if (log.isDebugEnabled()) {
            log.debug("Increasing window space by " + String.valueOf(count));
        }

        windowSpace += count;

        notifyAll();
    }

    /**
     * Waits until the specified number of bytes is available in the window
     *
     * @param minimum the minumum number of bytes needed
     */
    public synchronized void waitForWindowSpace(int minimum) {
        if (log.isDebugEnabled()) {
            log.debug("Waiting for " + String.valueOf(minimum)
                      + " bytes of window space");
        }

        while (windowSpace<minimum) {
            try {
                wait(50);
            } catch (InterruptedException e) {
            }
        }
    }
}
