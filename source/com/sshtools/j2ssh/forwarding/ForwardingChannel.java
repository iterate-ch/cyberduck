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
package com.sshtools.j2ssh.forwarding;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelState;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.IOStreamConnector;
import com.sshtools.j2ssh.io.IOStreamConnectorState;
import com.sshtools.j2ssh.util.MultipleStateMonitor;
import com.sshtools.j2ssh.util.State;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 *  This class implements the Connection Protocol TCPIP forwarding channel.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: ForwardingChannel.java,v 1.3 2002/12/18 00:54:49 martianx Exp
 *      $
 */
public class ForwardingChannel
         extends Channel {
    /**
     *  Local forwarding channel type
     */
    public final static String LOCAL_FORWARDING_CHANNEL = "direct-tcpip";

    /**
     *  Remote forwarding channel type
     */
    public final static String REMOTE_FORWARDING_CHANNEL = "forwarded-tcpip";
    private static Logger log = Logger.getLogger(ForwardingChannel.class);
    private ForwardingConfiguration config;
    private IOStreamConnector input;
    private IOStreamConnector output;
    private IOStreamConnectorMonitor iomon;
    private Socket socket;
    private String forwardType;
    private String hostToConnect;
    private String originatingIPAddress;
    private int originatingPort;
    private int portToConnect;


    /**
     *  Creates a new ForwardingChannel object.
     *
     *@param  forwardType                           the type of channel to
     *      create
     *@param  config                                the forwarding configuration
     *      for this channel
     *@param  hostToConnect                         the host to connect to a
     *      socket to
     *@param  portToConnect                         the port to connect to a
     *      socket to
     *@exception  ForwardingConfigurationException  Description of the Exception
     *@throws  ForwardingConfigurationException     if a configuration error
     *      occurs
     */
    public ForwardingChannel(String forwardType,
            ForwardingConfiguration config,
            String hostToConnect, int portToConnect)
             throws ForwardingConfigurationException {
        this(forwardType, config, hostToConnect, portToConnect, null);
    }


    /**
     *  Creates a new ForwardingChannel object.
     *
     *@param  forwardType                           the type of channel to
     *      create
     *@param  config                                the forwarding configuration
     *      for this channel
     *@param  hostToConnect                         the host to connect to on
     *      the remote side
     *@param  portToConnect                         the port to connect to on
     *      the remote side
     *@param  socket                                The connected socket to
     *      tunnel
     *@exception  ForwardingConfigurationException  Description of the Exception
     *@throws  ForwardingConfigurationException     if a configuration error
     *      occurs
     */
    public ForwardingChannel(String forwardType,
            ForwardingConfiguration config,
            String hostToConnect, int portToConnect,
            Socket socket)
             throws ForwardingConfigurationException {
        if (!forwardType.equals(LOCAL_FORWARDING_CHANNEL)
                && !forwardType.equals(REMOTE_FORWARDING_CHANNEL)) {
            throw new ForwardingConfigurationException("The forwarding type is invalid");
        }

        this.socket = socket;
        this.config = config;
        this.forwardType = forwardType;
        this.hostToConnect = hostToConnect;
        this.portToConnect = portToConnect;
    }


    /**
     *  Returns the SSH_MSG_CHANNEL_OPEN request data
     *
     *@return    a byte array containing the request data
     */
    public byte[] getChannelOpenData() {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();

            if (forwardType.equals(LOCAL_FORWARDING_CHANNEL)) {
                baw.writeString(hostToConnect);
                baw.writeInt(portToConnect);
            } else {
                baw.writeString(config.getAddressToBind());
                baw.writeInt(config.getPortToBind());
            }

            baw.writeString(socket.getRemoteSocketAddress().toString());
            baw.writeInt(socket.getPort());

            return baw.toByteArray();
        } catch (IOException ioe) {
            return null;
        }
    }


    /**
     *  Get the channel type
     *
     *@return    either LOCAL_FORWARDING_CHANNEL or REMOTE_FORWARDING_CHANNEL
     */
    public String getChannelType() {
        return forwardType;
    }


    /**
     *  Gets the minimum number of window space bytes
     *
     *@return
     */
    protected int getMinimumWindowSpace() {
        return 4096;
    }


    /**
     *  Get the maximum number of bytes that should be available for window
     *  space
     *
     *@return    the maximum number of bytes for window space
     */
    protected int getMaximumWindowSpace() {
        return 32648;
    }


    /**
     *  Gets the maximum number of bytes the remote side can send at once
     *
     *@return    the maximum packet size
     */
    protected int getMaximumPacketSize() {
        return 32648;
    }


    /**
     *  Get the forwaridng configuration for this channel
     *
     *@return    the ForwardingConfiguration instance for this channel
     */
    public ForwardingConfiguration getForwardingConfiguration() {
        return config;
    }


    /**
     *  Get the connected socket addess
     *
     *@return    the SocketAddress of the connected socket
     */
    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }


    /**
     *  Get the connected socket locate addess
     *
     *@return    the local InetAddress of the connected socket
     */
    public InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }


    /**
     *  Called by the framework when the channel closes
     *
     *@throws  ServiceOperationException  if an error occurs during closing
     */
    protected void onChannelClose()
             throws ServiceOperationException { }


    /**
     *  Called by the framework when the channel has been confirmed as open
     *
     *@throws  ServiceOperationException  if an error occurs during opening
     */
    protected void onChannelOpen()
             throws ServiceOperationException {

        if (socket == null) {
            try {
                socket = new Socket(hostToConnect, portToConnect);
            } catch (IOException ioe) {
                log.warn("Socket failed to create connection to "
                        + hostToConnect + ":" + String.valueOf(portToConnect));
            }
        }

        InetAddress address =
                ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
        if(address == null)
            throw new ServiceOperationException("Could not get remote address.");
        originatingIPAddress = address.getHostAddress();
        originatingPort = socket.getPort();

        try {
            input =
                    new IOStreamConnector(socket.getInputStream(),
                    this.getOutputStream());

            output =
                    new IOStreamConnector(this.getInputStream(),
                    socket.getOutputStream());
        } catch (IOException ioe) {
            throw new ServiceOperationException("Failed to connect the IOStreams");
        }

        iomon = new IOStreamConnectorMonitor(input, output);
    }

    /**
     * Return the input stream connector
     *
     * @return input stream connector
     */
    public IOStreamConnector getInputConnector() {
        return input;
    }

    /**
     * Return the output stream connector
     *
     * @return output stream connector
     */
    public IOStreamConnector getOutputConnector() {
        return output;
    }


    /**
     *  Called by the framework when a request is recieved for the channel.
     *  Forwarding channels do not have any requests so this methos does
     *  nothing.
     *
     *@param  request                     the request name
     *@param  wantReply                   whether the remote side wants a reply
     *@param  requestData                 the request data
     *@throws  ServiceOperationException  if the request is invalid
     */
    protected void onChannelRequest(String request, boolean wantReply,
            byte requestData[])
             throws ServiceOperationException { }


    /**
     *  This class implements a monitor to determine and close the channel once
     *  both the IOStreams have closed.
     *
     *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
     *@created    20 December 2002
     *@version    $Id: ForwardingChannel.java,v 1.3 2002/12/18 00:54:49 martianx
     *      Exp $
     */
    class IOStreamConnectorMonitor
             implements Runnable {
        private IOStreamConnector input;
        private IOStreamConnector output;
        private Logger log = Logger.getLogger(IOStreamConnectorMonitor.class);
        private Thread thread;


        /**
         *  Creates a new IOStreamConnectorMonitor object.
         *
         *@param  input   the channels input IOStreamConnector
         *@param  output  the channel outputs IOStreamConnector
         */
        public IOStreamConnectorMonitor(IOStreamConnector input,
                IOStreamConnector output) {
            this.input = input;
            this.output = output;
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }


        /**
         *  The thread's main!
         */
        public void run() {
            MultipleStateMonitor msm = new MultipleStateMonitor();
            State inputState = input.getState();
            State outputState = output.getState();
            msm.addState(inputState);
            msm.addState(outputState);

            while ((inputState.getValue() == IOStreamConnectorState.CONNECTED)
                    && (outputState.getValue() == IOStreamConnectorState.CONNECTED)) {
                log.debug("Waiting for IOStreamConnector state change");
                msm.monitor();
                log.debug("IOStreamConnection state has changed");
            }

            try {
                log.info("Both IOStreamConnectors have closed; closing channel");

                ChannelState state = ForwardingChannel.this.getState();

                if (state.getValue() != ChannelState.CHANNEL_CLOSED) {
                    ForwardingChannel.this.close();
                }
            } catch (IOException e) {
                log.warn("Failed to close forwarding channel", e);
            }
        }
    }
}
