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
package com.sshtools.j2ssh.agent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.util.StartStopState;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshAgentSocketListener {
    private static Log log = LogFactory.getLog(SshAgentSocketListener.class);
    StartStopState state = new StartStopState(StartStopState.STOPPED);
    KeyStore keystore;
    ServerSocket server;
    int port;
    Thread thread;
    String location;

    /**
     * Creates a new SshAgentSocketListener object.
     *
     * @param location the location of the listening agent. This should be a
     *                 random port on the localhost such as localhost:15342
     * @param keystore the keystore for agent operation
     * @throws AgentNotAvailableException if the location specifies an invalid
     *                                    location
     */
    public SshAgentSocketListener(String location, KeyStore keystore)
            throws AgentNotAvailableException {
        log.info("New SshAgent instance created");

        // Verify the agent location
        this.location = location;

        if (location == null) {
            throw new AgentNotAvailableException();
        }

        this.location = location;

        int idx = location.indexOf(":");

        if (idx == -1) {
            throw new AgentNotAvailableException();
        }

        String host = location.substring(0, idx);
        port = Integer.parseInt(location.substring(idx + 1));
        this.keystore = keystore;

        try {
            server = new ServerSocket(port, 5, InetAddress.getByName(host));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the agent listeners state
     *
     * @return the current state of the listener
     */
    public StartStopState getState() {
        return state;
    }

    /**
     * Starts the agent listener thread
     */
    public void start() {
        thread = new Thread(new Runnable() {
            public void run() {
                try {
                    Socket socket;
                    System.setProperty("sshtools.agent", location);
                    state.setValue(StartStopState.STARTED);

                    while ((socket = server.accept()) != null) {
                        SshAgentConnection agentClient = new SshAgentConnection(keystore,
                                socket.getInputStream(),
                                socket.getOutputStream());
                    }

                    thread = null;
                }
                catch (IOException ex) {
                    log.info("The agent listener closed: " +
                            ex.getMessage());
                }
                finally {
                    state.setValue(StartStopState.STOPPED);
                }
            }
        });
        thread.start();
    }

    /**
     * The current port of the agent listener
     *
     * @return the integer port
     */
    public int getPort() {
        return port;
    }

    /**
     * Stops the agent listener
     */
    public void stop() {
        try {
            server.close();
        }
        catch (IOException ex) {
        }
    }

    /**
     * Gets the underlying keystore for this agent listener.
     *
     * @return the keystore
     */
    protected KeyStore getKeystore() {
        return keystore;
    }

    /**
     * Configure a new random port for the agent listener.
     *
     * @return the random port for this agent.
     */
    public static int configureNewLocation() {
        return 49152 + (int)Math.round(((float)16383 * Math.random()));
    }

    /**
     * The main entry point for the application. This method currently accepts
     * the -start parameter which will look for the sshtools.agent system
     * property. To configure the agent and to get a valid location call with
     * -configure, set the system sshtools.home system property and start.
     *
     * @param args the programs arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("-start")) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            SshAgentSocketListener agent = new SshAgentSocketListener(System.getProperty("sshtools.agent"),
                                    new KeyStore());
                            agent.start();
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                thread.start();
            }

            if (args[0].equals("-configure")) {
                System.out.println("SET SSHTOOLS_AGENT=localhost:" +
                        String.valueOf(configureNewLocation()));
            }
        }
    }
}
