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
package com.sshtools.j2ssh.agent;

import java.io.IOException;
import java.net.InetAddress;

import com.sshtools.j2ssh.connection.InvalidChannelException;
import com.sshtools.j2ssh.connection.SocketChannel;
import com.sshtools.j2ssh.io.ByteArrayWriter;


/**
 * @author $author$
 * @version $Revision$
 */
public class AgentSocketChannel extends SocketChannel {
    /**  */
    public static final String AGENT_FORWARDING_CHANNEL = "auth-agent";

    //protected Socket socket = null;
    private boolean isForwarding;

    /**
     * Creates a new AgentSocketChannel object.
     *
     * @param isForwarding
     */
    public AgentSocketChannel(boolean isForwarding) {
        this.isForwarding = isForwarding;
    }

    /**
     * @return
     */
    public String getChannelType() {
        return AGENT_FORWARDING_CHANNEL;
    }

    /*public void bindSocket(Socket socket) throws IOException {
       this.socket = socket;
       if (state.getValue() == ChannelState.CHANNEL_OPEN) {
         bindInputStream(socket.getInputStream());
         bindOutputStream(socket.getOutputStream());
       }
     }*/
    protected void onChannelRequest(String requestType, boolean wantReply,
                                    byte[] requestData) throws java.io.IOException {
        if (wantReply) {
            connection.sendChannelRequestFailure(this);
        }
    }

    /**
     * @return
     */
    protected int getMaximumPacketSize() {
        return 32678;
    }

    /*protected void onChannelClose() throws java.io.IOException {
     }
     protected void onChannelEOF() throws IOException {
     }*/
    public byte[] getChannelOpenData() {
        return null;
    }

    /**
     * @return
     */
    protected int getMinimumWindowSpace() {
        return 1024;
    }

    /**
     * @throws com.sshtools.j2ssh.connection.InvalidChannelException
     *                                 DOCUMENT
     *                                 ME!
     * @throws InvalidChannelException
     */
    protected void onChannelOpen()
            throws com.sshtools.j2ssh.connection.InvalidChannelException {
        try {
            //if (socket != null) {
            if (isForwarding) {
                // Were forwarding so insert the forwarding notice before any other data
                SshAgentForwardingNotice msg = new SshAgentForwardingNotice(InetAddress.getLocalHost()
                        .getHostName(),
                        InetAddress.getLocalHost().getHostAddress(),
                        socket.getPort());
                ByteArrayWriter baw = new ByteArrayWriter();
                baw.writeBinaryString(msg.toByteArray());
                sendChannelData(baw.toByteArray());
            }

            super.onChannelOpen();

            // Now bind the socket to the channel
            //  bindInputStream(socket.getInputStream());
            //  bindOutputStream(socket.getOutputStream());
            //}
        }
        catch (IOException ex) {
            throw new InvalidChannelException(ex.getMessage());
        }
    }

    /**
     * @return
     */
    protected int getMaximumWindowSpace() {
        return 32768;
    }

    /**
     * @return
     */
    public byte[] getChannelConfirmationData() {
        return null;
    }
}
