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
package com.sshtools.j2ssh.forwarding;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.connection.IOChannel;


/**
 * @author $author$
 * @version $Revision$
 */
public class ForwardingIOChannel extends IOChannel implements ForwardingChannel {
    private static Log log = LogFactory.getLog(ForwardingIOChannel.class);
    private ForwardingChannelImpl channel;

    /**
     * Creates a new ForwardingIOChannel object.
     *
     * @param forwardType
     * @param hostToConnectOrBind
     * @param portToConnectOrBind
     * @param originatingHost
     * @param originatingPort
     * @throws ForwardingConfigurationException
     *
     */
    public ForwardingIOChannel(String forwardType, String name, /*ForwardingConfiguration config,*/
                               String hostToConnectOrBind, int portToConnectOrBind,
                               String originatingHost, int originatingPort)
            throws ForwardingConfigurationException {
        if (!forwardType.equals(LOCAL_FORWARDING_CHANNEL) &&
                !forwardType.equals(REMOTE_FORWARDING_CHANNEL) &&
                !forwardType.equals(X11_FORWARDING_CHANNEL)) {
            throw new ForwardingConfigurationException("The forwarding type is invalid");
        }

        channel = new ForwardingChannelImpl(forwardType, name,
                hostToConnectOrBind, portToConnectOrBind, originatingHost,
                originatingPort);
    }

    /**
     * @return
     */
    public byte[] getChannelOpenData() {
        return channel.getChannelOpenData();
    }

    /**
     * @return
     */
    public byte[] getChannelConfirmationData() {
        return channel.getChannelConfirmationData();
    }

    public String getName() {
        return channel.getName();
    }

    /**
     * @return
     */
    public String getChannelType() {
        return channel.getChannelType();
    }

    /**
     * @return
     */
    protected int getMinimumWindowSpace() {
        return 32768;
    }

    /**
     * @return
     */
    protected int getMaximumWindowSpace() {
        return 131072;
    }

    /**
     * @return
     */
    protected int getMaximumPacketSize() {
        return 32768;
    }

    /**
     * @return
     */
    public String getOriginatingHost() {
        return channel.getOriginatingHost();
    }

    /**
     * @return
     */
    public int getOriginatingPort() {
        return channel.getOriginatingPort();
    }

    /**
     * @return
     */
    public String getHostToConnectOrBind() {
        return channel.getHostToConnectOrBind();
    }

    /**
     * @return
     */
    public int getPortToConnectOrBind() {
        return channel.getPortToConnectOrBind();
    }

    /**
     * @param request
     * @param wantReply
     * @param requestData
     * @throws IOException
     */
    protected void onChannelRequest(String request, boolean wantReply,
                                    byte[] requestData) throws IOException {
        connection.sendChannelRequestFailure(this);
    }

    /**
     * @throws IOException
     */
    protected void onChannelOpen() throws IOException {
    }
}
