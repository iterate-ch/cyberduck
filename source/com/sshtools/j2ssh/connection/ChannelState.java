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
package com.sshtools.j2ssh.connection;
import com.sshtools.j2ssh.util.State;

/**
 *  This class implements a <code>State</code> object for a connection protocol
 *  channel
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public class ChannelState
         extends State {
    /**
     *  The channel is uninitialized
     */
    public final static int CHANNEL_UNINITIALIZED = 1;

    /**
     *  The channel is open
     */
    public final static int CHANNEL_OPEN = 2;

    /**
     *  The channel is closed
     */
    public final static int CHANNEL_CLOSED = 3;


    /**
     *  Creates a new ChannelState object.
     */
    public ChannelState() {
        super(CHANNEL_UNINITIALIZED);
    }


    /**
     *  Determines if the value is a valid state for this implementation
     *
     *@param  state  the state value
     *@return        <tt>true</tt> if the state is valid otherwise <tt>false
     *      </tt>
     */
    public boolean isValidState(int state) {
        return ((state == CHANNEL_UNINITIALIZED) || (state == CHANNEL_OPEN)
                || (state == CHANNEL_CLOSED));
    }
}
