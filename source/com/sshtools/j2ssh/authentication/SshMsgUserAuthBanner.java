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
public class SshMsgUserAuthBanner extends SshMessage {
	/**  */
	public final static int SSH_MSG_USERAUTH_BANNER = 53;
	private String banner;
	private String languageTag;

	/**
	 * Creates a new SshMsgUserAuthBanner object.
	 */
	public SshMsgUserAuthBanner() {
		super(SSH_MSG_USERAUTH_BANNER);
	}

	/**
	 * Creates a new SshMsgUserAuthBanner object.
	 *
	 * @param banner
	 */
	public SshMsgUserAuthBanner(String banner) {
		super(SSH_MSG_USERAUTH_BANNER);
		this.banner = banner;
		this.languageTag = "";
	}

	/**
	 * @return
	 */
	public String getBanner() {
		return banner;
	}

	/**
	 * @return
	 */
	public String getLanguageTag() {
		return languageTag;
	}

	/**
	 * @return
	 */
	public String getMessageName() {
		return "SSH_MSG_USERAUTH_BANNER";
	}

	/**
	 * @param baw
	 * @throws InvalidMessageException
	 */
	protected void constructByteArray(ByteArrayWriter baw)
	    throws InvalidMessageException {
		try {
			baw.writeString(banner);
			baw.writeString(languageTag);
		}
		catch(IOException ioe) {
			throw new InvalidMessageException("Error writing the message data");
		}
	}

	/**
	 * @param bar
	 * @throws InvalidMessageException
	 */
	protected void constructMessage(ByteArrayReader bar)
	    throws InvalidMessageException {
		try {
			banner = bar.readString();
			languageTag = bar.readString();
		}
		catch(IOException ioe) {
			throw new InvalidMessageException("Error reading the message data");
		}
	}
}
