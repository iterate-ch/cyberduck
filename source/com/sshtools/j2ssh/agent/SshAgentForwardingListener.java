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
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.connection.ConnectionProtocol;
import com.sshtools.j2ssh.util.StartStopState;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshAgentForwardingListener {
    private static Log log = LogFactory.getLog(SshAgentForwardingListener.class);
    private static HashMap agents = new HashMap();
    ServerSocket server;
    int port;
    String location;
    StartStopState state = new StartStopState(StartStopState.STOPPED);
    Thread thread;
    ConnectionProtocol connection;
    Vector references = new Vector();
    String sessionId;

    SshAgentForwardingListener(String sessionId, ConnectionProtocol connection) {
        log.info("Forwarding agent started");
        this.sessionId = sessionId;
        this.connection = connection;
        port = selectPort();
        location = "localhost:" + String.valueOf(port);
        thread = new Thread(new Runnable() {
            public void run() {
                state.setValue(StartStopState.STARTED);

                try {
                    server = new ServerSocket(port, 5,
                            InetAddress.getByName("localhost"));

                    //server.bind(new InetSocketAddress("localhost", port));
                    Socket socket;

                    while ((state.getValue() == StartStopState.STARTED) &&
                            ((socket = server.accept()) != null)) {
                        AgentSocketChannel channel = new AgentSocketChannel(true);
                        channel.bindSocket(socket);

                        if (!SshAgentForwardingListener.this.connection.openChannel(channel)) {
                            log.warn("Failed to open agent forwarding channel");
                        }
                    }
                }
                catch (Exception e) {
                    if (state.getValue() == StartStopState.STARTED) {
                        log.warn("Forwarding agent socket failed", e);
                    }
                }

                state.setValue(StartStopState.STOPPED);
            }
        });
    }

    /**
     * @return
     */
    public String getConfiguration() {
        return location;
    }

    /**
     * @param obj
     */
    public void addReference(Object obj) {
        if (!references.contains(obj)) {
            references.add(obj);
        }
    }

    /**
     * @param obj
     */
    public void removeReference(Object obj) {
        if (references.contains(obj)) {
            references.remove(obj);

            if (references.size() == 0) {
                stop();
                agents.remove(sessionId);
            }
        }
    }

    /**
     * @throws IOException
     */
    public void start() throws IOException {
        thread.start();
    }

    /**
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     *
     */
    public void stop() {
        try {
            state.setValue(StartStopState.STOPPED);
            server.close();
        }
        catch (IOException ex) {
        }
    }

    private int selectPort() {
        return 49152 +
                (int)Math.round(((float)16383 * ConfigurationLoader.getRND()
                .nextFloat()));
    }

    /**
     * @param sessionId
     * @param connection
     * @return
     * @throws AgentNotAvailableException
     */
    public static SshAgentForwardingListener getInstance(String sessionId,
                                                         ConnectionProtocol connection) throws AgentNotAvailableException {
        if (agents.containsKey(sessionId)) {
            SshAgentForwardingListener agent = (SshAgentForwardingListener)agents.get(sessionId);

            return agent;
        }
        else {
            try {
                SshAgentForwardingListener agent = new SshAgentForwardingListener(sessionId,
                        connection);
                agent.start();
                agents.put(sessionId, agent);

                return agent;
            }
            catch (IOException ex) {
                throw new AgentNotAvailableException();
            }
        }
    }
}
