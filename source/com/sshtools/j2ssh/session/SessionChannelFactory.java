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
package com.sshtools.j2ssh.session;

import java.util.ArrayList;
import java.util.List;

import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelFactory;
import com.sshtools.j2ssh.connection.InvalidChannelException;


/**
 * This class implements a <code>ChannelFactory</code> for the creation of the
 * 'session' channel type. This factory is used by server implementations to
 * allow the opening of session channels by remote clients. The
 * <code>openChannel</code> method returns an uninitialized
 * <code>SessionChannelServer</code> channel instance.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SessionChannelFactory
    implements ChannelFactory {
    /**
     * Creates a new SessionChannelFactory object.
     */
    public SessionChannelFactory() {
    }

    /**
     * Gets the list of channel types this factory can create. This method
     * returns one type 'session'
     *
     * @return a list of channel type Strings
     */
    public List getChannelType() {
        List list = new ArrayList();
        list.add(SessionChannelServer.SESSION_CHANNEL_TYPE);

        return list;
    }

    /**
     * Creates an uninitialized <code>SessionChannelServer</code> channel
     * instance.
     *
     * @param channelType the channel type to create
     * @param requestData the channel request data
     *
     * @return an uninitialized channel ready for opening
     *
     * @throws InvalidChannelException if the channel cannot be created
     */
    public Channel createChannel(String channelType, byte requestData[])
                          throws InvalidChannelException {
        if (channelType.equals("session")) {
            return new SessionChannelServer();
        } else {
            throw new InvalidChannelException("Only session channels can be opened by this factory");
        }
    }
}
