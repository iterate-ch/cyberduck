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
public class SshMsgUserAuthInfoRequest extends SshMessage {
	/**  */
	public static final int SSH_MSG_USERAUTH_INFO_REQUEST = 60;
	private String name;
	private String instruction;
	private String langtag;
	private KBIPrompt[] prompts;

	/**
	 * Creates a new SshMsgUserAuthInfoRequest object.
	 */
	public SshMsgUserAuthInfoRequest() {
		super(SSH_MSG_USERAUTH_INFO_REQUEST);
	}

	/**
	 * Creates a new SshMsgUserAuthInfoRequest object.
	 *
	 * @param name
	 * @param instruction
	 * @param langtag
	 */
	public SshMsgUserAuthInfoRequest(String name, String instruction,
	                                 String langtag) {
		super(SSH_MSG_USERAUTH_INFO_REQUEST);
		this.name = name;
		this.instruction = instruction;
		this.langtag = langtag;
	}

	/**
	 * @param prompt
	 * @param echo
	 */
	public void addPrompt(String prompt, boolean echo) {
		if(prompts == null) {
			prompts = new KBIPrompt[1];
			prompts[0] = new KBIPrompt(prompt, echo);
		}
		else {
			KBIPrompt[] temp = new KBIPrompt[prompts.length+1];
			System.arraycopy(prompts, 0, temp, 0, prompts.length);
			prompts = temp;
			prompts[prompts.length-1] = new KBIPrompt(prompt, echo);
		}
	}

	/**
	 * @return
	 */
	public KBIPrompt[] getPrompts() {
		return prompts;
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
	public String getInstruction() {
		return instruction;
	}

	/**
	 * @return
	 */
	public String getLanguageTag() {
		return langtag;
	}

	/**
	 * @return
	 */
	public String getMessageName() {
		return "SSH_MSG_USERAUTH_INFO_REQUEST";
	}

	/**
	 * @param baw
	 * @throws com.sshtools.j2ssh.transport.InvalidMessageException
	 *                                 DOCUMENT
	 *                                 ME!
	 * @throws InvalidMessageException
	 */
	protected void constructByteArray(ByteArrayWriter baw)
	    throws com.sshtools.j2ssh.transport.InvalidMessageException {
		try {
			if(name != null) {
				baw.writeString(name);
			}
			else {
				baw.writeString("");
			}

			if(instruction != null) {
				baw.writeString(instruction);
			}
			else {
				baw.writeString("");
			}

			if(langtag != null) {
				baw.writeString(langtag);
			}
			else {
				baw.writeString("");
			}

			if(prompts == null) {
				baw.writeInt(0);
			}
			else {
				baw.writeInt(prompts.length);

				for(int i = 0; i < prompts.length; i++) {
					baw.writeString(prompts[i].getPrompt());
					baw.write(prompts[i].echo() ? 1 : 0);
				}
			}
		}
		catch(IOException ioe) {
			throw new InvalidMessageException("Failed to write message data");
		}
	}

	/**
	 * @param bar
	 * @throws com.sshtools.j2ssh.transport.InvalidMessageException
	 *                                 DOCUMENT
	 *                                 ME!
	 * @throws InvalidMessageException
	 */
	protected void constructMessage(ByteArrayReader bar)
	    throws com.sshtools.j2ssh.transport.InvalidMessageException {
		try {
			name = bar.readString();
			instruction = bar.readString();
			langtag = bar.readString();

			long num = bar.readInt();
			String prompt;
			boolean echo;

			for(int i = 0; i < num; i++) {
				prompt = bar.readString();
				echo = (bar.read() == 1);
				addPrompt(prompt, echo);
			}
		}
		catch(IOException ioe) {
			throw new InvalidMessageException("Failed to read message data");
		}
	}
}
