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
package com.sshtools.j2ssh.transport;
import com.sshtools.j2ssh.util.State;

/**
 *  The transport protocol can only send specific transport messages whilst in
 *  key exchange. This can happen at any time if the remote or local side send
 *  another SSH_MSG_KEX_INIT. This state object allows the transport layer to
 *  maintain its state so that it can queue messages received from attached
 *  services whilst completing the key exchange.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: TransportProtocolState.java,v 1.8 2002/12/10 00:07:32
 *      martianx Exp $
 */
public class TransportProtocolState
         extends State {
    /**
     *  the transport protocol is uninitialized
     */
    public final static int UNINITIALIZED = 1;

    /**
     *  the transport protocol is connected and negotiating the protocol version
     */
    public final static int NEGOTIATING_PROTOCOL = 2;

    /**
     *  the transport protocol is performing key exchange
     */
    public final static int PERFORMING_KEYEXCHANGE = 3;

    /**
     *  the transport protocol is connected
     */
    public final static int CONNECTED = 4;

    /**
     *  the transport protocol is disconnected
     */
    public final static int DISCONNECTED = 5;


    /**
     *  The Constructor
     */
    public TransportProtocolState() {
        super(UNINITIALIZED);
    }


    /**
     *  Determines if the state is a valid TransportProtocolState
     *
     *@param  state  the state value
     *@return        <tt>true</tt> if the state is valid otherwise <tt>false
     *      </tt>
     */
    public boolean isValidState(int state) {
        return ((state == UNINITIALIZED) || (state == NEGOTIATING_PROTOCOL)
                || (state == PERFORMING_KEYEXCHANGE) || (state == CONNECTED)
                || (state == DISCONNECTED));
    }
}
