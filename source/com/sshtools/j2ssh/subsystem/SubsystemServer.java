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
package com.sshtools.j2ssh.subsystem;

import java.io.InputStream;
import java.io.OutputStream;

import com.sshtools.j2ssh.util.StartStopState;

import com.sshtools.j2ssh.session.SessionDataProvider;


/**
 * Abstract class implementing a Subsystem server
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class SubsystemServer
    implements SessionDataProvider, Runnable {
    private SubsystemMessageStore incoming = new SubsystemMessageStore();
    private SubsystemMessageStore outgoing = new SubsystemMessageStore();
    private SubsystemInputStream in = new SubsystemInputStream(outgoing);
    private SubsystemOutputStream out = new SubsystemOutputStream(incoming);
    private Thread thread;
    private StartStopState state = new StartStopState(StartStopState.STOPPED);

    /**
     * Constructs the object
     */
    public SubsystemServer() {
    }

    /**
     * Returns the subsystem inputstream
     *
     * @return the subsystems InputStream
     */
    public InputStream getInputStream() {
        return in;
    }

    /**
     * Returns the subsystem outputstream
     *
     * @return the subsystems OutputStream
     */
    public OutputStream getOutputStream() {
        return out;
    }

    /**
     * The threads main method
     */
    public void run() {

        state.setValue(StartStopState.STARTED);

        while (state.getValue()==StartStopState.STARTED) {
            SubsystemMessage msg = incoming.nextMessage();
            if(msg!=null)
              onMessageReceived(msg);
        }

        thread = null;
    }

    /**
     * Starts the subsystem
     */
    public void start() {

        thread.start();
    }

    /**
     * Stops the subsystem
     */
    public void stop() {
        state.setValue(StartStopState.STOPPED);
        incoming.close();
        outgoing.close();
    }

    /**
     * Called when a registered message is received
     *
     * @param msg the message received
     */
    protected abstract void onMessageReceived(SubsystemMessage msg);

    /**
     * Register a message on the incoming message store
     *
     * @param messageId the message id to process
     * @param implementor the implementation class
     */
    protected void registerMessage(int messageId, Class implementor) {
        incoming.registerMessage(messageId, implementor);
    }

    /**
     * Sends a message to the remote computer
     *
     * @param msg the message to send
     */
    protected void sendMessage(SubsystemMessage msg) {
        outgoing.addMessage(msg);
    }
}
