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
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  This abstract class should be subclassed to provide message implementations
 *  for the SSH2 protocol. The message object can be constructed either from a
 *  byte array or implementation specific types. The transport layer dynamically
 *  creates the class from the registered message details and calls <code>fromByteArray</code>
 *  to populate before routing the message to the registered message store.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public abstract class SshMessage {
    // The message Id of the message
    private int messageId;


    /**
     *  Constructs an SshMessage used to send. Subclasses classes will set their
     *  own properties typically passed to its own constructor.
     *
     *@param  messageId  The message Id of the message.
     */
    public SshMessage(int messageId) {
        // Save the message id
        this.messageId = messageId;
    }


    /**
     *  Gets the message id for this message
     *
     *@return    The message Id.
     */
    public final int getMessageId() {
        return messageId;
    }


    /**
     *  Gets the messageName attribute of the SshMessage object
     *
     *@return    The messageName value
     */
    public abstract String getMessageName();


    /**
     *  Converts the message into a byte array suitable for sending.
     *
     *@return                              A byte array containing the message
     *      data.
     *@exception  InvalidMessageException  if the message is invalid
     */
    public final byte[] toByteArray()
             throws InvalidMessageException {
        // Create a writer object to construct the array
        ByteArrayWriter baw = new ByteArrayWriter();

        // Write the message id
        baw.write(messageId);

        // Call the abstract method so subclasses classes can add their data
        constructByteArray(baw);

        // Return the array
        return baw.toByteArray();
    }


    /**
     *  Constructs an SshMessage from a byte array. This method calls the
     *  abstract method constructMessage to allow subclasses classes to perform
     *  message specific construction.
     *
     *@param  data                      The data being read.
     *@throws  InvalidMessageException  if the message is invalid
     */
    protected final void fromByteArray(ByteArrayReader data)
             throws InvalidMessageException {
        // Skip the first 5 bytes as this contains the packet length and payload
        // length fields
        data.skip(5);

        int id = data.read();

        if (id != messageId) {
            throw new InvalidMessageException("The message id "
                    + String.valueOf(id)
                    + " is not the same as the message implementation id "
                    + String.valueOf(messageId));
        }

        // Call abstract method for subclasses to extract the message specific data
        constructMessage(data);
    }

    public static Integer getMessageId(byte[] msgdata) {
      return new Integer(msgdata[5]);
    }


    /**
     *  Abstract method which is called to construct the byte array returned in
     *  a call to toByteArray.
     *
     *@param  baw                          The ByteArrayWriter instance
     *      constructing the array.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected abstract void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException;


    /**
     *  Abstract method which is called to construct the message from a byte
     *  array.
     *
     *@param  bar                          The ByteArrayReader containing the
     *      message data.
     *@exception  InvalidMessageException  if the message is invalid
     */
    protected abstract void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException;
}
