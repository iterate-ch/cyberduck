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
package com.sshtools.j2ssh.transport;

import java.io.IOException;

/**
 * <p>
 * This interface provides external access to messaging services supplied by
 * the SSH API.
 * </p>
 *
 * <p>
 * When a party wants to receive an incoming message it simply registers its
 * interest with the transport protocol through registerMessage(). Messages
 * are then routed to the synchronized message store object supplied during
 * registration where any waiting threads are notified. This framework for
 * message routing is implemented by the abstract Service class.
 * </p>
 *
 * <p>
 * Messages should be unregistered by services especially if the service is
 * temporary, such as Authenticaiton services. Messages are sent through the
 * transport layer by using sendMessage(). This requires an additional
 * identification object (typically passed as 'this') so that the transport
 * layer can filter messages whilst in key exchange. (only transport protocol
 * messages are valid during the key exchange process.
 * </p>
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public interface TransportProtocol {
    /**
     * Closes the connection
     *
     * @param description A description of why the disconnect is occuring
     */
    public void disconnect(String description);

    /**
     * Registers a message id with the transport layer
     *
     * @param messageId The id of the message being registered
     * @param implementor The class of the message implementation.
     * @param store The message store to route the object to
     *
     * @exception MessageAlreadyRegisteredException if the message id is
     *            already registered
     */
    public void registerMessage(Integer messageId, Class implementor,
                                SshMessageStore store)
                         throws MessageAlreadyRegisteredException;

    /**
     * Sends an Message
     *
     * @param ms The message to send
     * @param sender The sender of the message
     *
     * @exception TransportProtocolException if an error occurs
     */
    public void sendMessage(SshMessage ms, Object sender)
                     throws IOException;

    /**
     * Unregisters a message from the transport layer
     *
     * @param messageId The id of the message to unregister
     * @param store The message store currently receiving notification
     *
     * @exception MessageNotRegisteredException if the message is not
     *            registered
     */
    public void unregisterMessage(Integer messageId, SshMessageStore store)
                           throws MessageNotRegisteredException;
}
