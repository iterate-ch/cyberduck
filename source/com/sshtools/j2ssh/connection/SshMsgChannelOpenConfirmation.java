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

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;

import java.io.IOException;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshMsgChannelOpenConfirmation extends SshMessage {
	/**  */
	protected final static int SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 91;
	private byte[] channelData;
	private long initialWindowSize;
	private long maximumPacketSize;
	private long recipientChannel;
	private long senderChannel;

	/**
	 * Creates a new SshMsgChannelOpenConfirmation object.
	 *
	 * @param recipientChannel
	 * @param senderChannel
	 * @param initialWindowSize
	 * @param maximumPacketSize
	 * @param channelData
	 */
	public SshMsgChannelOpenConfirmation(long recipientChannel,
	                                     long senderChannel, long initialWindowSize, long maximumPacketSize,
	                                     byte[] channelData) {
		super(SSH_MSG_CHANNEL_OPEN_CONFIRMATION);
		this.recipientChannel = recipientChannel;
		this.senderChannel = senderChannel;
		this.initialWindowSize = initialWindowSize;
		this.maximumPacketSize = maximumPacketSize;
		this.channelData = channelData;
	}

	/**
	 * Creates a new SshMsgChannelOpenConfirmation object.
	 */
	public SshMsgChannelOpenConfirmation() {
		super(SSH_MSG_CHANNEL_OPEN_CONFIRMATION);
	}

	/**
	 * @return
	 */
	public byte[] getChannelData() {
		return channelData;
	}

	/**
	 * @return
	 */
	public long getInitialWindowSize() {
		return initialWindowSize;
	}

	/**
	 * @return
	 */
	public long getMaximumPacketSize() {
		return maximumPacketSize;
	}

	/**
	 * @return
	 */
	public String getMessageName() {
		return "SSH_MSG_CHANNEL_OPEN_CONFIRMATION";
	}

	/**
	 * @return
	 */
	public long getRecipientChannel() {
		return recipientChannel;
	}

	/**
	 * @return
	 */
	public long getSenderChannel() {
		return senderChannel;
	}

	/**
	 * @param baw
	 * @throws InvalidMessageException
	 */
	protected void constructByteArray(ByteArrayWriter baw)
	    throws InvalidMessageException {
		try {
			baw.writeInt(recipientChannel);
			baw.writeInt(senderChannel);
			baw.writeInt(initialWindowSize);
			baw.writeInt(maximumPacketSize);

			if(channelData != null) {
				baw.write(channelData);
			}
		}
		catch(IOException ioe) {
			throw new InvalidMessageException("Invalid message data");
		}
	}

	/**
	 * @param bar
	 * @throws InvalidMessageException
	 */
	protected void constructMessage(ByteArrayReader bar)
	    throws InvalidMessageException {
		try {
			recipientChannel = bar.readInt();
			senderChannel = bar.readInt();
			initialWindowSize = bar.readInt();
			maximumPacketSize = bar.readInt();

			if(bar.available() > 0) {
				channelData = new byte[bar.available()];
				bar.read(channelData);
			}
		}
		catch(IOException ioe) {
			throw new InvalidMessageException("Invalid message data");
		}
	}
}
