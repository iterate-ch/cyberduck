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
package com.sshtools.j2ssh.session;

import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelEventListener;


/**
 * <p/>
 * This class provides a utility to read and parse the output a session,
 * providing methods to wait for specific strings such as the prompt or
 * command input requests.
 * </p>
 *
 * @author Lee David Painter
 * @version $Revision$
 * @since 0.2.1
 */
public class SessionOutputReader {
    SessionChannelClient session;
    int pos = 0;
    int mark = 0;
    String output = "";

    /**
     * <p/>
     * Contructs the session reader.
     * </p>
     *
     * @param session the to read
     */
    public SessionOutputReader(SessionChannelClient session) {
        this.session = session;
        session.addEventListener(new SessionOutputListener());
    }

    /**
     * Returns the output of the entire session.
     *
     * @return a string containing the entire output of the session so far.
     */
    public String getOutput() {
        return output;
    }

    /**
     * <p/>
     * Returns the current position of the session input pointer. This pointer
     * is set to the position of the matched string everytime a match is found
     * during a call by <code>waitForString</code>
     * </p>
     *
     * @return the current input reader pointer
     */
    public int getPosition() {
        return pos;
    }

    /**
     * Mark the postion specified for filtering session output.
     *
     * @param mark output position to mark
     */
    public void markPosition(int mark) {
        this.mark = mark;
    }

    /**
     * Marks the current position.
     */
    public void markCurrentPosition() {
        this.mark = pos;
    }

    /**
     * <p/>
     * Returns a string containing the session output from the current marked
     * position to the end of the output.
     * </p>
     *
     * @return a string containing the session output from the marked position
     *         to current position
     */
    public String getMarkedOutput() {
        return output.substring(mark, pos);
    }

    /**
     * <p/>
     * Wait for a given String in the output buffer.
     * </p>
     *
     * @param str  the string to wait for
     * @param echo a callback interface to receive the session output whilst
     *             the no match for the string is found
     * @return true if the string was found, otherwise false
     * @throws InterruptedException if the thread is interrupted
     * @see waitForString(String, int, SessionOutputEcho)
     */
    public synchronized boolean waitForString(String str, SessionOutputEcho echo)
            throws InterruptedException {
        return waitForString(str, 0, echo);
    }

    /**
     * <p/>
     * Wait for a given String in the output buffer. This method will block
     * until the string is found.
     * </p>
     *
     * @param str the string to wait for
     * @return true if the string was found, otherwise false
     * @throws InterruptedException if the thread is interrupted
     * @see waitForString(String, int, SessionOutputEcho)
     */
    public synchronized boolean waitForString(String str)
            throws InterruptedException {
        return waitForString(str, 0, null);
    }

    /**
     * <p/>
     * Wait for a given String in the output buffer.
     * </p>
     *
     * @param str     the string to wait for
     * @param timeout the number of milliseconds to wait
     * @return true if the string was found, otherwise false
     * @throws InterruptedException if the thread is interrupted
     * @see waitForString(String, int, SessionOutputEcho)
     */
    public synchronized boolean waitForString(String str, int timeout)
            throws InterruptedException {
        return waitForString(str, timeout, null);
    }

    /**
     * <p/>
     * Wait for a given String in the output buffer. When this method is called
     * the method will block unitil either the String arrives in the input
     * buffer or the timeout specified has elasped.
     * </p>
     *
     * @param str     the string to wait for
     * @param timeout the number of milliseconds to wait, 0=infinite
     * @param echo    a callback interface to receive the session output whilst
     *                the no match for the string is found
     * @return true if the string was found, otherwise false
     * @throws InterruptedException if the thread is interrupted
     */
    public synchronized boolean waitForString(String str, int timeout,
                                              SessionOutputEcho echo) throws InterruptedException {
        long time = System.currentTimeMillis();

        while ((output.indexOf(str, pos) == -1) &&
                (((System.currentTimeMillis() - time) < timeout) ||
                (timeout == 0))) {
            int tmp = output.length();
            wait((timeout > 0) ? (timeout -
                    (System.currentTimeMillis() - time)) : 0);

            if ((output.length() > tmp) && (echo != null)) {
                echo.echo(output.substring(tmp, output.length()));
            }
        }

        if (output.indexOf(str, pos) > -1) {
            pos = output.indexOf(str, pos) + str.length();

            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @param echo
     * @throws InterruptedException
     */
    public synchronized void echoLineByLineToClose(SessionOutputEcho echo)
            throws InterruptedException {
        while (session.isOpen()) {
            waitForString("\n", 1000, echo);
        }
    }

    private synchronized void breakWaiting() {
        notifyAll();
    }

    /**
     * The ChannelEventListener to receive event notifications
     */
    class SessionOutputListener implements ChannelEventListener {
        public void onChannelOpen(Channel channel) {
        }

        public void onChannelClose(Channel channel) {
            breakWaiting();
        }

        public void onChannelEOF(Channel channel) {
            // Timeout
            breakWaiting();
        }

        public void onDataSent(Channel channel, byte[] data) {
            // Do nothing
        }

        public void onDataReceived(Channel channel, byte[] data) {
            output += new String(data);
            breakWaiting();
        }
    }
}
