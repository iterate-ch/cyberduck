/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.util;

import java.io.Serializable;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public abstract class State implements Serializable {
    /**  */
    protected int state;

    /**
     * Creates a new State object.
     *
     * @param initialState
     */
    public State(int initialState) {
        this.state = initialState;
    }

    /**
     *
     *
     * @param state
     *
     * @return
     */
    public abstract boolean isValidState(int state);

    /**
     *
     *
     * @param state
     *
     * @throws InvalidStateException
     */
    public synchronized void setValue(int state) throws InvalidStateException {
        if (!isValidState(state)) {
            throw new InvalidStateException("The state is invalid");
        }

        this.state = state;
        notifyAll();
    }

    /**
     *
     *
     * @return
     */
    public synchronized int getValue() {
        return state;
    }

    /**
     *
     */
    public synchronized void breakWaiting() {
        notifyAll();
    }

    /**
     *
     *
     * @param state
     *
     * @return
     *
     * @throws InvalidStateException
     * @throws InterruptedException
     */
    public synchronized boolean waitForState(int state)
        throws InvalidStateException, InterruptedException {
        return waitForState(state, 0);
    }

    /**
     *
     *
     * @param state
     * @param timeout
     *
     * @return
     *
     * @throws InvalidStateException
     * @throws InterruptedException
     */
    public synchronized boolean waitForState(int state, long timeout)
        throws InvalidStateException, InterruptedException {
        if (!isValidState(state)) {
            throw new InvalidStateException("The state is invalid");
        }

        if (timeout < 0) {
            timeout = 0;
        }

        while (this.state != state) {
            //   try {
            wait(timeout);

            if (timeout != 0) {
                break;
            }

            //   } catch (InterruptedException e) {
            //        break;
            //    }
        }

        return this.state == state;
    }

    /**
     *
     *
     * @return
     *
     * @throws InterruptedException
     */
    public synchronized int waitForStateUpdate() throws InterruptedException {
        // try {
        wait();

        // } catch (InterruptedException ie) {
        //  }
        return state;
    }

    /*public synchronized boolean waitForStateUpdate(long timeout) {
        int oldstate = state;
        if(timeout<0)
     timeout=0;
        try {
     wait(timeout);
        } catch(InterruptedException ie) {
        }
        return !(oldstate==state);
      }*/
}
