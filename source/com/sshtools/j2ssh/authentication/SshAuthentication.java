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
package com.sshtools.j2ssh.authentication;

import java.awt.Component;

import java.util.Map;

import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.TransportProtocol;
import com.sshtools.j2ssh.transport.TransportProtocolException;

import java.io.IOException;

import java.util.Properties;


/**
 * This abstract class is the base for all SSH Authentication methods.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class SshAuthentication {
    /** The AuthenticationProtocol instance monitoring this authentication */
    protected AuthenticationProtocol authentication;

    /**
     * The TransportProtocol instance for sending/recieving method specific
     * messages
     */
    protected TransportProtocol transport;
    private String username;

    /**
     * Gets the authentication method name.
     *
     * @return The method name
     */
    public abstract String getMethodName();

    /**
     * The main authentication method; an implementation should authenticate by
     * using the messages detailed in the method specification. The
     * authentication protocol handles the success or failure.
     *
     * @param serviceToStart The name of the service to start after
     *        authentication
     *
     * @exception AuthenticationProtocolException if an error occurs during
     *            authentication
     * @exception TransportProtocolException if an error occurs in the
     *            Transport Protocol
     */
    public abstract void authenticate(String serviceToStart)
                               throws IOException;

    /**
     * Performs the authentication methods server side authentication
     *
     * @param msg The user authentication request
     * @param nativeSettings A Map of native setting name/value pairs
     *
     * @throws TransportProtocolException if an error occurs in the Transport
     *         Protocol
     * @throws AuthenticationProtocolException if an error occurs during
     *         authentication
     */
    public abstract void authenticate(SshMsgUserAuthRequest msg,
                                      Map nativeSettings)
                               throws IOException;

    /**
     * Initiates the authentication method with the parent authentication
     * protocol instance and the transport protocol for sending/recieving
     * method specific messages
     *
     * @param authentication The parent AuthenticationProtocol
     * @param transport The Transport Protocol used for sending/receiving
     */
    public void init(AuthenticationProtocol authentication,
                     TransportProtocol transport) {
        this.authentication = authentication;
        this.transport = transport;
    }

    /**
     * An implementation should display a modal dialog so that the user can
     * enter method specific information; this is only called on GUI
     * implementations.
     *
     * @param parent The parent component
     *
     * @return Returns true if authentication is ready
     */
    public abstract boolean showAuthenticationDialog(Component parent)
        throws IOException;

    /**
     * Sets the username for the authentication; this is called by the
     * framework to provide the authentication method with the username from
     * the connection properties. It is possible for the user to specify a
     * different username so an implementation should use this to default
     * only.
     *
     * @param username The user's name
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the username for this authentication instance
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    public abstract Properties getPersistableProperties();

    public abstract void setPersistableProperties(Properties properties);

    public abstract boolean canAuthenticate();

}
