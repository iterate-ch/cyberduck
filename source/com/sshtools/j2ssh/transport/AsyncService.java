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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.SshThread;


/**
 * <p/>
 * Extends the simple <code>Service</code> class to provide an asyncronous
 * messaging service for the transport protocol.
 * </p>
 *
 * @author Lee David Painter
 * @version $Revision$
 * @since 0.2.0
 */
public abstract class AsyncService extends Service implements Runnable {
    private static Log log = LogFactory.getLog(Service.class);

    /**  */
    protected SshThread thread;

    /**
     * <p/>
     * Constructs an asyncronous service.
     * </p>
     *
     * @param serviceName the name of the service
     * @since 0.2.0
     */
    public AsyncService(String serviceName) {
        super(serviceName);
    }

    /**
     * <p/>
     * Implements the abstract <code>Service</code> method and starts the
     * service thread.
     * </p>
     *
     * @throws IOException if an IO error occurs
     * @since 0.2.0
     */
    protected void onStart() throws IOException {
        if (Thread.currentThread() instanceof SshThread) {
            thread = ((SshThread) Thread.currentThread()).cloneThread(this,
                    getServiceName());
        }
        else {
            thread = new SshThread(this, getServiceName(), true);
        }

        log.info("Starting " + getServiceName() + " service thread");
        thread.start();
    }

    /**
     * <p/>
     * Implements the asyncronous services message loop.
     * </p>
     *
     * @since 0.2.0
     */
    public final void run() {
        int[] messageFilter = getAsyncMessageFilter();
        state.setValue(ServiceState.SERVICE_STARTED);

        SshMessage msg = null;

        while ((state.getValue() == ServiceState.SERVICE_STARTED) &&
                transport.isConnected()) {
            try {
                // Get the next message from the message store
                msg = messageStore.getMessage(messageFilter);

                if (state.getValue() == ServiceState.SERVICE_STOPPED) {
                    break;
                }

                if (log.isDebugEnabled()) {
                    log.debug("Routing " + msg.getMessageName());
                }

                onMessageReceived(msg);

                if (log.isDebugEnabled()) {
                    log.debug("Finished processing " + msg.getMessageName());
                }
            }
            catch (MessageStoreEOFException eof) {
                stop();
            }
            catch (Exception ex) {
                if ((state.getValue() != ServiceState.SERVICE_STOPPED) &&
                        transport.isConnected()) {
                    log.fatal("Service message loop failed!", ex);
                    stop();
                }
            }
        }

        onStop();
        log.info(getServiceName() + " thread is exiting");
        thread = null;
    }

    /**
     * <p/>
     * The service thread calls this method when the thread is exiting.
     * </p>
     *
     * @since 0.2.0
     */
    protected abstract void onStop();

    /**
     * <p/>
     * Implement this method by returning the message ids of the asyncrounous
     * messages your implementation wants to receive.
     * </p>
     *
     * @return an int array of message ids
     * @since 0.2.0
     */
    protected abstract int[] getAsyncMessageFilter();

    /**
     * <p/>
     * Called by the service thread when an asyncronous message is received.
     * </p>
     *
     * @param msg the message received
     * @throws IOException if an IO error occurs
     * @since 0.2.0
     */
    protected abstract void onMessageReceived(SshMessage msg)
            throws IOException;
}
