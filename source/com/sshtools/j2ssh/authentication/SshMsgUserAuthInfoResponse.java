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

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshMsgUserAuthInfoResponse extends SshMessage {
    /**  */
    public static final int SSH_MSG_USERAUTH_INFO_RESPONSE = 61;
    private KBIPrompt[] prompts;
    String[] responses;

    /**
     * Creates a new SshMsgUserAuthInfoResponse object.
     */
    public SshMsgUserAuthInfoResponse() {
        super(SSH_MSG_USERAUTH_INFO_RESPONSE);
    }

    /**
     * Creates a new SshMsgUserAuthInfoResponse object.
     *
     * @param prompts
     */
    public SshMsgUserAuthInfoResponse(KBIPrompt[] prompts) {
        super(SSH_MSG_USERAUTH_INFO_RESPONSE);

        if (prompts != null) {
            responses = new String[prompts.length];

            for (int i = 0; i < responses.length; i++) {
                responses[i] = prompts[i].getResponse();
            }
        }
    }

    /**
     * @return
     */
    public String getMessageName() {
        return "SSH_MSG_USERAUTH_INFO_RESPONSE";
    }

    /**
     * @return
     */
    public String[] getResponses() {
        return responses;
    }

    /**
     * @param baw
     * @throws com.sshtools.j2ssh.transport.InvalidMessageException
     *                                 DOCUMENT
     *                                 ME!
     * @throws InvalidMessageException
     */
    protected void constructByteArray(ByteArrayWriter baw)
            throws com.sshtools.j2ssh.transport.InvalidMessageException {
        try {
            if (responses == null) {
                baw.writeInt(0);
            }
            else {
                baw.writeInt(responses.length);

                for (int i = 0; i < responses.length; i++) {
                    baw.writeString(responses[i]);
                }
            }
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Failed to write message data");
        }
    }

    /**
     * @param bar
     * @throws com.sshtools.j2ssh.transport.InvalidMessageException
     *                                 DOCUMENT
     *                                 ME!
     * @throws InvalidMessageException
     */
    protected void constructMessage(ByteArrayReader bar)
            throws com.sshtools.j2ssh.transport.InvalidMessageException {
        try {
            int num = (int)bar.readInt();

            if (num > 0) {
                responses = new String[num];

                for (int i = 0; i < responses.length; i++) {
                    responses[i] = bar.readString();
                }
            }
        }
        catch (IOException ioe) {
            throw new InvalidMessageException("Failed to read message data");
        }
    }
}
