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
package com.sshtools.j2ssh.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sshtools.j2ssh.io.ByteArrayReader;


/**
 * <p>
 * The message store allows the transport protocol to provide asynchronous
 * messaging. When an object (such as Service) wishes to receive messages, it
 * creates an instance of the message store and calls the TransportProtocol
 * interface method registerMessage passing the message Id of the desired
 * message and the message store object that recieves notifications of a
 * messages arrival.
 * </p>
 *
 * <p>
 * When a message is received the transport protocol looks up the message store
 * object for the incoming message Id and adds the message to it. Any waiting
 * threads are notified so that the message can be handled appropriatley.
 * </p>
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public final class SshMessageStore {
    // List to hold messages as they are received
    private List messages = new ArrayList();
    private Map register = new HashMap();
    private boolean isClosed = false;

    /**
     * Constructs the message store object.
     */
    public SshMessageStore() {
    }

    /**
     * Determines if the message store has been closed
     *
     * @return <tt>true</tt> if the store is closed otherwies <tt>false</tt>
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Gets a message from the store. This method will block untill a suitable
     * message has been received.
     *
     * @param messageIdFilter an array of message id numbers used so that only
     *        specific message ids are returned. if this value is null the
     *        next available message is returned
     *
     * @return the next available message whose message id is contained in the
     *         filter
     *
     * @throws MessageStoreEOFException if the message store is EOF
     */
    public synchronized SshMessage getMessage(int messageIdFilter[])
                                       throws MessageStoreEOFException {
        if ((messages.size()<=0) && isClosed) {
            throw new MessageStoreEOFException();
        }

        if (messageIdFilter==null) {
            return nextMessage();
        }

        SshMessage msg;

        while ((messages.size()>0) || !isClosed) {
            for (int x = 0;x<messages.size();x++) {
                msg = (SshMessage) messages.get(x);

                // Determine whether its one of the filtered messages
                for (int i = 0;i<messageIdFilter.length;i++) {
                    if (msg.getMessageId()==messageIdFilter[i]) {
                        messages.remove(msg);

                        return msg;
                    }
                }
            }

            try {
                if (!isClosed) {
                    wait();
                }
            } catch (InterruptedException ie) {
            }
        }

        throw new MessageStoreEOFException();
    }

    /**
     * Gets a single message from the store. If no messages of the requried id
     * are available then the method blocks until a message is recieved
     *
     * @param messageId the id of the message to collect
     *
     * @return the message instance
     *
     * @throws MessageStoreEOFException if the message store is EOF
     */
    public synchronized SshMessage getMessage(int messageId)
                                       throws MessageStoreEOFException {
        if ((messages.size()<=0) && isClosed) {
            throw new MessageStoreEOFException();
        }

        SshMessage msg;

        while ((messages.size()>0) || !isClosed) {
            for (int x = 0;x<messages.size();x++) {
                msg = (SshMessage) messages.get(x);

                if (msg.getMessageId()==messageId) {
                    return msg;
                }
            }

            try {
                if (!isClosed) {
                    wait();
                }
            } catch (InterruptedException ie) {
            }
        }

        throw new MessageStoreEOFException();
    }

    /**
     * Determine if the message is registered with this message store
     *
     * @param messageId the id of the message
     *
     * @return <tt>true</tt> if the message is registered, otherwise <tt>false</tt>
     */
    public boolean isRegisteredMessage(Integer messageId) {
        return register.containsKey(messageId);
    }

    /**
     * Adds a message to the message store
     *
     * @param msgdata the raw message data
     *
     * @throws MessageNotRegisteredException if the message type is not registered with this store
     * @throws InvalidMessageException if the message instance cannot be created
     */
    public void addMessage(byte msgdata[])
                    throws MessageNotRegisteredException,
                           InvalidMessageException {
        Integer messageId = new Integer(msgdata[5]);

        if (!isRegisteredMessage(messageId)) {
            throw new MessageNotRegisteredException(messageId);
        }

        Class cls = (Class) register.get(SshMessage.getMessageId(msgdata));

        try {
            SshMessage msg = (SshMessage) cls.newInstance();
            msg.fromByteArray(new ByteArrayReader(msgdata));
            addMessage(msg);
        } catch (IllegalAccessException iae) {
            throw new InvalidMessageException("Illegal access for implementation class "
                                              + cls.getName());
        } catch (InstantiationException ie) {
            throw new InvalidMessageException("Instantiation failed for class "
                                              + cls.getName());
        }
    }

    /**
     * Adds a message to the message store and notify the waiting threads.
     *
     * @param msg The message to add.
     *
     * @throws MessageNotRegisteredException if the message is not registered with this store
     */
    public synchronized void addMessage(SshMessage msg)
                                 throws MessageNotRegisteredException {
        // Add the message
        messages.add(msg);

        // Notify the threads
        notifyAll();
    }

    /**
     * Closes the message store.
     */
    public synchronized void close() {
        isClosed = true;

        // We need to notify all anyway as if there are messages still available
        // it should not affect the waiting threads as they are waiting for their
        // own messages to be received becuase non were avaialable in the first place
        //if (messages.size()<=0) {
        notifyAll();

        //}
    }

    /**
     * Get the next message available from the message store. If there are no
     * messages available, the method will wait until notified of a new
     * message.
     *
     * @return The next available message.
     *
     * @throws MessageStoreEOFException if the message store is EOF
     */
    public synchronized SshMessage nextMessage()
                                        throws MessageStoreEOFException {
        if ((messages.size()<=0) && isClosed) {
            throw new MessageStoreEOFException();
        }

        // If there are no messages available then wait untill there are.
        while ((messages.size()<=0)) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        if (messages.size()>0) {
            return (SshMessage) messages.remove(0);
        } else {
            throw new MessageStoreEOFException();
        }
    }

    /**
     * Collect a message without removing it from the message store
     *
     * @param messageIdFilter an array of message id's that are acceptable
     *        return messages.
     *
     * @return The first message found that matches any of the id values.
     *
     * @throws MessageStoreEOFException if the message store is EOF
     * @throws MessageNotAvailableException if there are no messages available
     */
    public synchronized SshMessage peekMessage(int messageIdFilter[])
                                        throws MessageStoreEOFException,
                                               MessageNotAvailableException {
        SshMessage msg;

        if (messages.size()>0) {
            for (int x = 0;x<messages.size();x++) {
                msg = (SshMessage) messages.get(x);

                // Determine whether its one of the filtered messages
                for (int i = 0;i<messageIdFilter.length;i++) {
                    if (msg.getMessageId()==messageIdFilter[i]) {
                        return msg;
                    }
                }
            }
        }

        if (isClosed) {
            throw new MessageStoreEOFException();
        } else {
            throw new MessageNotAvailableException();
        }
    }

    /**
     * Collect a single message without removing it from the message store
     *
     * @param messageId The id of the message required
     *
     * @return the message instance
     *
     * @throws MessageStoreEOFException if the message store is EOF
     * @throws MessageNotAvailableException if there are no messages available
     */
    public synchronized SshMessage peekMessage(int messageId)
                                        throws MessageStoreEOFException,
                                               MessageNotAvailableException {
        SshMessage msg;

        if (messages.size()>0) {
            for (int x = 0;x<messages.size();x++) {
                msg = (SshMessage) messages.get(x);

                if (msg.getMessageId()==messageId) {
                    return msg;
                }
            }
        }

        if (isClosed) {
            throw new MessageStoreEOFException();
        } else {
            throw new MessageNotAvailableException();
        }
    }

    /**
     * Register an <code>SshMessage</code> implementation with this message
     * store
     *
     * @param messageId the id of the message
     * @param implementor the implementation class
     *
     * @throws MessageAlreadyRegisteredException if the message id is already registered
     */
    public void registerMessage(int messageId, Class implementor) {
        Integer id = new Integer(messageId);
        register.put(id, implementor);
    }
}
