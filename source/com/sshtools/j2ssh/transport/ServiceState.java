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
 *  When a service is requested the transport layer sends an
 *  SSH_MSG_SERVICE_REQUEST message to the remote computer. This state object is
 *  used to synchronize with the input stream thread so that the transport layer
 *  can synchronously return either a new instance of the requested service or
 *  deny the service request.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public class ServiceState
         extends State {
    /**
     *  the service is uninitialized
     */
    public final static int SERVICE_UNINITIALIZED = 1;

    /**
     *  The service has been started
     */
    public final static int SERVICE_STARTED = 2;

    /**
     *  The service has been stopped
     */
    public final static int SERVICE_STOPPED = 3;


    /**
     *  Constructor for the SshServiceState object, defaults the state to
     *  service requested.
     */
    public ServiceState() {
        super(SERVICE_UNINITIALIZED);
    }


    /**
     *  Determines whether the state is valid
     *
     *@param  state  the state to validate
     *@return        <tt>true</tt> if the state is valid otherwise <tt>false
     *      </tt>
     */
    public boolean isValidState(int state) {
        return ((state == SERVICE_UNINITIALIZED) || (state == SERVICE_STARTED)
                || (state == SERVICE_STOPPED));
    }
}
