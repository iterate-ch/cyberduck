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
package com.sshtools.j2ssh.authentication;

import java.io.IOException;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.SshMessage;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshMsgUserAuthPKOK extends SshMessage {
	/**  */
	public final static int SSH_MSG_USERAUTH_PK_OK = 60;
	private String algorithm;
	private byte[] key;

	//private boolean ok;

	/**
	 * Creates a new SshMsgUserAuthPKOK object.
	 */
	public SshMsgUserAuthPKOK() {
		super(SSH_MSG_USERAUTH_PK_OK);
	}

	/**
	 * Creates a new SshMsgUserAuthPKOK object.
	 *
	 * @param ok
	 * @param algorithm
	 * @param key
	 */
	public SshMsgUserAuthPKOK(/*boolean ok,*/
	    String algorithm, byte[] key) {
		super(SSH_MSG_USERAUTH_PK_OK);

		//this.ok = ok;
		this.algorithm = algorithm;
		this.key = key;
	}

	/**
	 *
	 *
	 * @return
	 */

	/*public boolean isOk() {
	  return ok;
	   }*/

	/**
	 * @return
	 */
	public String getMessageName() {
		return "SSH_MSG_USERAUTH_PK_OK";
	}

	/**
	 * @param baw
	 * @throws InvalidMessageException
	 */
	protected void constructByteArray(ByteArrayWriter baw)
	    throws InvalidMessageException {
		try {
			//baw.write(ok ? 1 : 0);
			baw.writeString(algorithm);
			baw.writeBinaryString(key);
		}
		catch(IOException ioe) {
			throw new InvalidMessageException("Failed to write message data!");
		}
	}

	/**
	 * @param bar
	 * @throws InvalidMessageException
	 */
	protected void constructMessage(ByteArrayReader bar)
	    throws InvalidMessageException {
		try {
			//ok = ((bar.read() == 1) ? true : false);
			algorithm = bar.readString();
			key = bar.readBinaryString();
		}
		catch(IOException ioe) {
			throw new InvalidMessageException("Failed to read message data!");
		}
	}
}
