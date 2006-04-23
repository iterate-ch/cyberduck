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
package com.sshtools.j2ssh;

import com.sshtools.j2ssh.authentication.AuthenticationProtocolClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelEventAdapter;
import com.sshtools.j2ssh.connection.ChannelEventListener;
import com.sshtools.j2ssh.connection.ChannelFactory;
import com.sshtools.j2ssh.connection.ConnectionProtocol;
import com.sshtools.j2ssh.net.HttpProxySocketProvider;
import com.sshtools.j2ssh.net.SocketTransportProvider;
import com.sshtools.j2ssh.net.SocksProxySocket;
import com.sshtools.j2ssh.net.TransportProvider;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.SshMsgIgnore;
import com.sshtools.j2ssh.transport.TransportProtocolClient;
import com.sshtools.j2ssh.transport.TransportProtocolState;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.util.State;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * <p/>
 * Implements an SSH client with methods to connect to a remote server and
 * perform all necersary SSH functions such as SCP, SFTP, executing commands,
 * starting the users shell and perform port forwarding.
 * </p>
 * <p/>
 * <p/>
 * There are several steps to perform prior to performing the desired task.
 * This involves the making the initial connection, authenticating the user
 * and creating a session to execute a command, shell or subsystem and/or
 * configuring the port forwarding manager.
 * </p>
 * <p/>
 * <p/>
 * To create a connection use the following code:<br>
 * <blockquote><pre>
 * // Create a instance and connect SshClient
 * ssh = new SshClient();
 * ssh.connect("hostname");
 * </pre></blockquote>
 * Once this code has executed and returned
 * the connection is ready for authentication:<br>
 * <blockquote><pre>
 * PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
 * pwd.setUsername("foo");
 * pwd.setPassword("xxxxx");
 * // Authenticate the user
 * int result = ssh.authenticate(pwd);
 * if(result==AuthenticationProtocolState.COMPLETED) {
 *    // Authentication complete
 * }
 * </pre></blockquote>
 * Once authenticated the user's shell can be started:<br>
 * <blockquote><pre>
 * // Open a session channel
 * SessionChannelClient session =
 *                      ssh.openSessionChannel();
 * <p/>
 * // Request a pseudo terminal, if you do not you may not see the prompt
 * if(session.requestPseudoTerminal("ansi", 80, 24, 0, 0, "") {
 *      // Start the users shell
 *      if(session.startShell()) {
 *         // Do something with the session output
 *         session.getOutputStream().write("echo message\n");
 *         ....
 *       }
 * }
 * </pre></blockquote>
 * </p>
 *
 * @author Lee David Painter
 * @version $Revision$
 * @since 0.2.0
 */
public class SshClient {
	private static Logger log = Logger.getLogger(SshClient.class);

	/**
	 * The SSH Authentication protocol implementation for this SSH client. The
	 * SSH Authentication protocol runs over the SSH Transport protocol as a
	 * transport protocol service.
	 */
	protected AuthenticationProtocolClient authentication;

	/**
	 * The SSH Connection protocol implementation for this SSH client. The
	 * connection protocol runs over the SSH Transport protocol as a transport
	 * protocol service and is started by the authentication protocol after a
	 * successful authentication.
	 */
	protected ConnectionProtocol connection;

    /**
     * The underlying socket
     */
    private TransportProvider socket;

    /**
	 * The SSH Transport protocol implementation for this SSH Client.
	 */
	protected TransportProtocolClient transport;

	/**
	 * The current state of the authentication for the current connection.
	 */
	protected int authenticationState = AuthenticationProtocolState.READY;

	/**
	 * The timeout in milliseconds for the underlying transport provider
	 * (typically a Socket).
	 */
	protected int socketTimeout = 0;

	/**
	 * A Transport protocol event handler instance that receives notifications
	 * of transport layer events such as Socket timeouts and disconnection.
	 */
	protected SshEventAdapter eventHandler = null;

	/**
	 * The currently active channels for this SSH Client connection.
	 */
	protected Vector activeChannels = new Vector();

	/**
	 * An channel event listener implemention to maintain the active channel
	 * list.
	 */
	protected ActiveChannelEventListener activeChannelListener = new ActiveChannelEventListener();

	/**
	 * Flag indicating whether the forwarding instance is created when the
	 * connection is made.
	 */
	protected boolean useDefaultForwarding = true;

	/**
	 * <p/>
	 * Contructs an unitilialized SshClient ready for connecting.
	 * </p>
	 */
	public SshClient() {
	}

	/**
	 * <p/>
	 * Returns the server's authentication banner.
	 * </p>
	 * <p/>
	 * <p/>
	 * In some jurisdictions, sending a warning message before authentication
	 * may be relevant for getting legal protection.  Many UNIX machines, for
	 * example, normally display text from `/etc/issue', or use "tcp wrappers"
	 * or similar software to display a banner before issuing a login prompt.
	 * </p>
	 * <p/>
	 * <p/>
	 * The server may or may not send this message. Call this method to
	 * retrieve the message, specifying a timeout limit to wait for the
	 * message.
	 * </p>
	 *
	 * @param timeout The number of milliseconds to wait for the banner message
	 *                before returning
	 * @return The server's banner message
	 * @throws IOException If an IO error occurs reading the message
	 * @since 0.2.0
	 */
	public String getAuthenticationBanner(int timeout)
	    throws IOException {
		if(authentication == null) {
			return "";
		}
		else {
			return authentication.getBannerMessage(timeout);
		}
	}

	/**
	 * <p/>
	 * Returns the list of available authentication methods for a given user.
	 * </p>
	 * <p/>
	 * <p/>
	 * A client may request a list of authentication methods that may continue
	 * by using the "none" authentication method.This method calls the "none"
	 * method and returns the available authentication methods.
	 * </p>
	 *
	 * @param username The name of the account for which you require the
	 *                 available authentication methods
	 * @return A list of Strings, for example "password", "publickey" &
	 *         "keyboard-interactive"
	 * @throws IOException If an IO error occurs during the operation
	 * @since 0.2.0
	 */
	public List getAvailableAuthMethods(String username)
	    throws IOException {
		if(authentication != null) {
			return authentication.getAvailableAuths(username,
			    connection.getServiceName());
		}
		else {
			return null;
		}
	}

	/**
	 * <p/>
	 * Returns the connection state of the client.
	 * </p>
	 *
	 * @return true if the client is connected, false otherwise
	 * @since 0.2.0
	 */
	public boolean isConnected() {
		State state = (transport == null) ? null : transport.getState();
		int value = (state == null) ? TransportProtocolState.DISCONNECTED
		            : state.getValue();

		return ((value == TransportProtocolState.CONNECTED) ||
		    (value == TransportProtocolState.PERFORMING_KEYEXCHANGE));
	}

	/**
	 * <p/>
	 * Evaluate whether the client has successfully authenticated.
	 * </p>
	 *
	 * @return true if the client is authenticated, otherwise false
	 */
	public boolean isAuthenticated() {
		return authenticationState == AuthenticationProtocolState.COMPLETE;
	}

	/**
	 * <p/>
	 * Returns the identification string sent by the server during protocol
	 * negotiation. For example "SSH-2.0-OpenSSH_p3.4".
	 * </p>
	 *
	 * @return The server's identification string.
	 * @since 0.2.0
	 */
	public String getServerId() {
		return transport.getRemoteId();
	}

	/**
	 * <p/>
	 * Returns the server's public key supplied during key exchange.
	 * </p>
	 *
	 * @return the server's public key
	 * @since 0.2.0
	 */
	public SshPublicKey getServerHostKey() {
		return transport.getServerHostKey();
	}

	/**
	 * <p/>
	 * Returns the transport protocol's connection state.
	 * </p>
	 *
	 * @return The transport protocol's state
	 * @since 0.2.0
	 */
	public TransportProtocolState getConnectionState() {
		return transport.getState();
	}

	/**
	 * <p/>
	 * Return's a rough guess at the server's EOL setting. This is simply
	 * derived from the identification string and should not be used as a cast
	 * iron proof on the EOL setting.
	 * </p>
	 *
	 * @return The transport protocol's EOL constant
	 * @since 0.2.0
	 */
	public int getRemoteEOL() {
		return transport.getRemoteEOL();
	}

	/**
	 * <p/>
	 * Set the event handler for the underlying transport protocol.
	 * </p>
	 * <blockquote>
	 * <pre>
	 * ssh.setEventHandler(new TransportProtocolEventHandler() {
	 * <p/>
	 *   public void onSocketTimeout(TransportProtocol transport) {<br>
	 *     // Do something to handle the socket timeout<br>
	 *   }
	 * <p/>
	 *   public void onDisconnect(TransportProtocol transport) {
	 *     // Perhaps some clean up?
	 *   }
	 * });
	 * </pre>
	 * </blockquote>
	 *
	 * @param eventHandler The event handler instance to receive transport
	 *                     protocol events
	 * @see com.sshtools.j2ssh.transport.TransportProtocolEventHandler
	 * @since 0.2.0
	 */
	public void addEventHandler(SshEventAdapter eventHandler) {
		// If were connected then add, otherwise store for later connection
		if(transport != null) {
			transport.addEventHandler(eventHandler);
			authentication.addEventListener(eventHandler);
		}
		else {
			this.eventHandler = eventHandler;
		}
	}

	/**
	 * <p/>
	 * Set's the socket timeout (in milliseconds) for the underlying transport
	 * provider. This MUST be called prior to connect.
	 * </p>
	 * <blockquote>
	 * SshClient ssh = new SshClient();
	 * ssh.setSocketTimeout(30000);
	 * ssh.connect("hostname");
	 * </blockquote>
	 *
	 * @param milliseconds The number of milliseconds without activity before
	 *                     the timeout event occurs
	 * @since 0.2.0
	 */
	public void setSocketTimeout(int milliseconds) {
		this.socketTimeout = milliseconds;
	}

	/**
	 * <p/>
	 * Return's a rough guess at the server's EOL setting. This is simply
	 * derived from the identification string and should not be used as a cast
	 * iron proof on the EOL setting.
	 * </p>
	 *
	 * @return The EOL string
	 * @since 0.2.0
	 */
	public String getRemoteEOLString() {
		return ((transport.getRemoteEOL() == TransportProtocolClient.EOL_CRLF)
		        ? "\r\n" : "\n");
	}

	/**
	 * Get the connection properties for this connection.
	 *
	 * @return
	 */
	public SshConnectionProperties getConnectionProperties() {
		return transport.getProperties();
	}

	/**
	 * <p/>
	 * Authenticate the user on the remote host.
	 * </p>
	 * <p/>
	 * <p/>
	 * To authenticate the user, create an <code>SshAuthenticationClient</code>
	 * instance and configure it with the authentication details.
	 * </p>
	 * <code> PasswordAuthenticationClient pwd = new
	 * PasswordAuthenticationClient(); pwd.setUsername("root");
	 * pwd.setPassword("xxxxxxxxx"); int result = ssh.authenticate(pwd);
	 * </code>
	 * <p/>
	 * <p/>
	 * The method returns a result value will one of the public static values
	 * defined in <code>AuthenticationProtocolState</code>. These are<br>
	 * <br>
	 * COMPLETED - The authentication succeeded.<br>
	 * PARTIAL   - The authentication succeeded but a further authentication
	 * method is required.<br>
	 * FAILED    - The authentication failed.<br>
	 * CANCELLED - The user cancelled authentication (can only be returned
	 * when the user is prompted for information.<br>
	 * </p>
	 *
	 * @param auth A configured SshAuthenticationClient instance ready for
	 *             authentication
	 * @return The authentication result
	 * @throws IOException If an IO error occurs during authentication
	 * @since 0.2.0
	 */
	public int authenticate(SshAuthenticationClient auth)
	    throws IOException {
		// Do the authentication
		authenticationState = authentication.authenticate(auth, connection);
		return authenticationState;
	}

	/**
	 * <p/>
	 * Determine whether a private/public key pair will be accepted for public
	 * key authentication.
	 * </p>
	 * <p/>
	 * <p/>
	 * When using public key authentication, the signing of data could take
	 * some time depending upon the available machine resources. By calling
	 * this method, you can determine whether the server will accept a key for
	 * authentication by providing the public key. The server will verify the
	 * key against the user's authorized keys and return true should the
	 * public key be authorized. The caller can then proceed with the private
	 * key operation.
	 * </p>
	 *
	 * @param username The username for authentication
	 * @param key      The public key for which authentication will be attempted
	 * @return true if the server will accept the key, otherwise false
	 * @throws IOException  If an IO error occurs during the operation
	 * @throws SshException
	 * @since 0.2.0
	 */
	public boolean acceptsKey(String username, SshPublicKey key)
	    throws IOException {
		if(authenticationState != AuthenticationProtocolState.COMPLETE) {
			PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();

			return pk.acceptsKey(authentication, username,
			    connection.getServiceName(), key);
		}
		else {
			throw new SshException("Authentication has been completed!");
		}
	}


	/**
	 * <p/>
	 * Connect the client to the server using the default connection
	 * properties.
	 * </p>
	 * <p/>
	 * <p/>
	 * This call attempts to connect to the hostname specified on the standard
	 * SSH port of 22 and uses all the default connection properties. When
	 * this method returns the connection has been established, the server's
	 * identity been verified and the connection is ready for user
	 * authentication. Host key verification will be performed using the host
	 * key verification instance provided:
	 * </p>
	 * <blockquote><pre>
	 * // Connect and consult $HOME/.ssh/known_hosts
	 * ssh.connect("hostname", new ConsoleKnownHostsKeyVerification());
	 * // Connect and allow any host
	 * ssh.connect("hostname", new
	 *                 IgnoreHostKeyVerification());
	 * </pre></blockquote>
	 * <p/>
	 * <p/>
	 * You can provide your own host key verification process by implementing
	 * the <code>HostKeyVerification</code> interface.
	 * </p>
	 *
	 * @param hostname The hostname of the server to connect
	 * @param hosts    The host key verification instance to consult for host key
	 *                 validation
	 * @throws IOException If an IO error occurs during the connect
	 *                     operation
	 * @see #connect(com.sshtools.j2ssh.configuration.SshConnectionProperties,
	    *      com.sshtools.j2ssh.transport.HostKeyVerification)
	 * @since 0.2.0
	 */
	public void connect(String hostname, HostKeyVerification hosts)
	    throws IOException {
		connect(hostname, 22, hosts);
	}


	/**
	 * <p/>
	 * Connect the client to the server on a specified port with default
	 * connection properties.
	 * </p>
	 * <p/>
	 * <p/>
	 * This call attempts to connect to the hostname and port specified. When
	 * this method returns the connection has been established, the server's
	 * identity been verified and the connection is ready for user
	 * authentication. Host key verification will be performed using the host
	 * key verification instance provided:
	 * </p>
	 * <blockquote><pre>
	 * // Connect and consult $HOME/.ssh/known_hosts
	 * ssh.connect("hostname", new ConsoleKnownHostsKeyVerification());
	 * // Connect and allow any host
	 * ssh.connect("hostname", new
	 *                 IgnoreHostKeyVerification());
	 * </pre></blockquote>
	 * <p/>
	 * <p/>
	 * You can provide your own host key verification process by implementing
	 * the <code>HostKeyVerification</code> interface.
	 * </p>
	 *
	 * @param hostname The hostname of the server to connect
	 * @param port     The port to connect
	 * @param hosts    The host key verification instance to consult for host key
	 *                 validation
	 * @throws IOException If an IO error occurs during the connect
	 *                     operation
	 * @see #connect(com.sshtools.j2ssh.configuration.SshConnectionProperties,
	    *      com.sshtools.j2ssh.transport.HostKeyVerification)
	 * @since 0.2.0
	 */
	public void connect(String hostname, int port, HostKeyVerification hosts)
	    throws IOException {
		SshConnectionProperties properties = new SshConnectionProperties();
		properties.setHost(hostname);
		properties.setPort(port);
		connect(properties, hosts);
	}

	/**
	 * <p/>
	 * Connect the client to the server with the specified properties.
	 * </p>
	 * <p/>
	 * <p/>
	 * This call attempts to connect to using the connection properties
	 * specified. When this method returns the connection has been
	 * established, the server's identity been verified and the connection is
	 * ready for user authentication. To use this method first create a
	 * properties instance and set the required fields.
	 * </p>
	 * <blockquote><pre>
	 * SshConnectionProperties properties = new
	 *                             SshConnectionProperties();
	 * properties.setHostname("hostname");
	 * properties.setPort(22);             // Defaults to 22
	 * // Set the prefered client->server encryption
	 * ssh.setPrefCSEncryption("blowfish-cbc");
	 * // Set the prefered server->client encrpytion
	 * ssh.setPrefSCEncrpyion("3des-cbc");
	 * ssh.connect(properties);
	 * </pre></blockquote>
	 * <p/>
	 * <p/>
	 * Host key verification will be performed using the host key verification
	 * instance provided:<br>
	 * <blockquote><pre>
	 * // Connect and consult $HOME/.ssh/known_hosts
	 * ssh.connect("hostname", new ConsoleKnownHostsKeyVerification());
	 * // Connect and allow any host
	 * ssh.connect("hostname", new
	 *                 IgnoreHostKeyVerification());
	 * </pre></blockquote>
	 * You can provide your own host key verification process by implementing the
	 * <code>HostKeyVerification</code> interface.
	 * </p>
	 *
	 * @param properties       The connection properties
	 * @param hostVerification The host key verification instance to consult
	 *                         for host  key validation
	 * @throws UnknownHostException If the host is unknown
	 * @throws IOException          If an IO error occurs during the connect
	 *                              operation
	 * @since 0.2.0
	 */
	public void connect(SshConnectionProperties properties,
	                    HostKeyVerification hostVerification)
	    throws UnknownHostException, IOException {

        if(properties.getTransportProvider() == SshConnectionProperties.USE_HTTP_PROXY) {
            socket = HttpProxySocketProvider.connectViaProxy(properties.getHost(),
                properties.getPort(), properties.getProxyHost(),
                properties.getProxyPort(), properties.getProxyUsername(),
                properties.getProxyPassword(), "J2SSH");
        }
        else if(properties.getTransportProvider() == SshConnectionProperties.USE_SOCKS4_PROXY) {
            socket = SocksProxySocket.connectViaSocks4Proxy(properties.getHost(),
                properties.getPort(), properties.getProxyHost(),
                properties.getProxyPort(), properties.getProxyUsername());
        }
        else if(properties.getTransportProvider() == SshConnectionProperties.USE_SOCKS5_PROXY) {
            socket = SocksProxySocket.connectViaSocks5Proxy(properties.getHost(),
                properties.getPort(), properties.getProxyHost(),
                properties.getProxyPort(), properties.getProxyUsername(),
                properties.getProxyPassword());
        }
        else {
            // No proxy just attempt a standard socket connection
            socket = new SocketTransportProvider();
            ((SocketTransportProvider)socket).connect(new InetSocketAddress(properties.getHost(), properties.getPort()));
            //socket.setTcpNoDelay(true);
            ((SocketTransportProvider)socket).setSoTimeout(socketTimeout);
        }

		// Start the transport protocol
		transport = new TransportProtocolClient(hostVerification);
		transport.addEventHandler(eventHandler);
		transport.startTransportProtocol(socket, properties);

		// Start the authentication protocol
		authentication = new AuthenticationProtocolClient();
		authentication.addEventListener(eventHandler);
		transport.requestService(authentication);
		connection = new ConnectionProtocol();
	}

	public void noop() throws IOException {
		transport.sendMessage(new SshMsgIgnore("NOOP"), this);
	}

	/**
	 * <p/>
	 * Sets the timeout value for the key exchange.
	 * </p>
	 * <p/>
	 * <p/>
	 * When this time limit is reached the transport protocol will initiate a
	 * key re-exchange. The default value is one hour with the minumin timeout
	 * being 60 seconds.
	 * </p>
	 *
	 * @param seconds The number of seconds beofre key re-exchange
	 * @throws IOException If the timeout value is invalid
	 * @since 0.2.0
	 */
	public void setKexTimeout(long seconds) throws IOException {
		transport.setKexTimeout(seconds);
	}

	/**
	 * <p/>
	 * Sets the key exchance transfer limit in kilobytes.
	 * </p>
	 * <p/>
	 * <p/>
	 * Once this amount of data has been transfered the transport protocol will
	 * initiate a key re-exchange. The default value is one gigabyte of data
	 * with the mimimun value of 10 kilobytes.
	 * </p>
	 *
	 * @param kilobytes The data transfer limit in kilobytes
	 * @throws IOException If the data transfer limit is invalid
	 */
	public void setKexTransferLimit(long kilobytes) throws IOException {
		transport.setKexTransferLimit(kilobytes);
	}

	/**
	 * <p/>
	 * Set's the send ignore flag to send random data packets.
	 * </p>
	 * <p/>
	 * <p/>
	 * If this flag is set to true, then the transport protocol will send
	 * additional SSH_MSG_IGNORE packets with random data.
	 * </p>
	 *
	 * @param sendIgnore true if you want to turn on random packet data,
	 *                   otherwise false
	 * @since 0.2.0
	 */
	public void setSendIgnore(boolean sendIgnore) {
		transport.setSendIgnore(sendIgnore);
	}

	/**
	 * <p/>
	 * Turn the default forwarding manager on/off.
	 * </p>
	 * <p/>
	 * <p/>
	 * If this flag is set to false before connection, the client will not
	 * create a port forwarding manager. Use this to provide you own
	 * forwarding implementation.
	 * </p>
	 *
	 * @param useDefaultForwarding Set to false if you not wish to use the
	 *                             default forwarding manager.
	 * @since 0.2.0
	 */
	public void setUseDefaultForwarding(boolean useDefaultForwarding) {
		this.useDefaultForwarding = useDefaultForwarding;
	}

	/**
	 * <p/>
	 * Disconnect the client.
	 * </p>
	 *
	 * @since 0.2.0
	 */
	public void disconnect() {
		if(connection != null) {
			connection.stop();
		}

		if(transport != null) {
			transport.disconnect("Terminating connection");
		}
	}

    public void interrupt() throws IOException {
        try {
            if(null == this.socket) {
                log.warn("Cannot interrupt; no socket");
                return;
            }
            socket.close();
        }
        finally {
            this.disconnect();
        }
    }

    /**
	 * <p/>
	 * Returns the number of bytes transmitted to the remote server.
	 * </p>
	 *
	 * @return The number of bytes transmitted
	 * @since 0.2.0
	 */
	public long getOutgoingByteCount() {
		return transport.getOutgoingByteCount();
	}

	/**
	 * <p/>
	 * Returns the number of bytes received from the remote server.
	 * </p>
	 *
	 * @return The number of bytes received
	 * @since 0.2.0
	 */
	public long getIncomingByteCount() {
		return transport.getIncomingByteCount();
	}

	/**
	 * <p/>
	 * Returns the number of active channels for this client.
	 * </p>
	 * <p/>
	 * <p/>
	 * This is the total count of sessions, port forwarding, sftp, scp and
	 * custom channels currently open.
	 * </p>
	 *
	 * @return The number of active channels
	 * @since 0.2.0
	 */
	public int getActiveChannelCount() {
		synchronized(activeChannels) {
			return activeChannels.size();
		}
	}

	/**
	 * <p/>
	 * Returns the list of active channels.
	 * </p>
	 *
	 * @return The list of active channels
	 * @since 0.2.0
	 */
	public List getActiveChannels() {
		synchronized(activeChannels) {
			return (List)activeChannels.clone();
		}
	}

	/**
	 * <p/>
	 * Returns true if there is an active session channel of the specified
	 * type.
	 * </p>
	 * <p/>
	 * <p/>
	 * When a session is created, it is assigned a default type. For instance,
	 * when a session is created it as a type of "uninitialized"; however when
	 * a shell is started on the session, the type is set to "shell". This
	 * also occurs for commands where the type is set to the command which is
	 * executed and subsystems where the type is set to the subsystem name.
	 * This allows each session to be saved in the active session channel's
	 * list and recalled later. It is also possible to set the session
	 * channel's type using the setSessionType method of the
	 * <code>SessionChannelClient</code> class.
	 * </p>
	 * <blockquote><pre>
	 * if(ssh.hasActiveSession("shell")) {
	 *      SessionChannelClient session =
	 *           ssh.getActiveSession("shell");
	 * }
	 * </pre></blockquote>
	 *
	 * @param type The string specifying the channel type
	 * @return true if an active session channel exists, otherwise false
	 * @since 0.2.0
	 */
	public boolean hasActiveSession(String type) {
		Iterator it = activeChannels.iterator();
		Object obj;

		while(it.hasNext()) {
			obj = it.next();

			if(obj instanceof SessionChannelClient) {
				if(((SessionChannelClient)obj).getSessionType().equals(type)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * <p/>
	 * Returns the active session channel of the given type.
	 * </p>
	 *
	 * @param type The type fo session channel
	 * @return The session channel instance
	 * @throws IOException If the session type does not exist
	 * @since 0.2.0
	 */
	public SessionChannelClient getActiveSession(String type)
	    throws IOException {
		Iterator it = activeChannels.iterator();
		Object obj;

		while(it.hasNext()) {
			obj = it.next();

			if(obj instanceof SessionChannelClient) {
				if(((SessionChannelClient)obj).getSessionType().equals(type)) {
					return (SessionChannelClient)obj;
				}
			}
		}

		throw new IOException("There are no active "+type+" sessions");
	}

	/**
	 * Determine whether the channel supplied is an active channel
	 *
	 * @param channel
	 * @return
	 */
	public boolean isActiveChannel(Channel channel) {
		return activeChannels.contains(channel);
	}

	/**
	 * <p/>
	 * Open's a session channel on the remote server.
	 * </p>
	 * <p/>
	 * <p/>
	 * A session channel may be used to start the user's shell, execute a
	 * command or start a subsystem such as SFTP.
	 * </p>
	 *
	 * @return An new session channel
	 * @throws IOException If authentication has not been completed, the
	 *                     server refuses to open the channel or a general IO error
	 *                     occurs
	 * @see com.sshtools.j2ssh.session.SessionChannelClient
	 * @since 0.2.0
	 */
	public SessionChannelClient openSessionChannel() throws IOException {
		return openSessionChannel(null);
	}

	/**
	 * <p/>
	 * Open's a session channel on the remote server.
	 * </p>
	 * <p/>
	 * <p/>
	 * A session channel may be used to start the user's shell, execute a
	 * command or start a subsystem such as SFTP.
	 * </p>
	 *
	 * @param eventListener an event listner interface to add to the channel
	 * @return
	 * @throws IOException
	 * @throws SshException
	 */
	public SessionChannelClient openSessionChannel(ChannelEventListener eventListener) throws IOException {
		if(authenticationState != AuthenticationProtocolState.COMPLETE) {
			throw new SshException("Authentication has not been completed!");
		}

		SessionChannelClient session = new SessionChannelClient();
		session.addEventListener(activeChannelListener);

		if(!connection.openChannel(session, eventListener)) {
			throw new SshException("The server refused to open a session");
		}

		return session;
	}

	/**
	 * <p>
	 * Open an SFTP client for file transfer operations.
	 * </p>
	 * <blockquote><pre>
	 * SftpClient sftp = ssh.openSftpClient();
	 * sftp.cd("foo");
	 * sftp.put("somefile.txt");
	 * sftp.quit();
	 * </pre></blockquote>
	 *
	 * @return Returns an initialized SFTP client
	 *
	 * @exception IOException If an IO error occurs during the operation
	 *
	 * @since 0.2.0
	 */
//    public SftpClient openSftpClient() throws IOException {
//        return openSftpClient(null);
//    }

	/**
	 * <p>
	 * Open an SFTP client for file transfer operations. Adds the supplied
	 * event listener to the underlying channel.
	 * </p>
	 *
	 * @param eventListener
	 *
	 * @return
	 *
	 * @throws IOException
	 */
//    public SftpClient openSftpClient(ChannelEventListener eventListener)
//        throws IOException {
//        SftpClient sftp = new SftpClient(this, eventListener);
//        activeSftpClients.add(sftp);
//
//        return sftp;
//    }

	/**
	 * Determine if there are existing sftp clients open
	 *
	 * @return
	 */
//    public boolean hasActiveSftpClient() {
//        synchronized (activeSftpClients) {
//            return activeSftpClients.size() > 0;
//        }
//    }

	/**
	 * Get an active sftp client
	 *
	 * @return
	 *
	 * @throws IOException
	 * @throws SshException
	 */
//    public SftpClient getActiveSftpClient() throws IOException {
//        synchronized (activeSftpClients) {
//            if (activeSftpClients.size() > 0) {
//                return (SftpClient) activeSftpClients.get(0);
//            } else {
//                throw new SshException("There are no active SFTP clients");
//            }
//        }
//    }

	/**
	 * <p/>
	 * Open an SCP client for file transfer operations where SFTP is not
	 * supported.
	 * </p>
	 * <p/>
	 * <p/>
	 * Sets the local working directory to the user's home directory
	 * </p>
	 * <blockquote><pre>
	 * ScpClient scp = ssh.openScpClient();
	 * scp.put("somefile.txt");
	 * </pre></blockquote>
	 *
	 * @return An initialized SCP client
	 * @throws IOException If an IO error occurs during the operation
	 * @see ScpClient
	 * @since 0.2.0
	 */
	public ScpClient openScpClient() throws IOException {
		return new ScpClient(new File(System.getProperty("user.home")), this,
		    false, activeChannelListener);
	}

	/**
	 * <p/>
	 * Open an SCP client for file transfer operations where SFTP is not
	 * supported.
	 * </p>
	 * <p/>
	 * <p/>
	 * This method sets a local current working directory.
	 * </p>
	 * <blockquote><pre>
	 * ScpClient scp = ssh.openScpClient("foo");
	 * scp.put("somefile.txt");
	 * </pre></blockquote>
	 *
	 * @param cwd The local directory as the base for all local files
	 * @return An intialized SCP client
	 * @throws IOException If an IO error occurs during the operation
	 * @since 0.2.0
	 */
	public ScpClient openScpClient(File cwd) throws IOException {
		return new ScpClient(cwd, this, false, activeChannelListener);
	}

	/**
	 * <p/>
	 * Open's an Sftp channel.
	 * </p>
	 * <p/>
	 * <p/>
	 * Use this sftp channel if you require a lower level api into the SFTP
	 * protocol.
	 * </p>
	 *
	 * @return an initialized sftp subsystem instance
	 * @throws IOException if an IO error occurs or the channel cannot be
	 *                     opened
	 * @since 0.2.0
	 */
	public SftpSubsystemClient openSftpChannel(String encoding) throws IOException {
		return openSftpChannel(null, encoding);
	}

	/**
	 * Open an SftpSubsystemChannel. For advanced use only
	 *
	 * @param eventListener
	 * @return
	 * @throws IOException
	 * @throws SshException
	 */
	public SftpSubsystemClient openSftpChannel(ChannelEventListener eventListener, String encoding)
            throws IOException {
		this.openSessionChannel(eventListener);
		SftpSubsystemClient sftp = new SftpSubsystemClient(encoding);

		if(!openChannel(sftp)) {
			throw new SshException("The SFTP subsystem failed to start");
		}

		// Initialize SFTP
		if(!sftp.initialize()) {
			throw new SshException("The SFTP Subsystem could not be initialized");
		}

		return sftp;
	}

	/**
	 * <p/>
	 * Open's a channel.
	 * </p>
	 * <p/>
	 * <p/>
	 * Call this method to open a custom channel. This method is used by all
	 * other channel opening methods. For example the openSessionChannel
	 * method could be implemented as:<br>
	 * <blockquote><pre>
	 * SessionChannelClient session =
	 *                 new SessionChannelClient();
	 * if(ssh.openChannel(session)) {
	 *    // Channel is now open
	 * }
	 * </pre></blockquote>
	 * </p>
	 *
	 * @param channel
	 * @return true if the channel was opened, otherwise false
	 * @throws IOException  if an IO error occurs
	 * @throws SshException
	 * @since 0.2.0
	 */
	public boolean openChannel(Channel channel) throws IOException {
		if(authenticationState != AuthenticationProtocolState.COMPLETE) {
			throw new SshException("Authentication has not been completed!");
		}

		// Open the channel providing our channel listener so we can track
		return connection.openChannel(channel, activeChannelListener);
	}

	/**
	 * <p/>
	 * Instructs the underlying connection protocol to allow channels of the
	 * given type to be opened by the server.
	 * </p>
	 * <p/>
	 * <p/>
	 * The client does not allow channels to be opened by default. Call this
	 * method to allow the server to open channels by providing a
	 * <code>ChannelFactory</code> implementation to create instances upon
	 * request.
	 * </p>
	 *
	 * @param channelName The channel type name
	 * @param cf          The factory implementation that will create instances of the
	 *                    channel when a channel open request is recieved.
	 * @throws IOException if an IO error occurs
	 * @since 0.2.0
	 */
	public void allowChannelOpen(String channelName, ChannelFactory cf)
	    throws IOException {
		connection.addChannelFactory(channelName, cf);
	}

	/**
	 * <p/>
	 * Stops the specified channel type from being opended.
	 * </p>
	 *
	 * @param channelName The channel type name
	 * @throws IOException if an IO error occurs
	 * @since 0.2.1
	 */
	public void denyChannelOpen(String channelName) throws IOException {
		connection.removeChannelFactory(channelName);
	}

	/**
	 * <p/>
	 * Send a global request to the server.
	 * </p>
	 * <p/>
	 * <p/>
	 * The SSH specification provides a global request mechanism which is used
	 * for starting/stopping remote forwarding. This is a general mechanism
	 * which can be used for other purposes if the server supports the global
	 * requests.
	 * </p>
	 *
	 * @param requestName The name of the global request
	 * @param wantReply   true if the server should send an explict reply
	 * @param requestData the global request data
	 * @return true if the global request succeeded or wantReply==false,
	 *         otherwise false
	 * @throws IOException if an IO error occurs
	 * @since 0.2.0
	 */
	public byte[] sendGlobalRequest(String requestName, boolean wantReply,
	                                byte[] requestData) throws IOException {
		return connection.sendGlobalRequest(requestName, wantReply, requestData);
	}

	/**
	 * <p/>
	 * Implements the <code>ChannelEventListener</code> interface to provide
	 * real time tracking of active channels.
	 * </p>
	 */
	class ActiveChannelEventListener extends ChannelEventAdapter {
		/**
		 * <p/>
		 * Adds the channel to the active channel list.
		 * </p>
		 *
		 * @param channel The channel being opened
		 */
		public void onChannelOpen(Channel channel) {
			synchronized(activeChannels) {
				activeChannels.add(channel);
			}
		}

		/**
		 * <p/>
		 * Removes the closed channel from the clients active channels list.
		 * </p>
		 *
		 * @param channel The channle being closed
		 */
		public void onChannelClose(Channel channel) {
			synchronized(activeChannels) {
				activeChannels.remove(channel);
			}
		}
	}
}
