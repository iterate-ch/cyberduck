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

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  This class implements the SSH_MSG_USERAUTH_BANNER message
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgUserAuthBanner.java,v 1.9 2002/12/10 11:20:35 martianx
 *      Exp $
 */
public class SshMsgUserAuthBanner
         extends SshMessage {
    /**
     *  The message id
     */
    public final static int SSH_MSG_USERAUTH_BANNER = 53;
    private String banner;
    private String languageTag;


    /**
     *  Constructor for the message
     */
    public SshMsgUserAuthBanner() {
        super(SSH_MSG_USERAUTH_BANNER);
    }


    /**
     *  Creates a new SshMsgUserAuthBanner object.
     *
     *@param  banner  The user authentication banner
     */
    public SshMsgUserAuthBanner(String banner) {
        super(SSH_MSG_USERAUTH_BANNER);
        this.banner = banner;
        this.languageTag = "";
    }


    /**
     *  Gets the banner text.
     *
     *@return    The user authentication banner
     */
    public String getBanner() {
        return banner;
    }


    /**
     *  Gets the language tag.
     *
     *@return    The language tag
     */
    public String getLanguageTag() {
        return languageTag;
    }


    /**
     *  Gets the message name for debugging.
     *
     *@return    The message name
     */
    public String getMessageName() {
        return "SSH_MSG_USERAUTH_BANNER";
    }


    /**
     *  Constructs a byye array containing the message data.
     *
     *@param  baw                       The byte array being written to.
     *@throws  InvalidMessageException  if the message data cannot be written
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeString(banner);
            baw.writeString(languageTag);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error writing the message data");
        }
    }


    /**
     *  Constructs the message from data received.
     *
     *@param  bar                       The byte array containing the message
     *@throws  InvalidMessageException  if the message data cannot be read
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            banner = bar.readString();
            languageTag = bar.readString();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error reading the message data");
        }
    }
}
