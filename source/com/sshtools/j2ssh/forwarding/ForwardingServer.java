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

import java.net.Socket;
import java.net.SocketPermission;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelFactory;
import com.sshtools.j2ssh.connection.ConnectionProtocol;
import com.sshtools.j2ssh.connection.GlobalRequestHandler;
import com.sshtools.j2ssh.connection.GlobalRequestResponse;
import com.sshtools.j2ssh.connection.InvalidChannelException;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.io.ByteArrayReader;

/**
 *  This class implements a forwarding channel server. <p>
 *
 *  The server takes requests for remote forwardings and creates a forwarding
 *  listener to wait for connections, passing any connections made through to
 *  the client end of the secure tunnel. </p> <p>
 *
 *  Additionally, the server will open channels requested by the client so that
 *  the client can forward connections through the secure tunnel for the server
 *  to deliver. </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: ForwardingServer.java,v 1.3 2002/12/10 11:20:35 martianx Exp
 *      $
 */
public class ForwardingServer
         implements ChannelFactory, GlobalRequestHandler {
    private static Logger log = Logger.getLogger(ForwardingServer.class);

    /**
     *  The remote forward global request name
     */
    public final static String REMOTE_FORWARD_REQUEST = "tcpip-forward";

    /**
     *  The cancel remote forwarding global request name
     */
    public final static String REMOTE_FORWARD_CANCEL_REQUEST =
            "cancel-tcpip-forward";
    private ConnectionProtocol connection;
    private List channelTypes = new Vector();
    private List localForwardings = new Vector();
    private List remoteForwardings = new Vector();


    /**
     *  Creates a new ForwardingServer object.
     *
     *@param  connection                     The connection for tunnelling
     *      forwarded sockets
     *@exception  ServiceOperationException  Description of the Exception
     *@throws  ServiceOperationException     if a critical service operation
     *      occurs
     */
    public ForwardingServer(ConnectionProtocol connection)
             throws IOException {
        this.connection = connection;
        channelTypes.add(ForwardingChannel.LOCAL_FORWARDING_CHANNEL);
        connection.allowChannelOpen(this);
        connection.allowGlobalRequest(REMOTE_FORWARD_REQUEST, this);
        connection.allowGlobalRequest(REMOTE_FORWARD_CANCEL_REQUEST, this);
    }


    /**
     *  Gets the channel types that can be created by this implementation of the
     *  ChannelFactory interface
     *
     *@return    a list of channel types
     */
    public List getChannelType() {
        return channelTypes;
    }


    /**
     *  Implements the ChannelFactory method to open a forwarding channel
     *
     *@param  channelType               the channel type to open
     *@param  requestData               the open channel request data
     *@return                           an uninitialized channel
     *@throws  InvalidChannelException  if the channel can not be created
     */
    public Channel createChannel(String channelType, byte requestData[])
             throws InvalidChannelException {
        String hostToConnect = null;
        int portToConnect = 0;
        String originatingAddress = null;
        int originatingPort = 0;

        if (!channelType.equals(ForwardingChannel.LOCAL_FORWARDING_CHANNEL)) {
            throw new InvalidChannelException("The client can only request the "
                    + "opening of a local forwarding channel");
        }

        try {
            ByteArrayReader bar = new ByteArrayReader(requestData);
            hostToConnect = bar.readString();
            portToConnect = (int) bar.readInt();
            originatingAddress = bar.readString();
            originatingPort = (int) bar.readInt();
        } catch (IOException ioe) {
            throw new InvalidChannelException("The channel request data is "
                    + "invalid/or corrupt for channel type "
                    + channelType);
        }

        try {
            // Get a configuration item for the forwarding
            ForwardingConfiguration config =
                    getLocalForwardingByAddress(originatingAddress, originatingPort);

            // Create the channel adding it to the active channels
            ForwardingChannel channel =
                    config.createForwardingChannel(channelType, hostToConnect,
                    portToConnect, null);

            return channel;
        } catch (ForwardingConfigurationException fce) {
            throw new InvalidChannelException("No valid forwarding configuration was available for "
                    + originatingAddress + ": "
                    + String.valueOf(originatingPort));
        }
    }


    /**
     *  Implements the GlobalRequesthandler method allowing the client to
     *  request and cancel a remote forwarding listener
     *
     *@param  requestName  the request name
     *@param  requestData  the request data
     *@return              the result of the global request
     */
    public GlobalRequestResponse processGlobalRequest(String requestName,
            byte requestData[]) {
        GlobalRequestResponse response = GlobalRequestResponse.REQUEST_FAILED;
        String addressToBind = null;
        int portToBind = -1;

        log.debug("Processing " + requestName + " global request");

        try {
            ByteArrayReader bar = new ByteArrayReader(requestData);
            addressToBind = bar.readString();
            portToBind = (int) bar.readInt();

            if (requestName.equals(REMOTE_FORWARD_REQUEST)) {

                addRemoteForwardingConfiguration(addressToBind, portToBind);
                response = GlobalRequestResponse.REQUEST_SUCCEEDED;
            }

            if (requestName.equals(REMOTE_FORWARD_CANCEL_REQUEST)) {

                removeRemoteForwarding(addressToBind, portToBind);
                response = GlobalRequestResponse.REQUEST_SUCCEEDED;
            }
        } catch (IOException ioe) {
            log.warn("The client failed to request " + requestName + " for "
                    + addressToBind + ":" + String.valueOf(portToBind), ioe);
        }

        return response;
    }


    /**
     *  Gets a local forwarding configuration by originating address
     *
     *@param  orginatingAddress                  the original address of the
     *      connection
     *@param  originatingPort                    the original port of the
     *      connection
     *@return                                    the forwarding configuration
     *      for the originating address
     *@throws  ForwardingConfigurationException  if the configuration does not
     *      exist
     */
    protected ForwardingConfiguration getLocalForwardingByAddress(String orginatingAddress,
            int originatingPort)
             throws ForwardingConfigurationException {
        Iterator it = localForwardings.iterator();
        ForwardingConfiguration config;

        while (it.hasNext()) {
            config = (ForwardingConfiguration) it.next();

            if (config.getAddressToBind().equals(orginatingAddress)
                    && (config.getPortToBind() == originatingPort)) {
                return config;
            }
        }

        config =
                new ForwardingConfiguration(orginatingAddress, originatingPort);

        localForwardings.add(config);

        return config;
    }


    /**
     *  Gets a remote forwarding configuration by the address bound
     *
     *@param  addressToBind                      the address being listened to
     *@param  portToBind                         the port being listened to
     *@return                                    the forwarding configuration
     *      for the address bound
     *@throws  ForwardingConfigurationException  if the configuration does not
     *      exist
     */
    protected ForwardingConfiguration getRemoteForwardingByAddress(String addressToBind,
            int portToBind)
             throws ForwardingConfigurationException {
        Iterator it = remoteForwardings.iterator();
        ForwardingConfiguration config;

        while (it.hasNext()) {
            config = (ForwardingConfiguration) it.next();

            if (config.getAddressToBind().equals(addressToBind)
                    && (config.getPortToBind() == portToBind)) {
                return config;
            }
        }

        throw new ForwardingConfigurationException("The remote forwarding does not exist!");
    }


    /**
     *  Adds a remote forwarding configuration
     *
     *@param  addressToBind                      the address to bind to
     *@param  portToBind                         the port to bind to
     *@throws  ForwardingConfigurationException  if the address is already in
     *      use or the security manager refuses to listen
     */
    protected void addRemoteForwardingConfiguration(String addressToBind,
            int portToBind)
             throws ForwardingConfigurationException {
        // Is the server already listening
        Iterator it = remoteForwardings.iterator();
        ForwardingConfiguration config;

        while (it.hasNext()) {
            config = (ForwardingConfiguration) it.next();

            if (config.getAddressToBind().equals(addressToBind)
                    && (config.getPortToBind() == portToBind)) {
                throw new ForwardingConfigurationException("The address and port are already in use!");
            }
        }

        config = new ForwardingConfiguration(addressToBind, portToBind);

        // Check the security mananger
        SecurityManager manager = System.getSecurityManager();

        if (manager != null) {
            try {
                manager.checkPermission(new SocketPermission(addressToBind
                        + ":"
                        + String.valueOf(portToBind),
                        "accept,listen"));
            } catch (SecurityException e) {
                throw new ForwardingConfigurationException("The security manager has denied listen permision on "
                        + addressToBind
                        + ":"
                        + String.valueOf(portToBind));
            }
        }

        ForwardingListener listener =
                new ServerForwardingListener(connection, addressToBind, portToBind);
        remoteForwardings.add(listener);
        listener.start();
    }


    /**
     *  Removes a remote forwarding configuration from the server
     *
     *@param  addressToBind                      the address to bind
     *@param  portToBind                         the port to bind
     *@throws  ForwardingConfigurationException  if the configuration does not
     *      exist
     */
    protected void removeRemoteForwarding(String addressToBind, int portToBind)
             throws ForwardingConfigurationException {
        ForwardingConfiguration config =
                getRemoteForwardingByAddress(addressToBind, portToBind);

        // Stop the forwarding
        config.stop();

        // Remove from the remote forwardings list
        remoteForwardings.remove(config);
    }


    /**
     *  Implements a ForwardingListener for the server
     *
     *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
     *@created    20 December 2002
     *@version    $Id: ForwardingServer.java,v 1.3 2002/12/10 11:20:35 martianx
     *      Exp $
     */
    class ServerForwardingListener
             extends ForwardingListener {
        /**
         *  Creates a new ServerForwardingListener object.
         *
         *@param  connection     the connection to tunnel through
         *@param  addressToBind  the address to listen on
         *@param  portToBind     the port to listen on
         */
        public ServerForwardingListener(ConnectionProtocol connection,
                String addressToBind, int portToBind) {
            super(connection, addressToBind, portToBind);
        }


        /**
         *  Creates a remote forwarding channel
         *
         *@param  hostToConnect                      the host to connect the
         *      remote end of the channel
         *@param  portToConnect                      the port to connect the
         *      remote end of the channel
         *@param  socket                             the connected socket to
         *      forward
         *@return                                    an initialized forwarding
         *      channel ready to be opened
         *@throws  ForwardingConfigurationException  if the channel cannot be
         *      created
         */
        public ForwardingChannel createChannel(String hostToConnect,
                int portToConnect, Socket socket)
                 throws ForwardingConfigurationException {
            return createForwardingChannel(ForwardingChannel.REMOTE_FORWARDING_CHANNEL,
                    hostToConnect, portToConnect, socket);
        }
    }
}
