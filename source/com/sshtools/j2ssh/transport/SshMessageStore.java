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
package com.sshtools.j2ssh.transport;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.io.ByteArrayReader;


/**
 * <p/>
 * This class implements a message store that can be used to provide a blocking
 * mechanism for transport protocol messages.
 * </p>
 *
 * @author Lee David Painter
 * @version $Revision$
 * @since 0.2.0
 */
public final class SshMessageStore {
    private static Log log = LogFactory.getLog(SshMessageStore.class);

    // List to hold messages as they are received
    private List messages = new ArrayList();
    private Map register = new HashMap();
    private boolean isClosed = false;
    private int[] singleIdFilter = new int[1];
    private int interrupt = 5000;
    private Vector listeners = new Vector();

    /**
     * <p/>
     * Contructs the message store.
     * </p>
     *
     * @since 0.2.0
     */
    public SshMessageStore() {
    }

    /**
     * <p/>
     * Evaluate whether the message store is closed.
     * </p>
     *
     * @return
     * @since 0.2.0
     */
    public boolean isClosed() {
        return isClosed;
    }

    public void addMessageListener(SshMessageListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * <p/>
     * Get a message from the store. This method will block until a message
     * with an id matching the supplied filter arrives, or the message store
     * closes. The message is removed from the store.
     * </p>
     *
     * @param messageIdFilter an array of message ids that are acceptable
     * @return the next available message
     * @throws MessageStoreEOFException if the message store is closed
     * @throws InterruptedException     if the thread was interrupted
     * @since 0.2.0
     */
    public synchronized SshMessage getMessage(int[] messageIdFilter)
            throws MessageStoreEOFException, InterruptedException {
        try {
            return getMessage(messageIdFilter, 0);
        }
        catch (MessageNotAvailableException e) {
            // This should never happen but throw just in case
            throw new MessageStoreEOFException();
        }
    }

    /**
     * <p/>
     * Get a message from the store. This method will block until a message
     * with an id matching the supplied filter arrives, the specified timeout
     * is reached or the message store closes. The message is removed from the
     * store.
     * </p>
     *
     * @param messageIdFilter an array of message ids that are acceptable.
     * @param timeout         the maximum number of milliseconds to block before
     *                        returning.
     * @return the next available message
     * @throws MessageStoreEOFException     if the message store is closed
     * @throws MessageNotAvailableException if the message is not available
     *                                      after a timeout
     * @throws InterruptedException         if the thread is interrupted
     * @since 0.2.0
     */
    public synchronized SshMessage getMessage(int[] messageIdFilter, int timeout)
            throws MessageStoreEOFException, MessageNotAvailableException,
            InterruptedException {
        if ((messages.size() <= 0) && isClosed) {
            throw new MessageStoreEOFException();
        }

        if (messageIdFilter == null) {
            return nextMessage();
        }

        SshMessage msg;
        boolean firstPass = true;

        if (timeout < 0) {
            timeout = 0;
        }

        while ((messages.size() > 0) || !isClosed) {
            // lookup the message
            msg = lookupMessage(messageIdFilter, true);

            if (msg != null) {
                return msg;
            }
            else {
                // If this is the second time and there's no message, then throw
                if (!firstPass && (timeout > 0)) {
                    throw new MessageNotAvailableException();
                }
            }

            // Now wait
            if (!isClosed) {
                wait((timeout == 0) ? interrupt : timeout);
            }

            firstPass = false;
        }

        throw new MessageStoreEOFException();
    }

    /**
     * <p/>
     * Get a message from the store. This method will block until a message
     * with an id matching the supplied id arrives, or the message store
     * closes. The message is removed from the store.
     * </p>
     *
     * @param messageId the id of the message requried
     * @return the next available message with the id supplied
     * @throws MessageStoreEOFException if the message store closed
     * @throws InterruptedException     if the thread is interrupted
     * @since 0.2.0
     */
    public synchronized SshMessage getMessage(int messageId)
            throws MessageStoreEOFException, InterruptedException {
        try {
            return getMessage(messageId, 0);
        }
        catch (MessageNotAvailableException e) {
            // This should never happen by throw jsut in case
            throw new MessageStoreEOFException();
        }
    }

    /**
     * <p/>
     * Get a message from the store. This method will block until a message
     * with an id matching the supplied id arrives,the specified timeout is
     * reached or the message store closes. The message will be removed from
     * the store.
     * </p>
     *
     * @param messageId the id of the message requried
     * @param timeout   the maximum number of milliseconds to block before
     *                  returning.
     * @return the next available message with the id supplied
     * @throws MessageStoreEOFException if the message store closed
     * @throws InterruptedException     if the thread is interrupted
     * @throws InterruptedException
     * @since 0.2.0
     */
    public synchronized SshMessage getMessage(int messageId, int timeout)
            throws MessageStoreEOFException, MessageNotAvailableException,
            InterruptedException {
        singleIdFilter[0] = messageId;

        return getMessage(singleIdFilter, timeout);
    }

    /**
     * <p/>
     * Evaluate whether the store has any messages.
     * </p>
     *
     * @return true if messages exist, otherwise false
     * @since 0.2.0
     */
    public boolean hasMessages() {
        return messages.size() > 0;
    }

    /**
     * <p/>
     * Returns the number of messages contained within this message store.
     * </p>
     *
     * @return the number of messages
     * @since 0.2.0
     */
    public int size() {
        return messages.size();
    }

    /**
     * <p/>
     * Determines if the message id is a registered message of this store.
     * </p>
     *
     * @param messageId the message id
     * @return true if the message id is registered, otherwise false
     * @since 0.2.0
     */
    public boolean isRegisteredMessage(Integer messageId) {
        return register.containsKey(messageId);
    }

    /**
     * <p/>
     * Adds a raw message to the store and processes the data into a registered
     * message.
     * </p>
     *
     * @param msgdata the raw message data to process
     * @throws MessageNotRegisteredException if the message id of the raw data
     *                                       is not a registered message
     * @throws InvalidMessageException       if the message is invalid
     * @since 0.2.0
     */
    public void addMessage(byte[] msgdata)
            throws MessageNotRegisteredException, InvalidMessageException {
        Integer messageId = new Integer(msgdata[5]);

        if (!isRegisteredMessage(messageId)) {
            throw new MessageNotRegisteredException(messageId);
        }

        Class cls = (Class) register.get(SshMessage.getMessageId(msgdata));

        try {
            SshMessage msg = (SshMessage) cls.newInstance();
            msg.fromByteArray(new ByteArrayReader(msgdata));
            addMessage(msg);
        }
        catch (IllegalAccessException iae) {
            throw new InvalidMessageException("Illegal access for implementation class " + cls.getName());
        }
        catch (InstantiationException ie) {
            throw new InvalidMessageException("Instantiation failed for class " +
                    cls.getName());
        }
    }

    /**
     * <p/>
     * Add a formed message to the store.
     * </p>
     *
     * @param msg the message to add to the store
     * @throws MessageNotRegisteredException if the message type is not
     *                                       registered with the store
     * @since 0.2.0
     */
    public synchronized void addMessage(SshMessage msg)
            throws MessageNotRegisteredException {
        // Add the message
        messages.add(messages.size(), msg);

        synchronized (listeners) {
            if (listeners.size() > 0) {
                for (Iterator it = listeners.iterator(); it.hasNext();) {
                    ((SshMessageListener) it.next()).messageReceived(msg);
                }
            }
        }

        // Notify the threads
        notifyAll();
    }

    /**
     * <p/>
     * Closes the store. This will cause any blocking operations on the message
     * store to return.
     * </p>
     *
     * @since 0.2.0
     */
    public synchronized void close() {
        isClosed = true;

        // We need to notify all anyway as if there are messages still available
        // it should not affect the waiting threads as they are waiting for their
        // own messages to be received because non were avaialable in the first place
        //if (messages.size()<=0) {
        notifyAll();

        //}
    }

    /**
     * <p/>
     * Get the next message in the store or wait until a new message arrives.
     * The message is removed from the store.
     * </p>
     *
     * @return the next available message.
     * @throws MessageStoreEOFException if the message store is closed
     * @throws InterruptedException     if the thread is interrupted
     * @since 0.2.0
     */
    public synchronized SshMessage nextMessage()
            throws MessageStoreEOFException, InterruptedException {
        if ((messages.size() <= 0) && isClosed) {
            throw new MessageStoreEOFException();
        }

        // If there are no messages available then wait untill there are.
        while ((messages.size() <= 0) && !isClosed) {
            wait(interrupt);
        }

        if (messages.size() > 0) {
            return (SshMessage) messages.remove(0);
        }
        else {
            throw new MessageStoreEOFException();
        }
    }

    /**
     *
     */
    public synchronized void breakWaiting() {
        notifyAll();
    }

    /**
     * <p/>
     * Get a message from the store without removing or blocking if the message
     * does not exist.
     * </p>
     *
     * @param messageIdFilter the id of the message requried
     * @return the next available message with the id supplied
     * @throws MessageStoreEOFException     if the message store closed
     * @throws MessageNotAvailableException if the message is not available
     * @throws InterruptedException         if the thread is interrupted
     * @since 0.2.0
     */
    public synchronized SshMessage peekMessage(int[] messageIdFilter)
            throws MessageStoreEOFException, MessageNotAvailableException,
            InterruptedException {
        return peekMessage(messageIdFilter, 0);
    }

    /**
     * <p/>
     * Get a message from the store without removing it; only blocking for the
     * number of milliseconds specified in the timeout field. If timeout is
     * zero, the method will not block.
     * </p>
     *
     * @param messageIdFilter an array of acceptable message ids
     * @param timeout         the number of milliseconds to wait
     * @return the next available message of the acceptable message ids
     * @throws MessageStoreEOFException     if the message store is closed
     * @throws MessageNotAvailableException if the message is not available
     * @throws InterruptedException         if the thread is interrupted
     * @since 0.2.0
     */
    public synchronized SshMessage peekMessage(int[] messageIdFilter,
                                               int timeout)
            throws MessageStoreEOFException, MessageNotAvailableException,
            InterruptedException {
        SshMessage msg;

        // Do a straight lookup
        msg = lookupMessage(messageIdFilter, false);

        if (msg != null) {
            return msg;
        }

        // If were willing to wait the wait and look again
        if (timeout > 0) {
            if (log.isDebugEnabled()) {
                log.debug("No message so waiting for " +
                        String.valueOf(timeout) + " milliseconds");
            }

            wait(timeout);
            msg = lookupMessage(messageIdFilter, false);

            if (msg != null) {
                return msg;
            }
        }

        // Nothing even after a wait so throw the relevant exception
        if (isClosed) {
            throw new MessageStoreEOFException();
        }
        else {
            throw new MessageNotAvailableException();
        }
    }

    private SshMessage lookupMessage(int[] messageIdFilter, boolean remove) {
        SshMessage msg;

        for (int x = 0; x < messages.size(); x++) {
            msg = (SshMessage) messages.get(x);

            // Determine whether its one of the filtered messages
            for (int i = 0; i < messageIdFilter.length; i++) {
                if (msg.getMessageId() == messageIdFilter[i]) {
                    if (remove) {
                        messages.remove(msg);
                    }

                    return msg;
                }
            }
        }

        return null;
    }

    /**
     * <p/>
     * Get a message from the store without removing it.
     * </p>
     *
     * @param messageId the acceptable message id
     * @return the next available message.
     * @throws MessageStoreEOFException     if the message store is closed.
     * @throws MessageNotAvailableException if the message is not available.
     * @throws InterruptedException         if the thread is interrupted
     * @since 0.2.0
     */
    public synchronized SshMessage peekMessage(int messageId)
            throws MessageStoreEOFException, MessageNotAvailableException,
            InterruptedException {
        return peekMessage(messageId, 0);
    }

    /**
     * <p/>
     * Removes a message from the message store.
     * </p>
     *
     * @param msg the message to remove
     * @since 0.2.0
     */
    public synchronized void removeMessage(SshMessage msg) {
        messages.remove(msg);
    }

    /**
     * <p/>
     * Get a message from the store without removing it, only blocking for the
     * number of milliseconds specified in the timeout field.
     * </p>
     *
     * @param messageId the acceptable message id
     * @param timeout   the timeout setting in milliseconds
     * @return the next available message
     * @throws MessageStoreEOFException     if the message store is closed
     * @throws MessageNotAvailableException if the message is not available
     * @throws InterruptedException         if the thread is interrupted
     * @since 0.2.0
     */
    public synchronized SshMessage peekMessage(int messageId, int timeout)
            throws MessageStoreEOFException, MessageNotAvailableException,
            InterruptedException {
        singleIdFilter[0] = messageId;

        return peekMessage(singleIdFilter, timeout);
    }

    /**
     * <p/>
     * Register a message implementation with the store.
     * </p>
     *
     * @param messageId   the id of the message
     * @param implementor the class of the implementation
     * @since 0.2.0
     */
    public void registerMessage(int messageId, Class implementor) {
        Integer id = new Integer(messageId);
        register.put(id, implementor);
    }

    /**
     * <p/>
     * Returns an Object array (Integers) of the registered message ids.
     * </p>
     *
     * @return the registered message id array
     * @since 0.2.0
     */
    public Object[] getRegisteredMessageIds() {
        return register.keySet().toArray();
    }

    /**
     * <p/>
     * Create a formed message from raw message data.
     * </p>
     *
     * @param msgdata the raw message data
     * @return the formed message
     * @throws MessageNotRegisteredException if the message is not a registered
     *                                       message
     * @throws InvalidMessageException       if the message is invalid
     * @since 0.2.0
     */
    public SshMessage createMessage(byte[] msgdata)
            throws MessageNotRegisteredException, InvalidMessageException {
        Integer messageId = SshMessage.getMessageId(msgdata);

        if (!isRegisteredMessage(messageId)) {
            throw new MessageNotRegisteredException(messageId);
        }

        Class cls = (Class) register.get(SshMessage.getMessageId(msgdata));

        try {
            SshMessage msg = (SshMessage) cls.newInstance();
            msg.fromByteArray(new ByteArrayReader(msgdata));

            return msg;
        }
        catch (IllegalAccessException iae) {
            throw new InvalidMessageException("Illegal access for implementation class " + cls.getName());
        }
        catch (InstantiationException ie) {
            throw new InvalidMessageException("Instantiation failed for class " +
                    cls.getName());
        }
    }
}
