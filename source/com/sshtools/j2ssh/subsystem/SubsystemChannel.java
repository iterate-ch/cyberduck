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
package com.sshtools.j2ssh.subsystem;

import com.sshtools.j2ssh.connection.*;
import com.sshtools.j2ssh.io.*;
import com.sshtools.j2ssh.transport.*;

import org.apache.commons.logging.*;

import java.io.*;


public abstract class SubsystemChannel extends Channel {
    private static Log log = LogFactory.getLog(SubsystemChannel.class);
    Integer exitCode = null;
    String name;
    protected SubsystemMessageStore messageStore;
    DynamicBuffer buffer = new DynamicBuffer();
    int nextMessageLength = -1;

    public SubsystemChannel(String name) {
        this.name = name;
        this.messageStore = new SubsystemMessageStore();
    }

    public SubsystemChannel(String name, SubsystemMessageStore messageStore) {
        this.name = name;
        this.messageStore = messageStore;
    }

    public String getChannelType() {
        return "session";
    }

    protected void sendMessage(SubsystemMessage msg)
        throws InvalidMessageException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Sending " + msg.getMessageName() + " subsystem message");
        }

        byte[] msgdata = msg.toByteArray();

        // Write the message length
        sendChannelData(ByteArrayWriter.encodeInt(msgdata.length));

        // Write the message data
        sendChannelData(msgdata);
    }

    protected void onChannelRequest(String requestType, boolean wantReply,
        byte[] requestData) throws java.io.IOException {
        log.debug("Channel Request received: " + requestType);

        if (requestType.equals("exit-status")) {
            exitCode = new Integer((int) ByteArrayReader.readInt(requestData, 0));
            log.debug("Exit code of " + exitCode.toString() + " received");
        } else if (requestType.equals("exit-signal")) {
            ByteArrayReader bar = new ByteArrayReader(requestData);
            String signal = bar.readString();
            boolean coredump = bar.read() != 0;
            String message = bar.readString();
            String language = bar.readString();
            log.debug("Exit signal " + signal + " received");
            log.debug("Signal message: " + message);
            log.debug("Core dumped: " + String.valueOf(coredump));

            /*if (signalListener != null) {
              signalListener.onExitSignal(signal, coredump, message);
                       }*/
        } else if (requestType.equals("xon-xoff")) {
            /*if (requestData.length >= 1) {
              localFlowControl = (requestData[0] != 0);
                       }*/
        } else if (requestType.equals("signal")) {
            String signal = ByteArrayReader.readString(requestData, 0);
            log.debug("Signal " + signal + " received");

            /*if (signalListener != null) {
              signalListener.onSignal(signal);
                       }*/
        } else {
            if (wantReply) {
                connection.sendChannelRequestFailure(this);
            }
        }
    }

    protected void onChannelExtData(SshMsgChannelExtendedData msg)
        throws java.io.IOException {
    }

    protected void onChannelData(SshMsgChannelData msg)
        throws java.io.IOException {
        // Write the data to a temporary buffer that may also contain data
        // that has not been processed
        buffer.getOutputStream().write(msg.getChannelData());

        int read;
        byte[] tmp = new byte[4];
        byte[] msgdata;

        // Now process any outstanding messages
        while (buffer.getInputStream().available() > 4) {
            if (nextMessageLength == -1) {
                read = 0;

                while ((read += buffer.getInputStream().read(tmp)) < 4) {
                    ;
                }

                nextMessageLength = (int) ByteArrayReader.readInt(tmp, 0);
            }

            if (buffer.getInputStream().available() >= nextMessageLength) {
                msgdata = new byte[nextMessageLength];
                buffer.getInputStream().read(msgdata);
                messageStore.addMessage(msgdata);
                nextMessageLength = -1;
            } else {
                break;
            }
        }
    }

    protected void onChannelEOF() throws java.io.IOException {
    }

    protected void onChannelClose() throws java.io.IOException {
    }

    public byte[] getChannelOpenData() {
        return null;
    }

    protected void onChannelOpen() throws java.io.IOException {
    }

    public boolean startSubsystem() throws IOException {
        log.info("Starting " + name + " subsystem");

        ByteArrayWriter baw = new ByteArrayWriter();
        baw.writeString(name);

        return connection.sendChannelRequest(this, "subsystem", true,
            baw.toByteArray());
    }

    public byte[] getChannelConfirmationData() {
        return null;
    }
}
