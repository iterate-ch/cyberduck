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
package com.sshtools.j2ssh.connection;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author $author$
 * @version $Revision$
 */
public abstract class BindingChannel extends Channel {
    private Log log = LogFactory.getLog(BindingChannel.class);

    /**  */
    protected BindingChannel boundChannel;

    /**  */
    protected Vector messages = new Vector();

    /**
     * @return
     */
    public boolean isBound() {
        return boundChannel != null;
    }

    /**
     * @param boundChannel
     * @throws IOException
     */
    public void bindChannel(BindingChannel boundChannel)
            throws IOException {
        if (boundChannel == null) {
            throw new IOException("[" + getName() +
                    "] Bound channel cannot be null");
        }

        if (isBound()) {
            throw new IOException("[" + getName() +
                    "] This channel is already bound to another channel [" +
                    boundChannel.getName() + "]");
        }

        this.boundChannel = boundChannel;

        if (!boundChannel.isBound()) {
            boundChannel.bindChannel(this);

            synchronized (messages) {
                if (boundChannel.isOpen() && (messages.size() > 0)) {
                    sendOutstandingMessages();
                }
            }
        }
        else {
            if (!boundChannel.boundChannel.equals(this)) {
                throw new IOException("[" + getName() +
                        "] Channel is already bound to an another channel [" +
                        boundChannel.boundChannel.getName() + "]");
            }
        }
    }

    private void sendOutstandingMessages() throws IOException {
        // Send the outstanding messages
        if (boundChannel == null) {
            return;
        }

        synchronized (messages) {
            Iterator it = messages.iterator();

            while (it.hasNext()) {
                Object obj = it.next();

                if (obj instanceof SshMsgChannelData) {
                    boundChannel.sendChannelData(((SshMsgChannelData) obj).getChannelData());
                }
                else if (obj instanceof SshMsgChannelExtendedData) {
                    boundChannel.sendChannelExtData(((SshMsgChannelExtendedData) obj).getDataTypeCode(),
                            ((SshMsgChannelExtendedData) obj).getChannelData());
                }
                else {
                    throw new IOException("[" + getName() +
                            "] Invalid message type in pre bound message list!");
                }
            }

            messages.clear();
        }
    }

    /**
     * @param msg
     * @throws java.io.IOException
     */
    protected void onChannelExtData(SshMsgChannelExtendedData msg)
            throws java.io.IOException {
        synchronized (messages) {
            if (boundChannel != null) {
                if (boundChannel.isOpen()) {
                    boundChannel.sendChannelExtData(msg.getDataTypeCode(),
                            msg.getChannelData());
                }
                else {
                    messages.add(msg);
                }
            }
        }
    }

    /**
     * @param msg
     * @throws java.io.IOException
     */
    protected void onChannelData(SshMsgChannelData msg)
            throws java.io.IOException {
        synchronized (messages) {
            if (boundChannel != null) {
                if (boundChannel.isOpen()) {
                    boundChannel.sendChannelData(msg.getChannelData());
                }
                else {
                    messages.add(msg);
                }
            }
        }
    }

    /*public void setLocalEOF() throws IOException {
       synchronized(state) {
         super.setLocalEOF();
         if (!boundChannel.isRemoteEOF()) {
      log.info("onLocalEOF [" + getName() + "] is setting " + boundChannel.getName() + " to EOF");
      boundChannel.setRemoteEOF();
      //boundChannel.setLocalEOF();
         }
       }
     }*/
    protected void setRemoteEOF() throws IOException {
        synchronized (state) {
            super.setRemoteEOF();

            if (!boundChannel.isLocalEOF()) {
                log.info("onRemoteEOF [" + getName() + "] is setting " +
                        boundChannel.getName() + " to EOF");
                boundChannel.setLocalEOF();

                //boundChannel.setRemoteEOF();
            }
        }
    }

    /**
     * @throws java.io.IOException
     */
    protected void onChannelEOF() throws java.io.IOException {
    }

    /**
     * @throws java.io.IOException
     */
    protected void onChannelClose() throws java.io.IOException {
        /*synchronized(state) {
             if (boundChannel != null) {
               if (boundChannel.isOpen())
                 boundChannel.close();
             }
         }*/
    }

    /**
     * @throws java.io.IOException
     */
    protected void onChannelOpen() throws java.io.IOException {
        synchronized (messages) {
            if (boundChannel != null) {
                if (boundChannel.isOpen() && (messages.size() > 0)) {
                    sendOutstandingMessages();
                }
            }
        }
    }
}
