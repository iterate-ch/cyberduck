/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshMsgUserAuthFailure extends SshMessage {
    /**  */
    protected final static int SSH_MSG_USERAUTH_FAILURE = 51;
    private List auths;
    private boolean partialSuccess;

    /**
     * Creates a new SshMsgUserAuthFailure object.
     */
    public SshMsgUserAuthFailure() {
        super(SSH_MSG_USERAUTH_FAILURE);
    }

    /**
     * Creates a new SshMsgUserAuthFailure object.
     *
     * @param auths
     * @param partialSuccess
     * @throws InvalidMessageException
     */
    public SshMsgUserAuthFailure(String auths, boolean partialSuccess)
            throws InvalidMessageException {
        super(SSH_MSG_USERAUTH_FAILURE);
        loadListFromDelimString(auths);
        this.partialSuccess = partialSuccess;
    }

    /**
     * @return
     */
    public List getAvailableAuthentications() {
        return auths;
    }

    /**
     * @return
     */
    public String getMessageName() {
        return "SSH_MSG_USERAUTH_FAILURE";
    }

    /**
     * @return
     */
    public boolean getPartialSuccess() {
        return partialSuccess;
    }

    /**
     * @param baw
     * @throws InvalidMessageException
     */
    protected void constructByteArray(ByteArrayWriter baw)
            throws InvalidMessageException {
        try {
            String authMethods = null;
            Iterator it = auths.iterator();

            while (it.hasNext()) {
                authMethods = ((authMethods == null) ? "" : (authMethods + ",")) +
                        (String)it.next();
            }

            baw.writeString(authMethods);
            baw.write((partialSuccess ? 1 : 0));
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }

    /**
     * @param bar
     * @throws InvalidMessageException
     */
    protected void constructMessage(ByteArrayReader bar)
            throws InvalidMessageException {
        try {
            String auths = bar.readString();
            partialSuccess = ((bar.read() != 0) ? true : false);
            loadListFromDelimString(auths);
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Invalid message data");
        }
    }

    private void loadListFromDelimString(String list) {
        StringTokenizer tok = new StringTokenizer(list, ",");
        auths = new ArrayList();

        while (tok.hasMoreElements()) {
            auths.add(tok.nextElement());
        }
    }
}
