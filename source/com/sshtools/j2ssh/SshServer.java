/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sshtools.j2ssh.authentication.AuthenticationProtocol;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.ServerConfiguration;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.connection.ConnectionProtocol;
import com.sshtools.j2ssh.forwarding.ForwardingServer;
import com.sshtools.j2ssh.session.SessionChannelFactory;
import com.sshtools.j2ssh.transport.TransportProtocolServer;
import com.sshtools.j2ssh.transport.TransportProtocolState;
import com.sshtools.j2ssh.util.InvalidStateException;
import com.sshtools.j2ssh.util.State;

/**
 *  This class implements an SSH server. To configure server properties such as
 *  listening port, bound address etc edit the server configuration file
 *  server.xml.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public class SshServer {
    private static Logger log = Logger.getLogger(SshServer.class);
    private static ServerConfiguration config =
            ConfigurationLoader.getServerConfiguration();
    private ConnectionListener listener = null;
    private ServerSocket server = null;
    private boolean shutdown = false;


    /**
     *  Creates the SshServer instance
     *
     *@exception  SshException  Description of the Exception
     *@throws  SshException     if a critical service operation fails
     */
    public SshServer()
             throws SshException {
        if (config == null) {
            throw new SshException("Server configuration not available!");
        }
    }


    /**
     *  The main method for running an SSH server.<br>
     *  <br>
     *  The following arguments are currently available:<br>
     *  <br>
     *  -start Starts the server<br>
     *  -stop Stops the server<br>
     *
     *
     *@param  args  the command line arguments
     */
    public static void main(String args[]) {
        try {
            org.apache.log4j.BasicConfigurator.configure();

            if (args[0].equals("-start")) {
                start();
            }

            if (args[0].equals("-stop")) {
                stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  Starts the SSH server application.
     *
     *@throws  SshException  if a critical error occurs
     */
    public static void start()
             throws SshException {
        SshServer server = new SshServer();
        server.startServer();
    }


    /**
     *  Starts the server instance
     */
    public void startServer() {
        log.info("Starting server");
        startServerSocket();
        startCommandSocket();
    }


    /**
     *  Stops the SSH server application.
     */
    public static void stop() {
        try {
            Socket socket =
                    new Socket(InetAddress.getLocalHost(), config.getCommandPort());
            socket.getOutputStream().write(0x3a);
            socket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     *  Process a command instruction from the listening command socket.
     *
     *@param  command       the command to process e.g. stop
     *@param  client        the socket connected to the command listener
     *@throws  IOException  if an IO error occurs
     */
    protected void processCommand(int command, Socket client)
             throws IOException {
        if (command == 0x3a) {
            stopServer();
        }
    }


    /**
     *  Starts a server socket to listen on the local host for command
     *  instructions. This currently implements the stop command only.
     */
    protected void startCommandSocket() {
        try {
            ServerSocket socket =
                    new ServerSocket(config.getCommandPort(), 50,
                    InetAddress.getLocalHost());
            Socket client;

            while ((client = socket.accept()) != null) {
                log.info("Command request received");
                // Read and process the command
                processCommand(client.getInputStream().read(), client);
                client.close();

                if (shutdown) {
                    break;
                }
            }

            socket.close();
        } catch (Exception e) {
            if (!shutdown) {
                log.fatal("The command socket failed", e);
            }
        }
    }


    /**
     *  Starts a server socket that waits for SSH client requests
     */
    protected void startServerSocket() {
        listener =
                new ConnectionListener(config.getListenAddress(), config.getPort());
        listener.start();
    }


    /**
     *  Stops the server socket.
     */
    protected void stopServer() {
        log.info("Shutting down");
        listener.stop();
        listener = null;
        System.exit(0);
    }


    /**
     *  This class implements a connected session thread to track each
     *  connection made to the SSH server.
     *
     *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
     *@created    20 December 2002
     *@version    $Id$
     */
    class ConnectedSession
             implements Runnable {
        private AuthenticationProtocol authentication;
        private ConnectionProtocol connection;
        private ForwardingServer forwarding;
        private Logger log = Logger.getLogger(ConnectedSession.class);
        private Socket socket;
        private Thread thread;
        private TransportProtocolServer transport;
        private boolean refuse;


        /**
         *  Creates a new ConnectedSession object.
         *
         *@param  socket  the connected socket
         *@param  refuse  <tt>true</tt> if the connection is to negotiate the
         *      protocol version and refuse the connection otherwise<tt>false
         *      </tt>
         */
        public ConnectedSession(Socket socket, boolean refuse) {
            this.socket = socket;
            this.thread = new Thread(this);
            thread.setDaemon(true);
            this.refuse = refuse;
            thread.start();
        }


        /**
         *  Gets the state of the connection
         *
         *@return    the connection's state
         */
        public TransportProtocolState getState() {
            return transport.getState();
        }


        /**
         *  Attaches the connected socket to a <code>TransportProtocolServer</code>
         *  instance and allows the session and port forwarding channels to be
         *  requested by the client.
         */
        public void run() {
            try {
                log.debug("Initializing connection");

                InetAddress address =
                        ((InetSocketAddress) socket.getRemoteSocketAddress())
                        .getAddress();

                log.debug("Remote Hostname: " + address.getHostName());
                log.debug("Remote IP: " + address.getHostAddress());

                // If we have too many connections we may need to refuse
                if (refuse) {
                    transport = new TransportProtocolServer(true);
                    transport.startTransportProtocol(socket,
                            new SshConnectionProperties());
                    thread = null;
                    return;
                }

                // Create the Transport Protocol
                transport = new TransportProtocolServer();

                // Create the Authentication Protocol
                authentication = new AuthenticationProtocol();

                // Create the Connection Protocol
                connection = new ConnectionProtocol();

                connection.allowChannelOpen(new SessionChannelFactory());

                forwarding = new ForwardingServer(connection);

                // Allow the Connection Protocol to be accepted by the Authentication Protocol
                authentication.acceptService(connection);

                // Allow the Authentication Protocol to be accepted by the Transport Protocol
                transport.acceptService(authentication);

                // Start the Transport Protocol
                transport.startTransportProtocol(socket,
                        new SshConnectionProperties());

            } catch (IOException e) {
                log.error("The session failed to initialize", e);
            }
        }


        /**
         *  Shuts the connection
         */
        public void shutdown() {
            transport.disconnect("The server is shutting down");
        }
    }


    /**
     *  This class implements a listener that listens on an address and port for
     *  client connections. For each connection the listener creates a <code>ConnectedSession</code>
     *  instance. If the maximum number of connections has been reached the
     *  connection is still made but immedialty sends a disconnect with too many
     *  connections reason.
     *
     *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
     *@created    20 December 2002
     *@version    $Id$
     */
    class ConnectionListener
             implements Runnable {
        private List activeConnections = new ArrayList();
        private Logger log = Logger.getLogger(ConnectionListener.class);
        private ServerSocket server;
        private String listenAddress;
        private Thread thread;
        private int maxConnections;
        private int port;
        private ConnectionListenerState state = new ConnectionListenerState();


        /**
         *  Creates a new ConnectionListener object.
         *
         *@param  listenAddress  the address to bind to
         *@param  port           the port to bind to
         */
        public ConnectionListener(String listenAddress, int port) {
            thread = new Thread(this);
            thread.setDaemon(true);
            this.port = port;
            this.listenAddress = listenAddress;
        }


        /**
         *  Gets a list of the active connections
         *
         *@return    a List of <code>ConnectedSession</code>
         */
        public List getActiveConnections() {
            return activeConnections;
        }


        /**
         *  Starts the server socket and listens for connections
         */
        public void run() {
            try {
                log.debug("Starting connection listener thread");

                state.isListening(true);

                server = new ServerSocket(port);

                Socket socket;
                maxConnections = config.getMaxConnections();
                boolean refuse = false;

                while ((socket = server.accept()) != null &&
                        state.getValue() == ConnectionListenerState.STARTED) {
                    log.debug("New connection requested");

                    if (maxConnections > activeConnections.size()
                            || maxConnections == 0) {
                        ConnectedSession session =
                                new ConnectedSession(socket, false);
                        activeConnections.add(session);

                    } else {
                        ConnectedSession session =
                                new ConnectedSession(socket, true);
                    }

                }

                state.isListening(false);

                log.info("Exiting connection listener thread");
            } catch (IOException ioe) {
                if (state.getValue() != ConnectionListenerState.STOPPED) {
                    log.debug("The listening socket failed", ioe);
                }
            } finally {

                thread = null;
            }
        }


        /**
         *  Starts the connection listener
         */
        public void start() {
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }


        /**
         *  Stops the connection listener
         */
        public void stop() {
            try {

                state.isListening(false);

                server.close();

                // Close all the connected sessions
                Iterator it = activeConnections.iterator();

                while (it.hasNext()) {
                    ConnectedSession session = (ConnectedSession) it.next();
                    session.shutdown();
                }

            } catch (IOException ioe) {
                log.warn("The listening socket failed to stop cleanly", ioe);
            }
        }


        /**
         *  Defines a simple state for the ConnectionListener
         *
         *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
         *@created    20 December 2002
         *@version    $Id: SshServer.java,v 1.6 2002/12/12 21:30:48 martianx Exp
         *      $
         */
        class ConnectionListenerState extends State {

            /**
             *  Description of the Field
             */
            public final static int STARTED = 1;
            /**
             *  Description of the Field
             */
            public final static int STOPPED = 2;


            /**
             *  Constructor
             */
            public ConnectionListenerState() {
                super(STOPPED);
            }


            /**
             *  Validates the state
             *
             *@param  state  the state value to validate
             *@return        <tt>true</tt> if the state is valid otherwies <tt>
             *      false</tt>
             */
            public boolean isValidState(int state) {
                return (state == STARTED || state == STOPPED);
            }


            /**
             *  Set the state
             *
             *@param  listening  <tt>true</tt> if the listener is active
             *      otherwise <tt>false</tt>
             */
            public void isListening(boolean listening) {
                if (listening) {
                    setValue(STARTED);
                } else {
                    setValue(STOPPED);
                }
            }

        }


        /**
         *  This class implements a monitor used to detect the closing of a
         *  <code>ConnectedSession</code> for removal from the active
         *  connections list.
         *
         *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
         *@created    20 December 2002
         *@version    $Id: SshServer.java,v 1.6 2002/12/12 21:30:48 martianx Exp
         *      $
         */
        class ConnectedSessionMonitor
                 implements Runnable {
            private ConnectedSession session;
            private Logger log =
                    Logger.getLogger(ConnectedSessionMonitor.class);
            private Thread thread;


            /**
             *  Creates a new ConnectedSessionMonitor object.
             *
             *@param  session  the connected session to monitor
             */
            public ConnectedSessionMonitor(ConnectedSession session) {
                this.session = session;
                this.thread = new Thread(this);
                thread.setDaemon(true);
                thread.start();
            }


            /**
             *  Gets the state of the connection and waits for the disconnected
             *  state. When disconnected the monitor removes the connection from
             *  the active connection list.
             */
            public void run() {
                log.debug("Starting connection monitor thread");

                TransportProtocolState state = session.getState();

                try {
                    state.waitForState(TransportProtocolState.DISCONNECTED);
                } catch (InvalidStateException ise) {
                }

                log.debug("Connection disconnected, removing from active connections");
                activeConnections.remove(session);
            }
        }
    }
}
