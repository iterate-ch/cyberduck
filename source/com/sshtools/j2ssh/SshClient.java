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

import java.net.Socket;

import java.util.List;
import java.util.Properties;

import com.sshtools.j2ssh.authentication.AuthenticationProtocol;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.SshAuthentication;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.connection.ConnectionProtocol;
import com.sshtools.j2ssh.forwarding.ForwardingClient;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.ConsoleHostKeyVerification;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.TransportProtocolClient;
import com.sshtools.j2ssh.transport.TransportProtocolState;
import com.sshtools.j2ssh.util.State;

/**
 *  This class implements an SSH client connection providing access to session
 *  and port forwarding channels.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public class SshClient {
    private static Logger log = Logger.getLogger(SshClient.class);
    private AuthenticationProtocol authentication;
    private ConnectionProtocol connection;
    private ForwardingClient forwarding;
    private List authMethods;
    private Socket socket;
    private String authenticationBanner;
    private TransportProtocolClient transport;
    private int authenticationState = AuthenticationProtocolState.UNINITIALIZED;


    /**
     *  Constructor for the SshClient object
     */
    public SshClient() { }


    /**
     *  Returns the authenticaiton banner supplied by the remote server.
     *
     *@return    the authentication banner, may be null
     */
    public String getAuthenticationBanner() {
        return authenticationBanner;
    }


    /**
     *  Provides the list of available authentication methods for the current
     *  user. This list could possibly contain authentication methods that are
     *  not allowed for the user as detailed within the ssh transport protocol
     *  specification.
     *
     *@param  username       the user name
     *@return                A list containing the method names as specified in
     *      [SSH-USERAUTH]
     *@throws  SshException  if a critical error occurs
     */
    public List getAvailableAuthMethods(String username)
             throws IOException {
        if (authentication != null) {
            return authentication.getAvailableAuths(username,
                    connection.getServiceName());
        } else {
            return null;
        }
    }


    /**
     *  Determine if the client is connected.
     *
     *@return    <tt>true</tt> if the connection is still active otherwise <tt>
     *      false</tt>
     */
    public boolean isConnected() {
        State state = transport == null ? null : transport.getState();
        int value = state == null ? TransportProtocolState.DISCONNECTED :
            state.getValue();

        return ((value == TransportProtocolState.CONNECTED)
                || (value == TransportProtocolState.PERFORMING_KEYEXCHANGE));
    }


    /**
     *  Gets the transport protocols state; use this state object to determine
     *  the current state of the transport protocol and to wait for state change
     *  notifications.
     *
     *@return    the transport protocols state instance
     */
    public TransportProtocolState getConnectionState() {
        return transport.getState();
    }


    /**
     *  Gets the client forwarding manager which can be used to configure and
     *  start port forwardings.
     *
     *@return    the client's forwarding manager
     */
    public ForwardingClient getForwardingClient() {
        return forwarding;
    }


    /**
     *  When the transport protocol negotiates the protocol version, it uses the
     *  EOL provided with the protocol identification string as the guessed EOL
     *  setting for the remote computer.<br>
     *  <br>
     *  The value returned will be any of the following values:<br>
     *  <br>
     *  TransportProtocol.EOL_CRLF<br>
     *  TransportProtocol.EOL_LF<br>
     *
     *
     *@return    the guessed EOL value
     */
    public int getRemoteEOL() {
        return transport.getRemoteEOL();
    }


    /**
     *  Authenticates the user.
     *
     *@param  auth              The authenticaiton instance to use
     *@return                   The current state value of the authentication
     *      protocols <code>
     *         AuthenticationProtocolState</code> instance.
     *@exception  SshException  if a critical error occurs
     */
    public int authenticate(SshAuthentication auth)
             throws IOException {
        // Do the authentication
        authenticationState = authentication.authenticate(auth, connection);
        if(authenticationState==AuthenticationProtocolState.COMPLETE
            || authenticationState==AuthenticationProtocolState.PARTIAL) {




        }

        return authenticationState;
    }


    /**
     *  Connects the client to an SSH server on the standard port 22 and using
     *  the default <code>ConsoleHostKeyVerification</code> instance to verify
     *  the host key.<br>
     *  <br>
     *  All connection properties such as cipher and message authentication
     *  algorithms are defaulted.
     *
     *@param  hostname       the hostname to connect
     *@throws  SshException  if a critical error occurs
     */
    public void connect(String hostname)
             throws IOException {
        connect(hostname, 22, new ConsoleHostKeyVerification());
    }


    /**
     *  Connects the client to an SSH server on the standard port 22.<br>
     *  <br>
     *  All connection properties such as cipher and message authentication
     *  algorithms are defaulted.
     *
     *@param  hostname       the hostname to connect.
     *@param  hosts          a host key verification implementation for user
     *      interaction to verifiy the servers host key.
     *@throws  SshException  if a critical error occurs
     */
    public void connect(String hostname, HostKeyVerification hosts)
             throws IOException {
        connect(hostname, 22, hosts);
    }


    /**
     *  Connects the client to an SSH server using the default <code>
     * ConsoleHostKeyVerification</code> instance to verify the host key.<br>
     *  <br>
     *  All connection properties such as cipher and message authentication
     *  algorithms are defaulted.
     *
     *@param  hostname       the hostname to connect
     *@param  port           the port to connect
     *@throws  SshException  if a critical error occurs
     */
    public void connect(String hostname, int port)
             throws IOException {
        connect(hostname, port, new ConsoleHostKeyVerification());
    }


    /**
     *  Connects the client to an SSH server.<br>
     *  <br>
     *  All connection properties such as cipher and message authentication
     *  algorithms are defaulted.
     *
     *@param  hostname       the hostname to connect
     *@param  port           the port to connect
     *@param  hosts          a host key verification implementation for user
     *      interaction to verifiy the servers host key.
     *@throws  SshException  if a critical error occurs
     */
    public void connect(String hostname, int port, HostKeyVerification hosts)
             throws IOException {
        SshConnectionProperties properties = new SshConnectionProperties();
        properties.setHost(hostname);
        properties.setPort(port);

        connect(properties, hosts);
    }


    /**
     *  Connects to client to an SSH server
     *
     *@param  properties        the connection properties to use
     *@param  hostVerification  a host key verification implementation for user
     *      interaction to verifiy the servers host key.
     *@throws  SshException     if a critical error occurs
     */
    public void connect(SshConnectionProperties properties,
            HostKeyVerification hostVerification)
             throws IOException {

        socket = new Socket(properties.getHost(), properties.getPort());

        // Start the transport protocol
        transport = new TransportProtocolClient(hostVerification);
        transport.startTransportProtocol(socket, properties);

        // Start the authentication protocol
        authentication = new AuthenticationProtocol();
        transport.requestService(authentication);

        connection = new ConnectionProtocol();
        forwarding = new ForwardingClient(connection);

        // If we've received an authentication banner the record it
        authenticationBanner = authentication.getBannerMessage();
    }


    /**
     *  Disconnects the SSH connection
     */
    public void disconnect() {
        if(connection != null)
            connection.stop();
        if(transport != null)
            transport.disconnect("Terminating connection");
    }


    /**
     *  Opens a session channel. Use the methods on the <code>SessionChannelClient</code>
     *  object to execute commands, start the users shell or start a subsystem.
     *
     *@return                an open session channel
     *@throws  SshException  if a critical error occurs
     */
    public SessionChannelClient openSessionChannel()
             throws IOException {
        if (authenticationState != AuthenticationProtocolState.COMPLETE) {
            throw new SshException("Authentication has not been completed!");
        }

        SessionChannelClient session = new SessionChannelClient();

        if (!connection.openChannel(session)) {
            throw new SshException("The server refused to open a session");
        }

        return session;
    }
}
