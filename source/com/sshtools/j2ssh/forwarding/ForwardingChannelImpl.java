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

import com.sshtools.j2ssh.io.ByteArrayWriter;


/**
 * @author $author$
 * @version $Revision$
 */
public class ForwardingChannelImpl implements ForwardingChannel {
    private static Log log = LogFactory.getLog(ForwardingChannelImpl.class);
    private String forwardType;
    private String originatingHost;
    private int originatingPort;
    private String hostToConnectOrBind;
    private int portToConnectOrBind;
    private String name;

    /**
     * Creates a new ForwardingChannelImpl object.
     *
     * @param forwardType
     * @param hostToConnectOrBind
     * @param portToConnectOrBind
     * @param originatingHost
     * @param originatingPort
     * @throws ForwardingConfigurationException
     *
     */
    public ForwardingChannelImpl(String forwardType, String name, /*ForwardingConfiguration config,*/
                                 String hostToConnectOrBind, int portToConnectOrBind,
                                 String originatingHost, int originatingPort)
            throws ForwardingConfigurationException {
        if (!forwardType.equals(LOCAL_FORWARDING_CHANNEL) &&
                !forwardType.equals(REMOTE_FORWARDING_CHANNEL) &&
                !forwardType.equals(X11_FORWARDING_CHANNEL)) {
            throw new ForwardingConfigurationException("The forwarding type is invalid");
        }

        //this.config = config;
        this.forwardType = forwardType;
        this.hostToConnectOrBind = hostToConnectOrBind;
        this.portToConnectOrBind = portToConnectOrBind;
        this.originatingHost = originatingHost;
        this.originatingPort = originatingPort;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public String getHostToConnectOrBind() {
        return hostToConnectOrBind;
    }

    /**
     * @return
     */
    public int getPortToConnectOrBind() {
        return portToConnectOrBind;
    }

    /**
     * @return
     */
    public byte[] getChannelOpenData() {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();
            baw.writeString(hostToConnectOrBind);
            baw.writeInt(portToConnectOrBind);
            baw.writeString(originatingHost);
            baw.writeInt(originatingPort);

            return baw.toByteArray();
        }
        catch (IOException ioe) {
            return null;
        }
    }

    /**
     * @return
     */
    public byte[] getChannelConfirmationData() {
        return null;
    }

    /**
     * @return
     */
    public String getChannelType() {
        return forwardType;
    }

    /**
     * @return
     */
    public String getOriginatingHost() {
        return originatingHost;
    }

    /**
     * @return
     */
    public int getOriginatingPort() {
        return originatingPort;
    }
}
