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
package com.sshtools.j2ssh.io;
import com.sshtools.j2ssh.util.State;

/**
 *  Implements a State object for the IOStreamConnector class
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: IOStreamConnectorState.java,v 1.2 2002/12/09 23:35:42
 *      martianx Exp $
 */
public class IOStreamConnectorState
         extends State {
    /**
     *  The stream is at Beginning Of File
     */
    public final static int BOF = 1;

    /**
     *  The stream is connected
     */
    public final static int CONNECTED = 2;

    /**
     *  The stream is at End Of File
     */
    public final static int EOF = 3;

    /**
     *  The stream is closed
     */
    public final static int CLOSED = 4;


    /**
     *  Creates a new IOStreamConnectorState object.
     */
    public IOStreamConnectorState() {
        super(BOF);
    }


    /**
     *  Validiates that the state is valid for this State object
     *
     *@param  state  the state to validate
     *@return        <tt>true</tt> if the state is valid otherwise <tt>false
     *      </tt>
     */
    public boolean isValidState(int state) {
        return ((state == BOF) || (state == CONNECTED) || (state == EOF)
                || (state == CLOSED));
    }
}
