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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelFactory;
import com.sshtools.j2ssh.connection.ConnectionProtocol;
import com.sshtools.j2ssh.connection.InvalidChannelException;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.util.InvalidStateException;
import com.sshtools.j2ssh.util.StartStopState;

/**
 *  This class implements a forwarding manager for an SSH client. The manager
 *  allows the addition of named local and remote configurations.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: ForwardingClient.java,v 1.2 2002/12/10 00:07:30 martianx Exp
 *      $
 */
public class ForwardingClient
         implements ChannelFactory {
    private static Logger log = Logger.getLogger(ForwardingClient.class);
    private ConnectionProtocol connection;
    private List channelTypes = new Vector();
    private Map localForwardings = new HashMap();
    private Map remoteForwardings = new HashMap();


    /**
     *  Creates a new ForwardingClient object.
     *
     *@param  connection                     The ConnectionProtocol instance for
     *      tunnelling
     *@exception  ServiceOperationException  Description of the Exception
     *@throws  ServiceOperationException     if a critical service operation
     *      fails
     */
    public ForwardingClient(ConnectionProtocol connection)
             throws IOException {
        this.connection = connection;
        channelTypes.add(ForwardingChannel.REMOTE_FORWARDING_CHANNEL);
        connection.allowChannelOpen(this);
    }


    /**
     *  Get the channel types
     *
     *@return    List of Strings containing the channel types this
     *      implementation can open
     */
    public List getChannelType() {
        return channelTypes;
    }


    public boolean hasActiveConfigurations() {

      // First check the size
      if(localForwardings.size() == 0 &&
          remoteForwardings.size() == 0)
          return false;

      Iterator it = localForwardings.values().iterator();
      while(it.hasNext()) {
        if(((ForwardingConfiguration)it.next()).getState().getValue()==StartStopState.STARTED)
          return true;
      }

      it = remoteForwardings.values().iterator();
      while(it.hasNext()) {
        if(((ForwardingConfiguration)it.next()).getState().getValue()==StartStopState.STARTED)
          return true;
      }

      return false;

    }

    public boolean hasActiveForwardings() {

      // First check the size
      if(localForwardings.size() == 0 &&
          remoteForwardings.size() == 0)
          return false;

      Iterator it = localForwardings.values().iterator();
      while(it.hasNext()) {
        if(((ForwardingConfiguration)it.next()).getActiveForwardingChannels().size() > 0)
          return true;
      }

      it = remoteForwardings.values().iterator();
      while(it.hasNext()) {
        if(((ForwardingConfiguration)it.next()).getActiveForwardingChannels().size() > 0)
          return true;
      }

      return false;





    }
    /**
     *  Get a local forwarding configuration by its bound address
     *
     *@param  addressToBind                      the ip address
     *@param  portToBind                         the port
     *@return                                    the local forwaring instance
     *      for the address
     *@throws  ForwardingConfigurationException  if the configuration does not
     *      exist
     */
    public ForwardingConfiguration getLocalForwardingByAddress(String addressToBind,
            int portToBind)
             throws ForwardingConfigurationException {
        Iterator it = localForwardings.values().iterator();
        ForwardingConfiguration config;

        while (it.hasNext()) {
            config = (ForwardingConfiguration) it.next();

            if (config.getAddressToBind().equals(addressToBind)
                    && (config.getPortToBind() == portToBind)) {
                return config;
            }
        }

        throw new ForwardingConfigurationException("The configuration does not exist");
    }


    /**
     *  Gets the local forwarding by its unique name
     *
     *@param  name                               the forwardins unique name
     *@return                                    the forwarding configuraton
     *      instance
     *@throws  ForwardingConfigurationException  if the configuration does not
     *      exist
     */
    public ForwardingConfiguration getLocalForwardingByName(String name)
             throws ForwardingConfigurationException {
        if (!localForwardings.containsKey(name)) {
            throw new ForwardingConfigurationException("The configuraiton does not exist!");
        }

        return (ForwardingConfiguration) localForwardings.get(name);
    }


    /**
     *  Gets the local forwardings
     *
     *@return    the Map of forwardings name/ForwardingConfiguration
     */
    public Map getLocalForwardings() {
        return localForwardings;
    }


    /**
     *  Gets the remote forwardings
     *
     *@return    the Map of forwardings name/ClientForwardingListener
     */
    public Map getRemoteForwardings() {
        return remoteForwardings;
    }


    /**
     *  Get a remote forwarding by the address bound
     *
     *@param  addressToBind                      the address
     *@param  portToBind                         the port
     *@return                                    the remote forwardings
     *      configuration
     *@throws  ForwardingConfigurationException  if the forwarding does not
     *      exist
     */
    public ForwardingConfiguration getRemoteForwardingByAddress(String addressToBind,
            int portToBind)
             throws ForwardingConfigurationException {
        Iterator it = remoteForwardings.values().iterator();
        ForwardingConfiguration config;

        while (it.hasNext()) {
            config = (ForwardingConfiguration) it.next();

            if (config.getAddressToBind().equals(addressToBind)
                    && (config.getPortToBind() == portToBind)) {
                return config;
            }
        }

        throw new ForwardingConfigurationException("The configuration does not exist");
    }

    /** Remove a local forwarding configuration from the fowarding client. This
     *  will stop the configuration if it is open
     *
     * @param name unique name
     */
    public void removeLocalForwarding(String name)
        throws ForwardingConfigurationException {
        if (!localForwardings.containsKey(name)) {
            throw new ForwardingConfigurationException("The name is not a valid forwarding configuration");
        }
        ForwardingListener listener =
                (ForwardingListener) localForwardings.get(name);
        if(listener.isRunning())
            stopLocalForwarding(name);
        localForwardings.remove(name);
    }

    /** Remove a remote forwarding configuration from the fowarding client. This
     *  will stop the configuration if it is open
     *
     * @param name unique name
     */
    public void removeRemoteForwarding(String name)
             throws TransportProtocolException,
            ServiceOperationException,
            ForwardingConfigurationException {
        if (!remoteForwardings.containsKey(name)) {
            throw new ForwardingConfigurationException("The name is not a valid forwarding configuration");
        }
        ForwardingListener listener =
                (ForwardingListener) remoteForwardings.get(name);
        if(listener.isRunning())
            stopRemoteForwarding(name);
        remoteForwardings.remove(name);
    }


    /**
     *  Adds a local forwarding configuration to the forwarding client
     *
     *@param  uniqueName                         a name to identify
     *@param  addressToBind                      the address to bind & listen to
     *@param  portToBind                         the port to bind & listen to
     *@param  hostToConnect                      the host to connect from the
     *      remote side
     *@param  portToConnect                      the port to connect from the
     *      remote side
     *@throws  ForwardingConfigurationException  if the forwarding details are
     *      invalid
     */
    public void addLocalForwarding(String uniqueName, String addressToBind,
            int portToBind, String hostToConnect,
            int portToConnect)
             throws ForwardingConfigurationException {
        // Check that the name does not exist
        if (localForwardings.containsKey(uniqueName)) {
            throw new ForwardingConfigurationException("The configuration name already exists!");
        }

        // Check that the address to bind and port are not already being used
        Iterator it = localForwardings.values().iterator();
        ForwardingConfiguration config;

        while (it.hasNext()) {
            config = (ForwardingConfiguration) it.next();

            if (config.getAddressToBind().equals(addressToBind)
                    && (config.getPortToBind() == portToBind)) {
                throw new ForwardingConfigurationException("The address and port are already in use");
            }
        }

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

        // Create the configuration object
        localForwardings.put(uniqueName,
                new ClientForwardingListener(uniqueName,
                connection,
                addressToBind,
                portToBind,
                hostToConnect,
                portToConnect));
    }


     public void addLocalForwarding(ForwardingConfiguration fwd)
             throws ForwardingConfigurationException {
        // Check that the name does not exist
        if (localForwardings.containsKey(fwd.getName())) {
            throw new ForwardingConfigurationException("The configuration name already exists!");
        }

        // Check that the address to bind and port are not already being used
        Iterator it = localForwardings.values().iterator();
        ForwardingConfiguration config;
        while (it.hasNext()) {
            config = (ForwardingConfiguration) it.next();

            if (config.getAddressToBind().equals(fwd.getAddressToBind())
                    && (config.getPortToBind() == fwd.getPortToBind())) {
                throw new ForwardingConfigurationException("The address and port are already in use");
            }
        }

        // Check the security mananger
        SecurityManager manager = System.getSecurityManager();

        if (manager != null) {
            try {
                manager.checkPermission(new SocketPermission(fwd.getAddressToBind()
                        + ":"
                        + String.valueOf(fwd.getPortToBind()),
                        "accept,listen"));
            } catch (SecurityException e) {
                throw new ForwardingConfigurationException("The security manager has denied listen permision on "
                        + fwd.getAddressToBind()
                        + ":"
                        + String.valueOf(fwd.getPortToBind()));
            }
        }

        // Create the configuration object
        localForwardings.put(fwd.getName(),
                new ClientForwardingListener(fwd.getName(),
                connection,
                fwd.getAddressToBind(),
                fwd.getPortToBind(),
                fwd.getHostToConnect(),
                fwd.getPortToConnect()));
    }


    /**
     *  Adds a remote forwarding to the configuration client.
     *
     *@param  uniqueName                         the forwarding name
     *@param  addressToBind                      the address for the server to
     *      bind
     *@param  portToBind                         the port for the server to bind
     *@param  hostToConnect                      the host to connect from the
     *      local side
     *@param  portToConnect                      the port to connect from the
     *      local side
     *@throws  ForwardingConfigurationException  if the forwarding details are
     *      invalid
     */
    public void addRemoteForwarding(String uniqueName, String addressToBind,
            int portToBind, String hostToConnect,
            int portToConnect)
             throws ForwardingConfigurationException {
        // Check that the name does not exist
        if (remoteForwardings.containsKey(uniqueName)) {
            throw new ForwardingConfigurationException("The remote forwaring configuration name already exists!");
        }

        // Check that the address to bind and port are not already being used
        Iterator it = remoteForwardings.values().iterator();
        ForwardingConfiguration config;

        while (it.hasNext()) {
            config = (ForwardingConfiguration) it.next();

            if (config.getAddressToBind().equals(addressToBind)
                    && (config.getPortToBind() == portToBind)) {
                throw new ForwardingConfigurationException("The remote forwarding address and port are already in use");
            }
        }

        // Check the security mananger
        SecurityManager manager = System.getSecurityManager();

        if (manager != null) {
            try {
                manager.checkPermission(new SocketPermission(hostToConnect
                        + ":"
                        + String.valueOf(portToConnect),
                        "connect"));
            } catch (SecurityException e) {
                throw new ForwardingConfigurationException("The security manager has denied connect permision on "
                        + hostToConnect
                        + ":"
                        + String.valueOf(portToConnect));
            }
        }

        // Create the configuration object
        remoteForwardings.put(uniqueName,
                new ForwardingConfiguration(uniqueName,
                addressToBind,
                portToBind,
                hostToConnect,
                portToConnect));
    }


public void addRemoteForwarding(ForwardingConfiguration fwd)
             throws ForwardingConfigurationException {
        // Check that the name does not exist
        if (remoteForwardings.containsKey(fwd.getName())) {
            throw new ForwardingConfigurationException("The remote forwaring configuration name already exists!");
        }

        // Check that the address to bind and port are not already being used
        Iterator it = remoteForwardings.values().iterator();
        ForwardingConfiguration config;

        while (it.hasNext()) {
            config = (ForwardingConfiguration) it.next();

            if (config.getAddressToBind().equals(fwd.getAddressToBind())
                    && (config.getPortToBind() == fwd.getPortToBind())) {
                throw new ForwardingConfigurationException("The remote forwarding address and port are already in use");
            }
        }

        // Check the security mananger
        SecurityManager manager = System.getSecurityManager();

        if (manager != null) {
            try {
                manager.checkPermission(new SocketPermission(fwd.getHostToConnect()
                        + ":"
                        + String.valueOf(fwd.getPortToConnect()),
                        "connect"));
            } catch (SecurityException e) {
                throw new ForwardingConfigurationException("The security manager has denied connect permision on "
                        + fwd.getHostToConnect()
                        + ":"
                        + String.valueOf(fwd.getPortToConnect()));
            }
        }

        // Create the configuration object
        remoteForwardings.put(fwd.getName(), fwd);
    }

    /**
     *  Opens a remote forwarding channel when requested by the server.
     *
     *@param  channelType               the channel type request by the server
     *@param  requestData               the request data
     *@return                           the uninitialized channel
     *@throws  InvalidChannelException  if the configuration was not previously
     *      requested by the client
     */
    public Channel createChannel(String channelType, byte requestData[])
             throws InvalidChannelException {
        String addressBound = null;
        int portBound = 0;

        if (!channelType.equals(ForwardingChannel.REMOTE_FORWARDING_CHANNEL)) {
            throw new InvalidChannelException("The server can only request the opening a remote forwarding channel");
        }

        try {
            ByteArrayReader bar = new ByteArrayReader(requestData);
            addressBound = bar.readString();
            portBound = (int) bar.readInt();
        } catch (IOException ioe) {
            throw new InvalidChannelException("The channel request data is invalid/or corrupt");
        }

        try {
            ForwardingConfiguration config =
                    getRemoteForwardingByAddress(addressBound, portBound);

            // Create the channel adding it to the active channels
            ForwardingChannel channel =
                    config.createForwardingChannel(channelType,
                    config.getHostToConnect(),
                    config.getPortToConnect(), null);

            return channel;
        } catch (ForwardingConfigurationException fce) {
            throw new InvalidChannelException("No valid forwarding configuration was available for "
                    + addressBound + ": "
                    + String.valueOf(portBound));
        }
    }


    /**
     *  Starts the local forwarding. Calling this method starts a socket
     *  listener and when connected all connection data is tunnelled to the
     *  remote side
     *
     *@param  uniqueName                         the configuration name
     *@throws  ForwardingConfigurationException  if the configuration does not
     *      exist
     */
    public void startLocalForwarding(String uniqueName)
             throws ForwardingConfigurationException {
        if (!localForwardings.containsKey(uniqueName)) {
            throw new ForwardingConfigurationException("The name is not a valid forwarding configuration");
        }

        ForwardingListener listener =
                (ForwardingListener) localForwardings.get(uniqueName);

        listener.start();
    }


    /**
     *  Starts remote forwarding by making a request to the server to start
     *  listening on the bound address. When connections are recieved the server
     *  requests the opening of a channel to tunnel the connection at which
     *  point the local side opens up a connection to the host specified in the
     *  configuration
     *
     *@param  name                               The remote forwarding
     *      configuration name
     *@throws  TransportProtocolException        if an error occurs in the
     *      Transport Protocol
     *@throws  ServiceOperationException         if a critical service operation
     *      fails
     *@throws  ForwardingConfigurationException  if the configuration does not
     *      exist
     */
    public void startRemoteForwarding(String name)
             throws TransportProtocolException,
            ServiceOperationException,
            ForwardingConfigurationException {
        try {
            if (!remoteForwardings.containsKey(name)) {
                throw new ForwardingConfigurationException("The name is not a valid forwarding configuration");
            }

            ForwardingConfiguration config =
                    (ForwardingConfiguration) remoteForwardings.get(name);

            ByteArrayWriter baw = new ByteArrayWriter();
            baw.writeString(config.getAddressToBind());
            baw.writeInt(config.getPortToBind());

            if (connection.sendGlobalRequest(ForwardingServer.REMOTE_FORWARD_REQUEST,
                    true, baw.toByteArray())) {

                remoteForwardings.put(name, config);

                config.getState().setValue(StartStopState.STARTED);

                log.info("Remote forwarding configuration '" + name
                        + "' started");

                if (log.isDebugEnabled()) {
                    log.debug("Address to bind: " + config.getAddressToBind());
                    log.debug("Port to bind: "
                            + String.valueOf(config.getPortToBind()));
                    log.debug("Host to connect: " + config.hostToConnect);
                    log.debug("Port to connect: " + config.portToConnect);
                }
            }
        } catch (IOException ioe) {
            throw new ForwardingConfigurationException("Failed to write global request data");
        }
    }


    /**
     *  Stops the local fowwarding configuration from listenening for new
     *  connections
     *
     *@param  uniqueName                         the configuration name
     *@throws  ForwardingConfigurationException  if the configuraiton does not
     *      exist
     */
    public void stopLocalForwarding(String uniqueName)
             throws ForwardingConfigurationException {
        if (!localForwardings.containsKey(uniqueName)) {
            throw new ForwardingConfigurationException("The name is not a valid forwarding configuration");
        }

        ForwardingListener listener =
                (ForwardingListener) localForwardings.get(uniqueName);

        listener.stop();

        log.info("Local forwarding configuration " + uniqueName + "' stopped");
    }


    /**
     *  Stops the remote forwarding by requesting that the server stop
     *  listeneing for connections on the bound address and port
     *
     *@param  name                               the configuraiton name
     *@throws  TransportProtocolException        if an error occurs sending data
     *@throws  ServiceOperationException         if a criticial service
     *      operation fails
     *@throws  ForwardingConfigurationException  if the configuration does not
     *      exist
     */
    public void stopRemoteForwarding(String name)
             throws TransportProtocolException,
            ServiceOperationException,
            ForwardingConfigurationException {
        try {
            if (!remoteForwardings.containsKey(name)) {
                throw new ForwardingConfigurationException("The remote forwarding configuration does not exist");
            }

            ForwardingConfiguration config =
                    (ForwardingConfiguration) remoteForwardings.get(name);

            ByteArrayWriter baw = new ByteArrayWriter();
            baw.writeString(config.getAddressToBind());
            baw.writeInt(config.getPortToBind());

            if (connection.sendGlobalRequest(ForwardingServer.REMOTE_FORWARD_CANCEL_REQUEST,
                    true, baw.toByteArray())) {

                config.getState().setValue(StartStopState.STOPPED);

                log.info("Remote forwarding configuration '" + name
                        + "' stopped");
            }
        } catch (IOException ioe) {
            throw new ForwardingConfigurationException("Failed to write global request data");
        }
    }


    /**
     *  This class implements the client socket listener
     *
     *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
     *@created    20 December 2002
     *@version    $Id: ForwardingClient.java,v 1.2 2002/12/10 00:07:30 martianx
     *      Exp $
     */
    public class ClientForwardingListener
             extends ForwardingListener {
        /**
         *  Creates a new ClientForwardingListener object.
         *
         *@param  name           the configuration name
         *@param  connection     the connection for tunnelling
         *@param  addressToBind  the address to bind to
         *@param  portToBind     the port to bind to
         *@param  hostToConnect  the host the other end of the tunnel connects
         *      to
         *@param  portToConnect  the port the other end of the tunnel connects
         *      to
         */
        public ClientForwardingListener(String name,
                ConnectionProtocol connection,
                String addressToBind, int portToBind,
                String hostToConnect, int portToConnect) {
            super(name, connection, addressToBind, portToBind, hostToConnect,
                    portToConnect);
        }


        /**
         *  Creates a local forwarding channel for a connected socket
         *
         *@param  hostToConnect                      the host the other end of
         *      the tunnel connects to
         *@param  portToConnect                      the port the other end of
         *      the tunnel connects to
         *@param  socket                             the connected socket
         *@return                                    an initailized channel
         *      ready for opening
         *@throws  ForwardingConfigurationException  if the channel can not be
         *      created
         */
        public ForwardingChannel createChannel(String hostToConnect,
                int portToConnect, Socket socket)
                 throws ForwardingConfigurationException {
            return createForwardingChannel(ForwardingChannel.LOCAL_FORWARDING_CHANNEL,
                    hostToConnect, portToConnect, socket);
        }
    }
}
