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

/**
 * An abstract state object providing get, set and wait methods to manage the
 * state of an object
 *
 * @author Lee David Painter
 * @version $Id$
 *
 * @created 20 December 2002
 */
public abstract class State {
    /** the state value */
    protected int state;

    /**
     * Create the state
     *
     * @param initialState The initial state
     */
    public State(int initialState) {
        this.state = initialState;
    }

    /**
     * Determines whether the state is allowed for this instance
     *
     * @param state
     *
     * @return <tt>true</tt> if the state is valid otherwise <tt>false </tt>
     */
    public abstract boolean isValidState(int state);

    /**
     * Set the state to the new value
     *
     * @param state
     *
     * @throws InvalidStateException if the state is an invalid value
     */
    public synchronized void setValue(int state)
                                throws InvalidStateException {
        if (!isValidState(state)) {
            throw new InvalidStateException("The state is invalid");
        }

        this.state = state;
        notifyAll();
    }

    /**
     * Get the current state
     *
     * @return the current state value
     */
    public synchronized int getValue() {
        return state;
    }

    /**
     * Breaks any waiting threads by notifying them
     */
    public synchronized void breakWaiting() {
        notifyAll();
    }

    /**
     * Wait for the specified state
     *
     * @param state
     *
     * @throws InvalidStateException if the state value is invalid
     */
    public synchronized void waitForState(int state)
                                   throws InvalidStateException {
        if (!isValidState(state)) {
            throw new InvalidStateException("The state is invalid");
        }

        while (this.state!=state) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Waits for the state to be updated
     *
     * @return the new state value
     */
    public synchronized int waitForStateUpdate() {
        try {
            wait();
        } catch (InterruptedException ie) {
        }

        return state;
    }
}
