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

import com.sshtools.j2ssh.io.IOStreamConnector;
import com.sshtools.j2ssh.transport.MessageNotAvailableException;
import com.sshtools.j2ssh.transport.MessageStoreEOFException;
import com.sshtools.j2ssh.transport.SshMessageStore;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author $author$
 * @version $Revision$
 */
public abstract class IOChannel extends Channel {
	private static Logger log = Logger.getLogger(IOChannel.class);

	/**  */
	private SshMessageStore incoming = new SshMessageStore();

	/**  */
	protected ChannelInputStream in;

	/**  */
	protected ChannelOutputStream out;

	/**  */
	protected InputStream boundInputStream = null;

	/**  */
	protected OutputStream boundOutputStream = null;

	//protected IOChannel boundIOChannel = null;

	/**  */
	protected IOStreamConnector ios = null;

	/**
	 * @param connection
	 * @param localChannelId
	 * @param senderChannelId
	 * @param initialWindowSize
	 * @param maximumPacketSize
	 * @throws IOException
	 */
	protected void init(ConnectionProtocol connection, long localChannelId,
	                    long senderChannelId, long initialWindowSize, long maximumPacketSize)
	    throws IOException {
		this.in = new ChannelInputStream(incoming); //ChannelInputStream.createStandard(incoming);
		this.out = new ChannelOutputStream(this);
		super.init(connection, localChannelId, senderChannelId,
		    initialWindowSize, maximumPacketSize);
	}

	/**
	 * @throws IOException
	 */
	protected void open() throws IOException {
		super.open();

		// If were bound send any outstanding messages sitting around
		if(boundOutputStream != null) {
			sendOutstandingMessages();
		}

		// Start the bound inputstream
		if((boundInputStream != null) && (ios == null)) {
			ios.setCloseInput(false);
			ios.setCloseOutput(false);
			ios.connect(boundInputStream, out);
		}
	}

	/**
	 * @return
	 */
	public ChannelInputStream getInputStream() {
		return in;
	}

	/**
	 * @return
	 */
	public ChannelOutputStream getOutputStream() {
		return out;
	}

	/**
	 * @param msg
	 * @throws IOException
	 */
	protected void onChannelData(SshMsgChannelData msg)
	    throws IOException {
		// Synchronize on the message store to ensure that another thread
		// does not try to read its data. This will make sure that the incoming
		// messages are not being flushed to an outputstream after a bind
		synchronized(incoming) {
			if(boundOutputStream != null) {
				try {
					boundOutputStream.write(msg.getChannelData());
				}
				catch(IOException ex) {
					log.info("Could not route data to the bound OutputStream; Closing channel.");
					log.info(ex.getMessage());
					close();
				}
			}
			else {
				incoming.addMessage(msg);
			}
		}
	}

	/**
	 * @throws IOException
	 */
	public void setLocalEOF() throws IOException {
		super.setLocalEOF();

		if(!out.isClosed()) {
			out.close();
		}
	}

	/**
	 * @throws IOException
	 */
	protected void onChannelEOF() throws IOException {
		if(!in.isClosed()) {
			in.close();
		}
	}

	/**
	 * @throws IOException
	 */
	protected void onChannelClose() throws IOException {
		// Close the input/output streams
		if(!in.isClosed()) {
			in.close();
		}

		if(!out.isClosed()) {
			out.close();
		}

		// Close the bound channel

		/* if(boundIOChannel!=null && !boundIOChannel.isClosed())
		     boundIOChannel.close();*/

		// Close the IOStream connector if were bound
		if(ios != null) {
			ios.close();
		}
	}

	/**
	 * @param msg
	 * @throws IOException
	 */
	protected void onChannelExtData(SshMsgChannelExtendedData msg)
	    throws IOException {
		// This class will not deal with extended data
		// incoming.addMessage(msg);
	}

	/*public void bindIOChannel(IOChannel boundIOChannel) throws IOException {
	   this.boundIOChannel = boundIOChannel;
	    // If the bound channel is open then bind the outputstreams
	    if (boundIOChannel.getState().getValue() == ChannelState.CHANNEL_OPEN) {
	 throw new IOException("You cannot bind to an open channel");
	    }
	    // Create an event listener so we can listen
	    boundIOChannel.addEventListener(new ChannelEventListener() {
	 public void onChannelOpen(Channel channel) {
	     try {
	       bindOutputStream(IOChannel.this.boundIOChannel.getOutputStream());
	       IOChannel.this.boundIOChannel.bindOutputStream(getOutputStream());
	     }
	     catch (IOException ex) {
	       log.info("Failed to bind the channel");
	     }
	 }
	 public void onChannelEOF(Channel channel) {
	   try {
	       //setLocalEOF();
	       close();
	   }
	   catch (IOException ex) {
	     log.info("Failed to set the channel to EOF");
	   }
	 }
	 public void onChannelClose(Channel channel)  {
	   try {
	     if(!isClosed())
	       close();
	   }
	   catch (IOException ex) {
	     log.info("Failed to close the channel");
	   }
	 }
	 public void onDataReceived(Channel channel, byte[] data) {
	 }
	 public void onDataSent(Channel channel, byte[] data) {
	 }
	    });
	 }*/
	public void bindOutputStream(OutputStream boundOutputStream)
	    throws IOException {
		// Synchronize on the incoming message store to ensure that no other
		// messages are added whilst we transfer to a bound state
		synchronized(incoming) {
			this.boundOutputStream = boundOutputStream;

			if(state.getValue() == ChannelState.CHANNEL_OPEN) {
				sendOutstandingMessages();
			}
		}
	}

	/**
	 * @param boundInputStream
	 * @throws IOException
	 */
	public void bindInputStream(InputStream boundInputStream)
	    throws IOException {
		this.boundInputStream = boundInputStream;
		this.ios = new IOStreamConnector();

		if(state.getValue() == ChannelState.CHANNEL_OPEN) {
			ios.setCloseInput(false);
			ios.setCloseOutput(false);
			ios.connect(boundInputStream, out);
		}
	}

	private void sendOutstandingMessages() throws IOException {
		if((boundInputStream != null) && (boundOutputStream != null) &&
		    incoming.hasMessages()) {
			while(true) {
				try {
					// Peek into the message store and look for the next message
					SshMsgChannelData msg = (SshMsgChannelData)incoming.peekMessage(SshMsgChannelData.SSH_MSG_CHANNEL_DATA);

					// Remove the message so we dont process again
					incoming.removeMessage(msg);

					// Write the message out to the bound OutputStream
					try {
						boundOutputStream.write(msg.getChannelData());
					}
					catch(IOException ex1) {
						//log.info("Could not write outstanding messages to the bound OutputStream: "  +ex1.getMessage());
						close();
					}
				}
				catch(MessageStoreEOFException ex) {
					break;
				}
				catch(MessageNotAvailableException ex) {
					break;
				}
				catch(InterruptedException ex) {
                    throw new IOException(ex.getMessage());
				}
			}
		}
	}
}
