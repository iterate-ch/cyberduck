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
 *  Implements the disconnection Message.<br>
 *  <br>
 *  byte SSH_MSG_DISCONNECT<br>
 *  uint32 reason code<br>
 *  string description [RFC2279]<br>
 *  string language tag [RFC1766]<br>
 *  <br>
 *  <br>
 *  This message causes immediate termination of the connection. All
 *  implementations MUST be able to process this message; they SHOULD be able to
 *  send this message.<br>
 *  <br>
 *  The sender MUST NOT send or receive any data after this message, and the
 *  recipient MUST NOT accept any data after receiving this message. The
 *  description field gives a more specific explanation in a human- readable
 *  form. The error code gives the reason in a more machine- readable format
 *  (suitable for localization), and can have the following values:<br>
 *  <br>
 *  #define SSH_DISCONNECT_HOST_NOT_ALLOWED_TO_CONNECT 1<br>
 *  #define SSH_DISCONNECT_PROTOCOL_ERROR 2<br>
 *  #define SSH_DISCONNECT_KEY_EXCHANGE_FAILED 3<br>
 *  #define SSH_DISCONNECT_RESERVED 4<br>
 *  #define SSH_DISCONNECT_MAC_ERROR 5<br>
 *  #define SSH_DISCONNECT_COMPRESSION_ERROR 6<br>
 *  #define SSH_DISCONNECT_SERVICE_NOT_AVAILABLE 7<br>
 *  #define SSH_DISCONNECT_PROTOCOL_VERSION_NOT_SUPPORTED 8<br>
 *  #define SSH_DISCONNECT_HOST_KEY_NOT_VERIFIABLE 9<br>
 *  #define SSH_DISCONNECT_CONNECTION_LOST 10<br>
 *  #define SSH_DISCONNECT_BY_APPLICATION 11<br>
 *  #define SSH_DISCONNECT_TOO_MANY_CONNECTIONS 12<br>
 *  #define SSH_DISCONNECT_AUTH_CANCELLED_BY_USER 13<br>
 *  #define SSH_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE 14<br>
 *  #define SSH_DISCONNECT_ILLEGAL_USER_NAME 15<br>
 *  <br>
 *  If the description string is displayed, control character filtering
 *  discussed in [SSH-ARCH] should be used to avoid attacks by sending terminal
 *  control characters.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshMsgDisconnect.java,v 1.5 2002/12/10 00:07:32 martianx Exp
 *      $
 */
class SshMsgDisconnect
         extends SshMessage {
    /**
     *  The message id of this message
     */
    protected final static int SSH_MSG_DISCONNECT = 1;

    /**
     *  Host not allowed reason code
     */
    public final static int HOST_NOT_ALLOWED = 1;

    /**
     *  Protocol error reason code
     */
    public final static int PROTOCOL_ERROR = 2;

    /**
     *  Key exchange failed reason code
     */
    public final static int KEY_EXCHANGE_FAILED = 3;

    /**
     *  Reserved reason code
     */
    public final static int RESERVED = 4;

    /**
     *  Mac error reason code
     */
    public final static int MAC_ERROR = 5;

    /**
     *  Compression error reason code
     */
    public final static int COMPRESSION_ERROR = 6;

    /**
     *  Service not available reason code
     */
    public final static int SERVICE_NOT_AVAILABLE = 7;

    /**
     *  Protocol version not supported reason code
     */
    public final static int PROTOCOL_VERSION_NOT_SUPPORTED = 8;

    /**
     *  Host key not verifiable reason code
     */
    public final static int HOST_KEY_NOT_VERIFIABLE = 9;

    /**
     *  Connection lost reason code
     */
    public final static int CONNECTION_LOST = 10;

    /**
     *  The application disconnected reason code
     */
    public final static int BY_APPLICATION = 11;

    /**
     *  Too many connections reason code
     */
    public final static int TOO_MANY_CONNECTIONS = 12;

    /**
     *  Authentication cancelled by user reason code
     */
    public final static int AUTH_CANCELLED_BY_USER = 13;

    /**
     *  No more authentication methods available reason code
     */
    public final static int NO_MORE_AUTH_METHODS_AVAILABLE = 14;

    /**
     *  The user is illegal reason code
     */
    public final static int ILLEGAL_USER_NAME = 15;

    // The readble version of the disconneciton reason
    private String desc;

    // The language tag
    private String langTag;

    // Holds the reason for disconnection
    private int reasonCode;


    /**
     *  Constructs the disconnect message for sending.
     *
     *@param  reasonCode  The reason code for disconnection
     *@param  desc        A readable version of the disconnection reason
     *@param  langTag     The language tag for the description
     */
    public SshMsgDisconnect(int reasonCode, String desc, String langTag) {
        super(SSH_MSG_DISCONNECT);

        // Store the message values
        this.reasonCode = reasonCode;
        this.desc = desc;
        this.langTag = langTag;
    }


    /**
     *  Default constructor
     */
    public SshMsgDisconnect() {
        super(SSH_MSG_DISCONNECT);
    }


    /**
     *  Gets the description attribute of message
     *
     *@return    The description
     */
    public String getDescription() {
        return desc;
    }


    /**
     *  Gets the language tag attribute of the message
     *
     *@return    The language tag
     */
    public String getLanguageTag() {
        return langTag;
    }


    /**
     *  Gets the message name for debugging
     *
     *@return    "SSH_MSG_DISCONNECT"
     */
    public String getMessageName() {
        return "SSH_MSG_DISCONNECT";
    }


    /**
     *  Gets the reason code attribute of the message
     *
     *@return    The reason code
     */
    public int getReasonCode() {
        return reasonCode;
    }


    /**
     *  Abstract method implementation to construct a byte array containing the
     *  message data.
     *
     *@param  baw                          The byte array storing the message
     *      data.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException {
        try {
            baw.writeInt(reasonCode);
            baw.writeString(desc);
            baw.writeString(langTag);
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error writing message data: "
                    + ioe.getMessage());
        }
    }


    /**
     *  Abstract method implementation to construct the message from a byte
     *  array.
     *
     *@param  bar                          The message data received.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException {
        try {
            // Save the values
            reasonCode = (int) bar.readInt();
            desc = bar.readString();
            langTag = bar.readString();
        } catch (IOException ioe) {
            throw new InvalidMessageException("Error reading message data: "
                    + ioe.getMessage());
        }
    }
}
