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


/**
 * <p/>
 * This class implements the transport protocol service.
 * </p>
 * <p/>
 * <p/>
 * After the transport protocol negotiates the protocol version and performs
 * server authentication via key exchange, the client requests a service. The
 * service is identified by a name and currently there are 2 services defined.<br>
 * <br>
 * ssh-userauth<br>
 * ssh-connection<br>
 * <br>
 * These 2 services are implemented by the SSH authentication protocol and SSH
 * connection protocol respectivley. Further services can be defined and a
 * similar local naming policy is applied to the service names, as is applied
 * to the algorithm names; a local service should use the
 * "servicename(at)domain" syntax.
 * </p>
 *
 * @author Lee David Painter
 * @version $Revision$
 * @since 0.2.0
 */
public abstract class Service {
    private static Log log = LogFactory.getLog(Service.class);

    /**
     * Service start mode passed into <code>init</code> method when the service
     * is operating in client mode. i.e its requesting a service to be started
     * on the remote server and requires a SSH_MSG_SERVICE_ACCEPT message.
     */
    public final static int REQUESTING_SERVICE = 1;

    /**
     * Serivce start mode passed into <code>init</code> method when the service
     * is operating in server mode. i.e a client is requesting a service to be
     * started on the local computer and requires the SSH_MSG_SERVICE_ACCEPT
     * message to be sent.
     */
    public final static int ACCEPTING_SERVICE = 2;

    /**
     * The message store registered with the transport protocol to receive the
     * service's message.
     */
    protected SshMessageStore messageStore = new SshMessageStore();

    /**
     * The underlying transport protocol
     */
    protected TransportProtocol transport;

    /**
     * This instances start mode
     */
    protected Integer startMode = null;

    /**
     * The current state of the service
     */
    protected ServiceState state = new ServiceState();

    /**
     * The name of the service
     */
    private String serviceName;

    /**
     * <p/>
     * Constructs the service.
     * </p>
     *
     * @param serviceName the name of the service
     * @since 0.2.0
     */
    public Service(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * <p/>
     * Returns the service name.
     * </p>
     *
     * @return the serivce name
     * @since 0.2.0
     */
    public final String getServiceName() {
        return serviceName;
    }

    /**
     * <p/>
     * Starts the service.
     * </p>
     *
     * @throws IOException if an IO error occurs
     * @since 0.2.0
     */
    public final void start() throws IOException {
        if (startMode == null) {
            throw new IOException("Service must be initialized first!");
        }

        // If were accepted (i.e. client) we will call onServiceAccept()
        if (startMode.intValue() == REQUESTING_SERVICE) {
            log.info(serviceName + " has been accepted");
            onServiceAccept();
        }
        else {
            // We've recevied a request instead
            log.info(serviceName + " has been requested");
            onServiceRequest();
        }

        onStart();
        state.setValue(ServiceState.SERVICE_STARTED);
    }

    /**
     * <p/>
     * Called when the service is started.
     * </p>
     *
     * @throws IOException if an IO error occurs
     * @since 0.2.0
     */
    protected abstract void onStart() throws IOException;

    /**
     * <p/>
     * Returns the state of the service.
     * </p>
     *
     * @return the state of the service
     * @see ServiceState
     * @since 0.2.0
     */
    public ServiceState getState() {
        return state;
    }

    /**
     * <p/>
     * Initialize the service.
     * </p>
     *
     * @param startMode the mode of the service
     * @param transport the underlying transport protocol
     * @throws IOException if an IO error occurs
     * @since 0.2.0
     */
    public void init(int startMode, TransportProtocol transport)
            throws IOException {
        if ((startMode != REQUESTING_SERVICE) &&
                (startMode != ACCEPTING_SERVICE)) {
            throw new IOException("Invalid start mode!");
        }

        this.transport = transport;
        this.startMode = new Integer(startMode);

        //this.nativeSettings = nativeSettings;
        onServiceInit(startMode);
        transport.addMessageStore(messageStore);
    }

    /**
     * <p/>
     * Stops the service.
     * </p>
     *
     * @since 0.2.0
     */
    public final void stop() {
        messageStore.close();
        state.setValue(ServiceState.SERVICE_STOPPED);
    }

    /**
     * <p/>
     * Called when the service is accepted by the remote server.
     * </p>
     *
     * @throws IOException
     * @since 0.2.0
     */
    protected abstract void onServiceAccept() throws IOException;

    /**
     * <p/>
     * Called when the service is intialized.
     * </p>
     *
     * @param startMode the mode of the service
     * @throws IOException if an IO error occurs
     * @since 0.2.0
     */
    protected abstract void onServiceInit(int startMode)
            throws IOException;

    /**
     * @throws IOException
     */
    protected abstract void onServiceRequest() throws IOException;

    /**
     * <p/>
     * Sends the SSH_MSG_SERVICE_ACCEPT message to the client to indicate that
     * the local computer is accepting the remote computers service request.
     * </p>
     *
     * @throws IOException if an IO error occurs
     * @since 0.2.0
     */
    protected void sendServiceAccept() throws IOException {
        SshMsgServiceAccept msg = new SshMsgServiceAccept(serviceName);
        transport.sendMessage(msg, this);
    }
}
