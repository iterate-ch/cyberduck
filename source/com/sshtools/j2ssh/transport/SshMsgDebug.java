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

import java.io.IOException;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  <p>
 *
 *  Implements the transport protocol debug message. </p> <p>
 *
 *  byte SSH_MSG_DEBUG </p> <p>
 *
 *  boolean always_display </p> <p>
 *
 *  string message [RFC2279] </p> <p>
 *
 *  string language tag [RFC1766] </p> <p>
 *
 *  All implementations MUST understand this message, but they are allowed to
 *  ignore it. This message is used to pass the other side information that may
 *  help debugging. If always_display is TRUE, the message SHOULD be displayed.
 *  Otherwise, it SHOULD NOT be displayed unless debugging information has been
 *  explicitly requested by the user. </p> <p>
 *
 *  The message doesn't need to contain a newline. It is, however, allowed to
 *  consist of multiple lines separated by CRLF (Carriage Return - Line Feed)
 *  pairs. </p> <p>
 *
 *  If the message string is displayed, terminal control character filtering
 *  discussed in [SSH-ARCH] should be used to avoid attacks by sending terminal
 *  control characters. </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
class SshMsgDebug
         extends SshMessage {
    /**
     *  The message id of this message
     */
    protected final static int SSH_MSG_DEBUG = 4;

    // Holds the language_tag value
    private String langTag;

    // Holds the message value
    private String message;

    // Holds the always_display value
    private boolean alwaysDisplay;


    /**
     *  Constructs the message ready for sending.
     *
     *@param  alwaysDisplay  Should the debug message be displayed?
     *@param  message        The message to be displayed.
     *@param  langTag        The language tag of the message.
     */
    public SshMsgDebug(boolean alwaysDisplay, String message, String langTag) {
        super(SSH_MSG_DEBUG);

        // Save the debug details
        this.alwaysDisplay = alwaysDisplay;
        this.message = message;
        this.langTag = langTag;
    }


    /**
     *  Default constructor.
     */
    public SshMsgDebug() {
        super(SSH_MSG_DEBUG);
    }


    /**
     *  Gets the display always attribute of the message.
     *
     *@return    <tt>true</tt> if the message should always be displayed
     *      otherwise <tt>false</tt>
     */
    public boolean getDisplayAlways() {
        return alwaysDisplay;
    }


    /**
     *  Gets the language tag attribute of the message.
     *
     *@return    The language tag.
     */
    public String getLanguageTag() {
        return langTag;
    }


    /**
     *  Gets the message attribute of the message.
     *
     *@return    The debug message.
     */
    public String getMessage() {
        return message;
    }


    /**
     *  Gets the messageName attribute of the SshMsgDebug object
     *
     *@return    "SSH_MSG_DEBUG"
     */
    public String getMessageName() {
        return "SSH_MSG_DEBUG";
    }


    /**
     *  Abstract method implementation to construct a byte array suitable for
     *  sending.
     *
     *@param  baw                          The byte array to store the message.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            // Write the data
            baw.write(alwaysDisplay ? 1 : 0);
            baw.writeString(message);
            baw.writeString(langTag);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error writing message data: "
                    + ioe.getMessage());
        }
    }


    /**
     *  Abstract method implementation to extract the message data from a byte
     *  array.
     *
     *@param  bar                          The byte array to extract the message
     *      from.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            // Extract the message information
            alwaysDisplay = (bar.read() == 0) ? false : true;
            message = bar.readString();
            langTag = bar.readString();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error reading message data: "
                    + ioe.getMessage());
        }
    }
}
