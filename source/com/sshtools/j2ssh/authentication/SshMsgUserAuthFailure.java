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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  Implements the SSH_MSG_USERAUTH_FAILURE message
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgUserAuthFailure.java,v 1.6 2002/12/09 22:51:23 martianx
 *      Exp $
 */
public class SshMsgUserAuthFailure
         extends SshMessage {
    /**
     *  The message id
     */
    protected final static int SSH_MSG_USERAUTH_FAILURE = 51;
    private List auths;
    private boolean partialSuccess;


    /**
     *  Constructs the message.
     */
    public SshMsgUserAuthFailure() {
        super(SSH_MSG_USERAUTH_FAILURE);
    }


    /**
     *  Constructs the message.
     *
     *@param  auths                        The authentication methods available
     *@param  partialSuccess               True if a partial success
     *@exception  InvalidMessageException  if the message is invalid
     */
    public SshMsgUserAuthFailure(String auths, boolean partialSuccess)
             throws InvalidMessageException {
        super(SSH_MSG_USERAUTH_FAILURE);

        loadListFromDelimString(auths);

        this.partialSuccess = partialSuccess;
    }


    /**
     *  Gets the available Authentications methods
     *
     *@return    The list of available authentications
     */
    public List getAvailableAuthentications() {
        return auths;
    }


    /**
     *  Gets the message name for debugging.
     *
     *@return    The message name
     */
    public String getMessageName() {
        return "SSH_MSG_USERAUTH_FAILURE";
    }


    /**
     *  Gets the partial success flag.
     *
     *@return    The partial success value
     */
    public boolean getPartialSuccess() {
        return partialSuccess;
    }


    /**
     *  Constructs a byte array containing the message.
     *
     *@param  baw                       The byte array being written to
     *@throws  InvalidMessageException  if the message is invalid
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            String authMethods = null;
            Iterator it = auths.iterator();

            while (it.hasNext()) {
                authMethods = (authMethods==null? "" : authMethods + ",") + (String) it.next();
            }

            baw.writeString(authMethods);

            baw.write((partialSuccess ? 1 : 0));
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }


    /**
     *  Constructs the message from a byte array.
     *
     *@param  bar                          The data being read
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            String auths = bar.readString();
            partialSuccess = ((bar.read() != 0) ? true : false);

            loadListFromDelimString(auths);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }


    /**
     *  Loads the lists of available authentications from a comma delimited
     *  string.
     *
     *@param  list  A comma-delimited string of authentication methods
     */
    private void loadListFromDelimString(String list) {
        StringTokenizer tok = new StringTokenizer(list, ",");

        auths = new ArrayList();

        while (tok.hasMoreElements()) {
            auths.add(tok.nextElement());
        }
    }
}
