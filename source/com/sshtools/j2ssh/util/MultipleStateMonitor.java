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
package com.sshtools.j2ssh.util;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author unascribed
 * @version 1.0
 *
 * @created 20 December 2002
 */
public class MultipleStateMonitor {
    private List monitors = new Vector();
    private List states = new Vector();
    private State changed = null;

    /**
     * Creates a new MultipleStateMonitor object.
     */
    public MultipleStateMonitor() {
    }

    /**
     * Adds a state and a state value to the monitor
     *
     * @param state
     */
    public void addState(State state) {
        states.add(state);
    }

    /**
     * DOCUMENT ME!
     */
    public synchronized void breakWaiting() {
        notifyAll();
    }

    /**
     * Waits for any of the states to change
     *
     * @return
     */
    public synchronized State monitor() {
        monitors.clear();

        Iterator it = states.iterator();

        while (it.hasNext()) {
            monitors.add(new StateMonitor((State) it.next(), this));
        }

        try {
            wait();
        } catch (InterruptedException e) {
        }

        return changed;
    }

    /**
     * DOCUMENT ME!
     */
    public synchronized void updateState() {
        notifyAll();
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision$
     *
     * @created 20 December 2002
     */
    class StateMonitor
        implements Runnable {
        private MultipleStateMonitor monitor;
        private State state;
        private Thread thread;
        private boolean active = false;

        /**
         * Creates a new StateMonitor object.
         *
         * @param state DOCUMENT ME!
         * @param monitor DOCUMENT ME!
         */
        public StateMonitor(State state, MultipleStateMonitor monitor) {
            this.state = state;
            this.monitor = monitor;
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        /**
         * DOCUMENT ME!
         */
        public void close() {
            active = false;
            monitor.breakWaiting();
        }

        /**
         * DOCUMENT ME!
         */
        public void run() {
            active = true;

            while (active) {
                state.waitForStateUpdate();

                if (active) {
                    monitor.updateState();
                }
            }
        }
    }
}
