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
package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshFxpStatus extends SubsystemMessage implements MessageRequestId {
	/**  */
	public static final int SSH_FXP_STATUS = 101;

	/**  */
	public static final int STATUS_FX_OK = 0;

	/**  */
	public static final int STATUS_FX_EOF = 1;

	/**  */
	public static final int STATUS_FX_NO_SUCH_FILE = 2;

	/**  */
	public static final int STATUS_FX_PERMISSION_DENIED = 3;

	/**  */
	public static final int STATUS_FX_FAILURE = 4;

	/**  */
	public static final int STATUS_FX_BAD_MESSAGE = 5;

	/**  */
	public static final int STATUS_FX_NO_CONNECTION = 6;

	/**  */
	public static final int STATUS_FX_CONNECTION_LOST = 7;

	/**  */
	public static final int STATUS_FX_OP_UNSUPPORTED = 8;

	//public static final int STATUS_FX_INVALID_HANDLE = 9;
	//public static final int STATUS_FX_NO_SUCH_PATH = 10;
	//public static final int STATUS_FX_FILE_ALREADY_EXISTS = 11;
	//public static final int STATUS_FX_WRITE_PROTECT = 12;
	private UnsignedInteger32 id;
	private UnsignedInteger32 errorCode;
	private String errorMessage;
	private String languageTag;

	/**
	 * Creates a new SshFxpStatus object.
	 *
	 * @param id
	 * @param errorCode
	 * @param errorMessage
	 * @param languageTag
	 */
	public SshFxpStatus(UnsignedInteger32 id, UnsignedInteger32 errorCode,
	                    String errorMessage, String languageTag) {
		super(SSH_FXP_STATUS);
		this.id = id;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.languageTag = languageTag;
	}

	/**
	 * Creates a new SshFxpStatus object.
	 */
	public SshFxpStatus() {
		super(SSH_FXP_STATUS);
	}

	/**
	 * @return
	 */
	public UnsignedInteger32 getId() {
		return id;
	}

	/**
	 * @return
	 */
	public UnsignedInteger32 getErrorCode() {
		return errorCode;
	}

	/**
	 * @return
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @return
	 */
	public String getLanguageTag() {
		return languageTag;
	}

	/**
	 * @param bar
	 * @throws java.io.IOException
	 * @throws com.sshtools.j2ssh.transport.InvalidMessageException
	 *                             DOCUMENT
	 *                             ME!
	 */
	public void constructMessage(ByteArrayReader bar)
	    throws java.io.IOException,
	    com.sshtools.j2ssh.transport.InvalidMessageException {
		id = bar.readUINT32();
		errorCode = bar.readUINT32();
		errorMessage = bar.readString();
		languageTag = bar.readString();
	}

	/**
	 * @return
	 */
	public String getMessageName() {
		return "SSH_FXP_STATUS";
	}

	/**
	 * @param baw
	 * @throws java.io.IOException
	 * @throws com.sshtools.j2ssh.transport.InvalidMessageException
	 *                             DOCUMENT
	 *                             ME!
	 */
	public void constructByteArray(ByteArrayWriter baw)
	    throws java.io.IOException,
	    com.sshtools.j2ssh.transport.InvalidMessageException {
		baw.writeUINT32(id);
		baw.writeUINT32(errorCode);
		baw.writeString(errorMessage);
		baw.writeString(languageTag);
	}
}
