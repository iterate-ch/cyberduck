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

import com.sshtools.j2ssh.SshException;


/**
 * <p>
 * The transport layer throws this exception when an attempt is made to
 * unregister a message that has not been registered.
 * </p>
 *
 * <p>
 * CAUSES: <br>
 * Did you register it in the first place!<br>
 * Was it yours to unregister!
 * </p>
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class MessageNotRegisteredException
    extends SshException {
    /**
     * <p>
     * Constructs the exception.
     * </p>
     *
     * @param messageId The message id of the unregistered message
     */
    public MessageNotRegisteredException(Integer messageId) {
        super("Message Id " + messageId.toString()
              + " is not currently registered");
    }

    /**
     * Constructs the exception.
     *
     * @param messageId The message id of the unregistered message.
     * @param store The message store object that tried to unregister the
     *        message.
     */
    public MessageNotRegisteredException(Integer messageId,
                                         SshMessageStore store) {
        super("Message Id " + messageId.toString()
              + " is not registered to the message store specified");
    }
}
