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
package com.sshtools.j2ssh.forwarding;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.connection.ConnectionProtocol;

import com.sshtools.j2ssh.util.StartStopState;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 * <p>
 * This abstract class listens for connections and forwards the connection
 * through the secure tunnel to the remote computer which in turn delivers it
 * to its intended recipient.
 * </p>
 * Overside the <code>createChannel</code> method to create the necersary type
 * of channel for your implementation.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class ForwardingListener
    extends ForwardingConfiguration
    implements Runnable {
    private static Logger log = Logger.getLogger(ForwardingListener.class);
    private ConnectionProtocol connection;
    private ServerSocket server;
    private Thread thread;

    /**
     * Creates a new ForwardingListener object.
     *
     * @param name The name of this forwarding listener
     * @param connection The connection to tunnel incoming connections through
     * @param addressToBind The address to listen on
     * @param portToBind The port to listen on
     * @param hostToConnect The host to tunnel the connection to
     * @param portToConnect The port to tunnel the connection to
     */
    public ForwardingListener(String name, ConnectionProtocol connection,
                              String addressToBind, int portToBind,
                              String hostToConnect, int portToConnect) {
        super(name, addressToBind, portToBind, hostToConnect, portToConnect);

        log.info("Creating forwarding listener named '" + name + "'");

        this.connection = connection;

        if (log.isDebugEnabled()) {
            log.debug("Address to bind: " + getAddressToBind());
            log.debug("Port to bind: " + String.valueOf(getPortToBind()));
            log.debug("Host to connect: " + hostToConnect);
            log.debug("Port to connect: " + portToConnect);
        }
    }

    /**
     * Creates a new ForwardingListener object.
     *
     * @param connection The connection to tunnel incoming connections through
     * @param addressToBind The address to listen to
     * @param portToBind The port to listen to
     */
    public ForwardingListener(ConnectionProtocol connection,
                              String addressToBind, int portToBind) {
        this(addressToBind + ":" + String.valueOf(portToBind), connection,
             addressToBind, portToBind, "[Specified by connecting computer]", -1);
    }

    /**
     * Creates a server socket and listens for connections. When a connection
     * is made a channel is opened with the remote side to tunnel the
     * connection through.
     */
    public void run() {
        try {
            log.info("Starting forwarding listener thread for '" + name + "'");

            server =
                new ServerSocket(getPortToBind(), 5,
                                 InetAddress.getByName(getAddressToBind()));

            Socket socket;

            while (state.getValue()==StartStopState.STARTED) {
                socket = server.accept();

                if ((state.getValue()==StartStopState.STOPPED)
                        || (socket==null)) {
                    break;
                }

                log.info("Connection accepted, creating forwarding channel");

                ForwardingChannel channel =
                    createChannel(hostToConnect, portToConnect, socket);

                if (connection.openChannel(channel)) {
                    log.info("Forwarding channel for '" + name + "' is open");
                } else {
                    log.warn("Failed to open forwarding channel " + name);
                }
            }
        } catch (IOException ioe) {
            /* only warn if the forwarding has not been stopped */
            if (state.getValue()==StartStopState.STARTED) {
                log.warn("Local forwarding listener to " + hostToConnect + ":"
                         + String.valueOf(portToConnect) + " has failed", ioe);
            }
          } finally {
            stop();
         }
    }

    /**
     * Return if the listening is running
     */
    public boolean isRunning() {
        return thread != null && thread.isAlive();
    }

    /**
     * Starts the forwarding listener
     */
    public void start() {
        /* Set the state by calling the super method */
        super.start();

        /* Create a thread and start it */
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Stops the forwarding listener
     */
    public void stop() {
        /* Set the state by calling the super method */
        super.stop();

        try {
            /* Close the server socket */
            server.close();
        } catch (IOException ioe) {
            log.warn("Forwarding listener failed to stop", ioe);
        }

        thread = null;
    }

    /**
     * Abstract method that creates the required type of channel for the
     * listener
     *
     * @param hostToConnect The host to which forwarding is being delievered
     * @param portToConnect The port to which forwarding is being delievered
     * @param socket The connected socket to forward
     *
     * @return an initialized forwarding channel ready for opening
     *
     * @throws ForwardingConfigurationException if the channel cannot be created
     */
    protected abstract ForwardingChannel createChannel(String hostToConnect,
                                                       int portToConnect,
                                                       Socket socket)
        throws ForwardingConfigurationException;
}
