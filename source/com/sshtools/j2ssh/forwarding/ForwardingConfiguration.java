/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
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

import com.sshtools.j2ssh.connection.ChannelState;
import com.sshtools.j2ssh.util.InvalidStateException;
import com.sshtools.j2ssh.io.IOStreamConnectorListener;
import org.apache.log4j.Logger;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import javax.swing.event.EventListenerList;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;


import com.sshtools.j2ssh.util.StartStopState;

/**
 *  <p>
 *
 *  This class represents a single forwaring configuration and provides a means
 *  to create forwarding channels based upon the configuration properties. </p>
 *  <p>
 *
 *  The class tracks the created channels by monitoring the channel state and
 *  makes the active channels available for inspection through <code>getActiveForwardingChannels</code>
 *  . When the channel closes the monitor automatically removes the channel from
 *  the active list. </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: ForwardingConfiguration.java,v 1.2 2002/12/10 00:07:30
 *      martianx Exp $
 */
public class ForwardingConfiguration {
    /**
     *  The current state of the forwarding
     */
    protected StartStopState state = new StartStopState(StartStopState.STOPPED);

    /**
     *  The address to bind the tunnel to
     */
    protected String addressToBind;

    /**
     *  The host to connect the tunnel to
     */
    protected String hostToConnect;

    /**
     *  The name of the configuration
     */
    protected String name;

    /**
     *  The port to bind the tunnel to
     */
    protected int portToBind;

    /**
     *  The port to connect the tunnel to
     */
    protected int portToConnect;

    /**
     *  List of listeners to be informed when something happens on the forwarded
     *  connection, such as starting, stopping or data
     */
    protected EventListenerList listenerList = new EventListenerList();
    private List activeForwardings = new Vector();

    /**
     *  Creates a new ForwardingConfiguration object.
     *
     *@param  name           a name to identify the configuration
     *@param  addressToBind  the address to bind the tunnel to
     *@param  portToBind     the port to bind the tunnel to
     *@param  hostToConnect  the host to connect the tunnel to
     *@param  portToConnect  the port to connect the tunnel to
     */
    public ForwardingConfiguration(String name, String addressToBind,
        int portToBind, String hostToConnect, int portToConnect) {
        this.addressToBind = addressToBind;
        this.portToBind = portToBind;
        this.name = name;
        this.hostToConnect = hostToConnect;
        this.portToConnect = portToConnect;
    }

    /**
     *  Creates a new ForwardingConfiguration object.
     *
     *@param  addressToBind  the address to bind the tunnel to
     *@param  portToBind     the port to bind the tunnel to
     */
    protected ForwardingConfiguration(String addressToBind, int portToBind) {
        this(addressToBind + ":" + String.valueOf(portToBind), addressToBind,
            portToBind, "[Specified by connecting computer]", -1);
    }

    /**
     * Add a listener to be informed when a forwarded connection is open, closed
     * or data is sent
     *
     * @param l listener to add
     */
    public void addForwardingConfigurationListener(
        ForwardingConfigurationListener l) {
        listenerList.add(ForwardingConfigurationListener.class, l);
    }

    /**
     * Remove a listener being informed when a forwarded connection is open, closed
     * or data is sent
     *
     * @param l listener to remove
     */
    public void removeForwardingConfigurationListener(
        ForwardingConfigurationListener l) {
        listenerList.remove(ForwardingConfigurationListener.class, l);
    }

    /**
     *  Gets the currently active forwarding channels
     *
     *@return    a list of forwarding channels
     */
    public List getActiveForwardingChannels() {
        return activeForwardings;
    }

    /**
     *  Gets the address the tunnel is bound to
     *
     *@return    the ip address
     */
    public String getAddressToBind() {
        return addressToBind;
    }

    /**
     *  Gets the host the tunnel is connected to
     *
     *@return    the host the tunnel is connected to
     */
    public String getHostToConnect() {
        return hostToConnect;
    }

    /**
     *  Gets the name of this configuration
     *
     *@return    the configuration name
     */
    public String getName() {
        return name;
    }

    /**
     *  Gets the port this configuration is bound to
     *
     *@return    port number
     */
    public int getPortToBind() {
        return portToBind;
    }

    /**
     *  Gets the port the tunnel is connected to
     *
     *@return    port number
     */
    public int getPortToConnect() {
        return portToConnect;
    }

    /**
     *  Gets the state of the forwarding configuration
     *
     *@return    the configuration state
     */
    public StartStopState getState() {
        return state;
    }

    /**
     *  Starts the forwaring configuraiton and allows new forwarding channels to
     *  be opened.
     */
    public void start() {
        state.setValue(StartStopState.STARTED);
    }

    /**
     *  Stops the forwarding configuration from creating any more channels
     */
    public void stop() {
        state.setValue(StartStopState.STOPPED);
    }

    /**
     *  Creates a forwarding channel with the configurations properties
     *
     *@param  type                               the type of channel to create
     *@param  hostToConnect                      the host to connect to
     *@param  portToConnect                      the port to connect to
     *@param  socket                             the connected socket to tunnel
     *@return                                    an initialized forwarding
     *      channel
     *@throws  ForwardingConfigurationException  if the configuration failed to
     *      create thr channel, or the forwarding has been stopped
     */
    protected ForwardingChannel createForwardingChannel(String type,
            String hostToConnect,
            int portToConnect,
            Socket socket)
             throws ForwardingConfigurationException {
        if (state.getValue() == StartStopState.STOPPED) {
            throw new ForwardingConfigurationException("The forwarding has been stopped");
        }

        if (!type.equals(ForwardingChannel.LOCAL_FORWARDING_CHANNEL)
                && !type.equals(ForwardingChannel.REMOTE_FORWARDING_CHANNEL)) {
            throw new ForwardingConfigurationException("The channel type must either be "
                    + "ForwardingChannel.LOCAL_FORWARDING_CHANNEL_TYPE or "
                    + "ForwardingChannel.REMOTE_FORWARDING_CHANNEL_TYPE");
        }

        final ForwardingChannel channel = new ForwardingChannel(type, this,
                hostToConnect, portToConnect, socket);

        // Add it to the active forwardings
        activeForwardings.add(channel);

        // Start a monitor so we can detect the close
        ForwardingChannelMonitor monitor = new ForwardingChannelMonitor(channel);

        return channel;
    }

    /**
     *  This class implements a monitor to detect when a created channel has
     *  been closed so that it can be removed from the active channels lisr.
     *
     *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
     *@created    20 December 2002
     *@version    $Id: ForwardingConfiguration.java,v 1.2 2002/12/10 00:07:30
     *      martianx Exp $
     */
    protected class ForwardingChannelMonitor implements Runnable {
        private ForwardingChannel channel;
        private Logger log = Logger.getLogger(ForwardingChannelMonitor.class);
        private Thread thread;

        /**
         *  Creates a new ForwardingChannelMonitor object.
         *
         *@param  channel  the channel to monitor
         *@param  inListener the input stream listener
         *@param  outListener the output stream listener
         */
        public ForwardingChannelMonitor(ForwardingChannel channel) {
            this.channel = channel;
            this.thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        /**
         *  The monitors thread; waits for ChannelState.CHANNEL_CLOSED and then
         *  removes the channel from the active channels list.
         */
        public void run() {
            ChannelState state = channel.getState();

            try {
                log.debug("Waiting for the channel to open");
                state.waitForState(ChannelState.CHANNEL_OPEN);
            } catch(InvalidStateException ise) {
            }

        // Listen for data being forwarded
            IOStreamConnectorListener inListener = new IOStreamConnectorListener() {
                public void data(byte[] data, int count) {
                    ForwardingConfigurationListener[] l =
                        (ForwardingConfigurationListener[])listenerList.getListeners(
                        ForwardingConfigurationListener.class);
                    for(int i = (l.length - 1); i >= 0; i--)
                        l[i].dataSent(channel, count);
                }
            };
            channel.getInputConnector().addIOStreamConnectorListener(inListener);
            IOStreamConnectorListener outListener = new IOStreamConnectorListener() {
                public void data(byte[] data, int count) {
                    ForwardingConfigurationListener[] l =
                        (ForwardingConfigurationListener[])listenerList.getListeners(
                        ForwardingConfigurationListener.class);
                    for(int i = (l.length - 1); i >= 0; i--)
                        l[i].dataReceived(channel, count);
                }
            };
            channel.getOutputConnector().addIOStreamConnectorListener(outListener);
            ForwardingConfigurationListener[] l =
                (ForwardingConfigurationListener[])
                listenerList.getListeners(
                ForwardingConfigurationListener.class);
            for(int i = (l.length - 1); i >= 0; i--)
                l[i].opened(channel);

        // Now wait to be closed
            try {
                log.debug("Waiting for the channel to close");
                state.waitForState(ChannelState.CHANNEL_CLOSED);
            } catch(InvalidStateException ise) {
            }

            // Stop listening for data events
            channel.getInputConnector().removeIOStreamConnectorListener(inListener);
            channel.getOutputConnector().removeIOStreamConnectorListener(outListener);

            log.debug(
                "Removing closed forwarding channel from active forwardings");
            ForwardingConfiguration.this.activeForwardings.remove(channel);

            // Inform all the listeners of what has just happened
            l = (ForwardingConfigurationListener[])
                listenerList.getListeners(
                ForwardingConfigurationListener.class);

            for(int i = (l.length - 1); i >= 0; i--)
                l[i].closed(channel);
        }
    }
}
