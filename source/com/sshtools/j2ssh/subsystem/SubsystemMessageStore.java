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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sshtools.j2ssh.transport.InvalidMessageException;

import com.sshtools.j2ssh.util.OpenClosedState;

import com.sshtools.j2ssh.io.ByteArrayReader;

import org.apache.log4j.Logger;

/**
 * Implements a message store for buffering subsystem messages.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SubsystemMessageStore {
    // List to hold messages as they are received
    protected List messages = new ArrayList();

    // Map to hold message implementation classes
    protected Map registeredMessages = new HashMap();

    private OpenClosedState state = new OpenClosedState(OpenClosedState.OPEN);

    private static Logger log = Logger.getLogger(SubsystemMessageStore.class);

    /**
     * Constructs the message store object.
     */
    public SubsystemMessageStore() {
    }

    /**
     * Adds a message to the message store and notify the waiting threads.
     *
     * @param msg The message to add.
     */
    public synchronized void addMessage(SubsystemMessage msg) {

        log.info("Received " + msg.getMessageName() + " subsystem message");

        // Add the message
        messages.add(msg);

        // Notify the threads
        notifyAll();
    }

    /**
     * Adds raw message data to the message store. The data is converted into a
     * Subsystem message by looking up the implementation class for the
     * message id number.
     *
     * @param msgdata the message data to add
     *
     * @throws InvalidMessageException if the message is invalid
     */
    public synchronized void addMessage(byte msgdata[])
                                 throws InvalidMessageException {
        try {

            Class impl =
                (Class) registeredMessages.get(new Integer(msgdata[0]));

            SubsystemMessage msg = (SubsystemMessage) impl.newInstance();

            msg.fromByteArray(msgdata);

            addMessage(msg);

            return;
        } catch (IllegalAccessException iae) {
        } catch (InstantiationException ie) {
        }

        throw new InvalidMessageException("Could not instantiate message class");
    }

    /**
     * Get the next message available from the message store. If there are no
     * messages available, the method will wait until notified of a new
     * message.
     *
     * @return The next available message.
     */
    public synchronized SubsystemMessage nextMessage() {
        // If there are no messages available then wait untill there are.
        while (messages.size()<=0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        if(state.getValue()==OpenClosedState.OPEN)
          return (SubsystemMessage) messages.remove(0);
        else
          return null;
    }

    /**
     * Registers a message type
     *
     * @param messageId the message id number
     * @param implementor the implementation class
     */
    public void registerMessage(int messageId, Class implementor) {
        registeredMessages.put(new Integer(messageId), implementor);
    }

    public OpenClosedState getState() {
      return state;
    }

    public void close() {
      state.setValue(OpenClosedState.CLOSED);
      notifyAll();
    }
}
