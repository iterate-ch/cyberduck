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

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelEventListener;
import com.sshtools.j2ssh.util.StartStopState;


/**
 * @author $author$
 * @version $Revision$
 */
public class ForwardingConfiguration {
	private static Log log = LogFactory.getLog(ForwardingConfiguration.class);

	/**  */
	protected StartStopState state = new StartStopState(StartStopState.STOPPED);

	/**  */
	protected String addressToBind;

	/**  */
	protected String hostToConnect;

	/**  */
	protected String name;

	/**  */
	protected int portToBind;

	/**  */
	protected int portToConnect;

	/**  */
	protected ForwardingConfigurationMonitor monitor = new ForwardingConfigurationMonitor();

	/**  */
	protected EventListenerList listenerList = new EventListenerList();
	private List activeForwardings = new Vector();

	/**
	 * Creates a new ForwardingConfiguration object.
	 *
	 * @param name
	 * @param addressToBind
	 * @param portToBind
	 * @param hostToConnect
	 * @param portToConnect
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
	 * Creates a new ForwardingConfiguration object.
	 *
	 * @param addressToBind
	 * @param portToBind
	 */
	public ForwardingConfiguration(String addressToBind, int portToBind) {
		this(addressToBind+":"+String.valueOf(portToBind), addressToBind,
		    portToBind, "[Specified by connecting computer]", -1);
	}

	/**
	 * @param l
	 */
	public void addForwardingConfigurationListener(ForwardingConfigurationListener l) {
		listenerList.add(ForwardingConfigurationListener.class, l);
	}

	/**
	 * @param l
	 */
	public void removeForwardingConfigurationListener(ForwardingConfigurationListener l) {
		listenerList.remove(ForwardingConfigurationListener.class, l);
	}

	/**
	 * @return
	 */
	public List getActiveForwardingSocketChannels() {
		return activeForwardings;
	}

	public boolean isForwarding() {
		return state.getValue() == StartStopState.STARTED;
	}

	/**
	 * @return
	 */
	public String getAddressToBind() {
		return addressToBind;
	}

	/**
	 * @return
	 */
	public String getHostToConnect() {
		return hostToConnect;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public int getPortToBind() {
		return portToBind;
	}

	/**
	 * @return
	 */
	public int getPortToConnect() {
		return portToConnect;
	}

	/**
	 * @return
	 */
	public StartStopState getState() {
		return state;
	}

	/**
	 * @throws IOException
	 */
	public void start() throws IOException {
		state.setValue(StartStopState.STARTED);
	}

	/**
	 *
	 */
	public void stop() {
		state.setValue(StartStopState.STOPPED);
	}

	/**
	 * @param type
	 * @param hostToConnect
	 * @param portToConnect
	 * @param originatingHost
	 * @param originatingPort
	 * @return
	 * @throws ForwardingConfigurationException
	 *
	 */
	public ForwardingSocketChannel createForwardingSocketChannel(String type,
	                                                             String hostToConnect, int portToConnect, String originatingHost,
	                                                             int originatingPort) throws ForwardingConfigurationException {
		if(state.getValue() == StartStopState.STOPPED) {
			throw new ForwardingConfigurationException("The forwarding has been stopped");
		}

		if(!type.equals(ForwardingChannel.LOCAL_FORWARDING_CHANNEL) &&
		    !type.equals(ForwardingChannel.REMOTE_FORWARDING_CHANNEL) &&
		    !type.equals(ForwardingChannel.X11_FORWARDING_CHANNEL)) {
			throw new ForwardingConfigurationException("The channel type must either be "+
			    "ForwardingSocketChannel.LOCAL_FORWARDING_CHANNEL_TYPE or "+
			    "ForwardingSocketChannel.REMOTE_FORWARDING_CHANNEL_TYPE");
		}

		ForwardingSocketChannel channel;

		if(type.equals(ForwardingChannel.LOCAL_FORWARDING_CHANNEL)) {
			channel = new ForwardingSocketChannel(type, name, hostToConnect,
			    portToConnect, originatingHost, originatingPort);
		}
		else {
			channel = new ForwardingSocketChannel(type, name,
			    getAddressToBind(), getPortToBind(), originatingHost,
			    originatingPort);
		}

		channel.addEventListener(monitor);

		return channel;
	}

	/**
	 * @param type
	 * @param hostToConnect
	 * @param portToConnect
	 * @param originatingHost
	 * @param originatingPort
	 * @return
	 * @throws ForwardingConfigurationException
	 *
	 */
	public ForwardingIOChannel createForwardingIOChannel(String type,
	                                                     String hostToConnect, int portToConnect, String originatingHost,
	                                                     int originatingPort) throws ForwardingConfigurationException {
		if(state.getValue() == StartStopState.STOPPED) {
			throw new ForwardingConfigurationException("The forwarding has been stopped");
		}

		if(!type.equals(ForwardingChannel.LOCAL_FORWARDING_CHANNEL) &&
		    !type.equals(ForwardingChannel.REMOTE_FORWARDING_CHANNEL) &&
		    !type.equals(ForwardingChannel.X11_FORWARDING_CHANNEL)) {
			throw new ForwardingConfigurationException("The channel type must either be "+
			    "ForwardingSocketChannel.LOCAL_FORWARDING_CHANNEL_TYPE or "+
			    "ForwardingSocketChannel.REMOTE_FORWARDING_CHANNEL_TYPE");
		}

		ForwardingIOChannel channel;

		if(type.equals(ForwardingChannel.LOCAL_FORWARDING_CHANNEL)) {
			channel = new ForwardingIOChannel(type, name, getHostToConnect(),
			    getPortToConnect(), originatingHost, originatingPort);
		}
		else {
			channel = new ForwardingIOChannel(type, name, getAddressToBind(),
			    getPortToBind(), originatingHost, originatingPort);
		}

		channel.addEventListener(monitor);

		return channel;
	}

	/**
	 * @param type
	 * @param hostToConnect
	 * @param portToConnect
	 * @param originatingHost
	 * @param originatingPort
	 * @return
	 * @throws ForwardingConfigurationException
	 *
	 */
	public ForwardingBindingChannel createForwardingBindingChannel(String type, String hostToConnect, int portToConnect,
	                                                               String originatingHost, int originatingPort)
	    throws ForwardingConfigurationException {
		if(state.getValue() == StartStopState.STOPPED) {
			throw new ForwardingConfigurationException("The forwarding has been stopped");
		}

		if(!type.equals(ForwardingChannel.LOCAL_FORWARDING_CHANNEL) &&
		    !type.equals(ForwardingChannel.REMOTE_FORWARDING_CHANNEL) &&
		    !type.equals(ForwardingChannel.X11_FORWARDING_CHANNEL)) {
			throw new ForwardingConfigurationException("The channel type must either be "+
			    "ForwardingSocketChannel.LOCAL_FORWARDING_CHANNEL_TYPE or "+
			    "ForwardingSocketChannel.REMOTE_FORWARDING_CHANNEL_TYPE");
		}

		ForwardingBindingChannel channel;

		if(type.equals(ForwardingChannel.LOCAL_FORWARDING_CHANNEL)) {
			channel = new ForwardingBindingChannel(type, name,
			    getHostToConnect(), getPortToConnect(), originatingHost,
			    originatingPort);
		}
		else {
			channel = new ForwardingBindingChannel(type, name,
			    getAddressToBind(), getPortToBind(), originatingHost,
			    originatingPort);
		}

		channel.addEventListener(monitor);

		return channel;
	}

	public class ForwardingConfigurationMonitor implements ChannelEventListener {
		public void onChannelOpen(Channel channel) {
			if(log.isDebugEnabled()) {
				ForwardingChannel fch = (ForwardingChannel)channel;
				log.debug("Opening forwarding channel from "+
				    fch.getOriginatingHost()+":"+
				    String.valueOf(fch.getOriginatingPort()));
			}

			// Add channel to the active forwardings
			activeForwardings.add(channel);

			ForwardingConfigurationListener[] l = (ForwardingConfigurationListener[])listenerList.getListeners(ForwardingConfigurationListener.class);

			for(int i = (l.length-1); i >= 0; i--) {
				l[i].opened(ForwardingConfiguration.this,
				    (ForwardingSocketChannel)channel);
			}
		}

		public void onChannelEOF(Channel channel) {
			// Close the OutputStream to force the channel to close

			/* try {
			   //channel.getOutputStream().close();
			   //channel.getInputStream().close();
			   channel.close();
			 }
			 catch (IOException ex) {
			 }*/
		}

		public void onChannelClose(Channel channel) {
			if(log.isDebugEnabled()) {
				ForwardingChannel fch = (ForwardingChannel)channel;
				log.debug("Closing forwarding channel from "+
				    fch.getOriginatingHost()+":"+
				    String.valueOf(fch.getOriginatingPort()));
			}

			// Remove channel from the active forwardings
			activeForwardings.remove(channel);

			ForwardingConfigurationListener[] l = (ForwardingConfigurationListener[])listenerList.getListeners(ForwardingConfigurationListener.class);

			for(int i = (l.length-1); i >= 0; i--) {
				l[i].closed(ForwardingConfiguration.this,
				    (ForwardingSocketChannel)channel);
			}
		}

		public void onDataReceived(Channel channel, byte[] data) {
			ForwardingConfigurationListener[] l = (ForwardingConfigurationListener[])listenerList.getListeners(ForwardingConfigurationListener.class);

			for(int i = (l.length-1); i >= 0; i--) {
				l[i].dataReceived(ForwardingConfiguration.this,
				    (ForwardingSocketChannel)channel, data.length);
			}
		}

		public void onDataSent(Channel channel, byte[] data) {
			ForwardingConfigurationListener[] l = (ForwardingConfigurationListener[])listenerList.getListeners(ForwardingConfigurationListener.class);

			for(int i = (l.length-1); i >= 0; i--) {
				l[i].dataSent(ForwardingConfiguration.this,
				    (ForwardingSocketChannel)channel, data.length);
			}
		}
	}
}
