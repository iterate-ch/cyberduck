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

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class SshMsgUserAuthPwdChangeReq extends SshMessage {
    /**  */
    public static final int SSH_MSG_USERAUTH_PWD_CHANGEREQ = 60;
    private String prompt;
    private String language;

    /**
     * Creates a new SshMsgUserAuthPwdChangeReq object.
     */
    public SshMsgUserAuthPwdChangeReq() {
        super(SSH_MSG_USERAUTH_PWD_CHANGEREQ);
    }

    /**
     * Creates a new SshMsgUserAuthPwdChangeReq object.
     *
     * @param prompt
     * @param language
     */
    public SshMsgUserAuthPwdChangeReq(String prompt, String language) {
        super(SSH_MSG_USERAUTH_PWD_CHANGEREQ);
        this.prompt = prompt;
        this.language = language;
    }

    /**
     *
     *
     * @return
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     *
     *
     * @return
     */
    public String getLanguage() {
        return language;
    }

    /**
     *
     *
     * @return
     */
    public String getMessageName() {
        return "SSH_MSG_USERAUTH_PWD_CHANGEREQ";
    }

    /**
     *
     *
     * @param baw
     *
     * @throws InvalidMessageException
     */
    protected void constructByteArray(ByteArrayWriter baw)
        throws InvalidMessageException {
        try {
            baw.writeString(prompt);
            baw.writeString(language);
        } catch (Exception ex) {
            throw new InvalidMessageException(ex.getMessage());
        }
    }

    /**
     *
     *
     * @param bar
     *
     * @throws InvalidMessageException
     */
    protected void constructMessage(ByteArrayReader bar)
        throws InvalidMessageException {
        try {
            prompt = bar.readString();
            language = bar.readString();
        } catch (Exception ex) {
            throw new InvalidMessageException(ex.getMessage());
        }
    }
}
