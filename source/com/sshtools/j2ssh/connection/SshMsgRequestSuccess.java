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

import java.io.IOException;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshMsgRequestSuccess extends SshMessage {
	/**  */
	protected final static int SSH_MSG_REQUEST_SUCCESS = 81;
	private byte[] requestData;

	/**
	 * Creates a new SshMsgRequestSuccess object.
	 *
	 * @param requestData
	 */
	public SshMsgRequestSuccess(byte[] requestData) {
		super(SSH_MSG_REQUEST_SUCCESS);
		this.requestData = requestData;
	}

	/**
	 * Creates a new SshMsgRequestSuccess object.
	 */
	public SshMsgRequestSuccess() {
		super(SSH_MSG_REQUEST_SUCCESS);
	}

	/**
	 * @return
	 */
	public String getMessageName() {
		return "SSH_MSG_REQUEST_SUCCESS";
	}

	/**
	 * @return
	 */
	public byte[] getRequestData() {
		return requestData;
	}

	/**
	 * @param baw
	 * @throws InvalidMessageException
	 */
	protected void constructByteArray(ByteArrayWriter baw)
	    throws InvalidMessageException {
		try {
			if(requestData != null) {
				baw.write(requestData);
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
			if(bar.available() > 0) {
				requestData = new byte[bar.available()];
				bar.read(requestData);
			}
		}
		catch(IOException ioe) {
			throw new InvalidMessageException("Invalid message data");
		}
	}
}
