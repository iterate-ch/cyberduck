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
package com.sshtools.j2ssh.authentication;
import com.sshtools.j2ssh.util.State;

/**
 *  This class implements a state object for the Authentication Protocol.<br>
 *  The state represents either the uninitialized or reading state prior to an
 *  authentication attempt or current state of attempted authentication.<br>
 *
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: AuthenticationProtocolState.java,v 1.7 2002/12/09 22:51:23
 *      martianx Exp $
 */
public class AuthenticationProtocolState
         extends State {
    /**
     *  The uninitialized state.
     */
    public final static int UNINITIALIZED = 0;

    /**
     *  The protocol is ready to accept/perform authentication.
     */
    public final static int READY = 1;

    /**
     *  The previous authentication attempt failed.
     */
    public final static int FAILED = 2;

    /**
     *  The previous authentication attemp succeeded but an additional
     *  authentication is required.
     */
    public final static int PARTIAL = 3;

    /**
     *  The previous authentication attempt completed all required
     *  authentications.
     */
    public final static int COMPLETE = 4;


    /**
     *  Constructor for the state object.
     */
    public AuthenticationProtocolState() {
        super(UNINITIALIZED);
    }


    /**
     *  Determines if the state is valid for this state object
     *
     *@param  state  The state to validate
     *@return        <tt>true</tt> if the state is valid otherwise <tt>false
     *      </tt>
     */
    public boolean isValidState(int state) {
        return ((state == UNINITIALIZED) || (state == READY) || (state == FAILED)
                || (state == PARTIAL) || (state == COMPLETE));
    }
}
