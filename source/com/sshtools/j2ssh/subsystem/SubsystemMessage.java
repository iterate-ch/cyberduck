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
package com.sshtools.j2ssh.subsystem;

import java.io.IOException;

import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;
/**
 *  Abstract Subsystem message
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SubsystemMessage.java,v 1.5 2002/12/10 00:07:30 martianx Exp
 *      $
 */
public abstract class SubsystemMessage {
    private int type;


    /**
     *  Constructor for the message
     *
     *@param  type  the subsystem message id number
     */
    public SubsystemMessage(int type) {
        this.type = type;
    }


    /**
     *  Implement this to return the message name; this is used for debuging
     *  purposes only
     *
     *@return    the message name for example the SFTP message "SSH_FXP_INIT"
     */
    public abstract String getMessageName();


    /**
     *  Returns the message id
     *
     *@return    the message id number
     */
    public int getMessageType() {
        return type;
    }


    /**
     *  Implement this method to output the message to a byte array
     *
     *@param  baw                       the byte array being written
     *@throws  InvalidMessageException  if the message is invalid
     *@throws  IOException              if the data cannot be written
     */
    public abstract void constructByteArray(ByteArrayWriter baw)
             throws InvalidMessageException,
            IOException;


    /**
     *  Implement this message to construct the message from a byte array
     *
     *@param  bar                       the byte array being read
     *@throws  InvalidMessageException  if the message is invalid
     *@throws  IOException              if the data cannot be read
     */
    public abstract void constructMessage(ByteArrayReader bar)
             throws InvalidMessageException, IOException;


    /**
     *  Constructs the message from a byte array
     *
     *@param  data                      the message data
     *@throws  InvalidMessageException  if the message is invalid
     */
    public void fromByteArray(byte data[])
             throws InvalidMessageException {
        try {
            ByteArrayReader bar = new ByteArrayReader(data);
            if (bar.available() > 0) {
                type = bar.read();
                constructMessage(bar);
            } else {
                throw new InvalidMessageException("Not enough message data to complete the message");
            }
        } catch (IOException ioe) {
            throw new InvalidMessageException("The message data cannot be read!");
        }
    }


    /**
     *  Constructs a byte array containing the message data
     *
     *@return                           the message as a byte array
     *@throws  InvalidMessageException  if the message is invalid
     */
    public byte[] toByteArray()
             throws InvalidMessageException {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();
            baw.write(type);
            constructByteArray(baw);
            return baw.toByteArray();
        } catch (IOException ioe) {
            throw new InvalidMessageException("The message data cannot be written!");
        }
    }
}
