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
package com.sshtools.j2ssh.session;

import com.sshtools.j2ssh.connection.ChannelInputStream;
import com.sshtools.j2ssh.connection.IOChannel;
import com.sshtools.j2ssh.connection.SshMsgChannelExtendedData;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.subsystem.SubsystemClient;
import com.sshtools.j2ssh.transport.SshMessageStore;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author $author$
 * @version $Revision$
 */
public class SessionChannelClient extends IOChannel {
	private static Logger log = Logger.getLogger(SessionChannelClient.class);
	private Integer exitCode = null;
	private String sessionType = "Uninitialized";
	private SubsystemClient subsystem;
	private boolean localFlowControl = false;
	private SignalListener signalListener;
	private SshMessageStore errorMessages = new SshMessageStore();
	private ChannelInputStream stderr = new ChannelInputStream(/*ChannelInputStream.createExtended(*/
	    errorMessages,
	    new Integer(SshMsgChannelExtendedData.SSH_EXTENDED_DATA_STDERR));

	//  new Integer(SshMsgChannelExtendedData.SSH_EXTENDED_DATA_STDERR));

	/**
	 * Creates a new SessionChannelClient object.
	 */
	public SessionChannelClient() {
		super();
		setName("session");
	}

	/**
	 * @return
	 */
	public byte[] getChannelOpenData() {
		return null;
	}

	/**
	 * @return
	 */
	public byte[] getChannelConfirmationData() {
		return null;
	}

	/**
	 * @return
	 */
	public String getChannelType() {
		return "session";
	}

	/**
	 * @return
	 */
	protected int getMinimumWindowSpace() {
		return 1024;
	}

	/**
	 * @return
	 */
	protected int getMaximumWindowSpace() {
		return 32648;
	}

	/**
	 * @return
	 */
	protected int getMaximumPacketSize() {
		return 32648;
	}

	/**
	 * @param signalListener
	 */
	public void setSignalListener(SignalListener signalListener) {
		this.signalListener = signalListener;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public boolean setEnvironmentVariable(String name, String value)
	    throws IOException {
		log.debug("Requesting environment variable to be set ["+name+"="+
		    value+"]");

		ByteArrayWriter baw = new ByteArrayWriter();
		baw.writeString(name);
		baw.writeString(value);

		return connection.sendChannelRequest(this, "env", true,
		    baw.toByteArray());
	}

	/**
	 * @param display
	 * @param cookie
	 * @return
	 * @throws IOException
	 */
	public boolean requestX11Forwarding(int display, String cookie)
	    throws IOException {
		log.debug("Requesting X11 forwarding for display "+display+
		    " using cookie "+cookie);

		ByteArrayWriter baw = new ByteArrayWriter();
		baw.writeBoolean(false);
		baw.writeString("MIT-MAGIC-COOKIE-1");
		baw.writeString(cookie);
		baw.writeUINT32(new UnsignedInteger32(String.valueOf(display)));

		return connection.sendChannelRequest(this, "x11-req", true,
		    baw.toByteArray());
	}

	/**
	 * @return
	 */
	public Integer getExitCode() {
		return exitCode;
	}

	/**
	 * @param term
	 * @throws IOException
	 */
	public void changeTerminalDimensions(PseudoTerminal term)
	    throws IOException {
		log.debug("Changing terminal dimensions");

		ByteArrayWriter baw = new ByteArrayWriter();
		baw.writeInt(term.getColumns());
		baw.writeInt(term.getRows());
		baw.writeInt(term.getWidth());
		baw.writeInt(term.getHeight());
		connection.sendChannelRequest(this, "window-change", false,
		    baw.toByteArray());
	}

	/**
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public boolean executeCommand(String command) throws IOException {
		log.info("Requesting command execution");
		log.debug("Command is "+command);

		ByteArrayWriter baw = new ByteArrayWriter();
		baw.writeString(command);

		if(connection.sendChannelRequest(this, "exec", true, baw.toByteArray())) {
			if(sessionType.equals("Uninitialized")) {
				sessionType = command;
			}

			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * @param term
	 * @param cols
	 * @param rows
	 * @param width
	 * @param height
	 * @param terminalModes
	 * @return
	 * @throws IOException
	 */
	public boolean requestPseudoTerminal(String term, int cols, int rows,
	                                     int width, int height, String terminalModes) throws IOException {
		log.info("Requesting pseudo terminal");

		if(log.isDebugEnabled()) {
			log.debug("Terminal Type is "+term);
			log.debug("Columns="+String.valueOf(cols));
			log.debug("Rows="+String.valueOf(rows));
			log.debug("Width="+String.valueOf(width));
			log.debug("Height="+String.valueOf(height));
		}

		// This requests a pseudo terminal
		ByteArrayWriter baw = new ByteArrayWriter();
		baw.writeString(term);
		baw.writeInt(cols);
		baw.writeInt(rows);
		baw.writeInt(width);
		baw.writeInt(height);
		baw.writeString(terminalModes);

		return connection.sendChannelRequest(this, "pty-req", true,
		    baw.toByteArray());
	}

	/**
	 * @param term
	 * @return
	 * @throws IOException
	 */
	public boolean requestPseudoTerminal(PseudoTerminal term)
	    throws IOException {
		return requestPseudoTerminal(term.getTerm(), term.getColumns(),
		    term.getRows(), term.getWidth(), term.getHeight(),
		    term.getEncodedTerminalModes());
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public boolean startShell() throws IOException {
		log.debug("Requesting users shell");

		// Send the request for a shell, we want a reply
		if(connection.sendChannelRequest(this, "shell", true, null)) {
			if(sessionType.equals("Uninitialized")) {
				sessionType = "shell";
			}

			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * @param subsystem
	 * @return
	 * @throws IOException
	 */
	public boolean startSubsystem(String subsystem) throws IOException {
		log.info("Starting "+subsystem+" subsystem");

		ByteArrayWriter baw = new ByteArrayWriter();
		baw.writeString(subsystem);

		if(connection.sendChannelRequest(this, "subsystem", true,
		    baw.toByteArray())) {
			if(sessionType.equals("Uninitialized")) {
				sessionType = subsystem;
			}

			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * @param subsystem
	 * @return
	 * @throws IOException
	 */
	public boolean startSubsystem(SubsystemClient subsystem)
	    throws IOException {
		boolean result = startSubsystem(subsystem.getName());

		if(result) {
			this.subsystem = subsystem;
			subsystem.setSessionChannel(this);
			subsystem.start();
		}

		return result;
	}

	/**
	 * @return
	 */
	public boolean isLocalFlowControlEnabled() {
		return localFlowControl;
	}

	/**
	 * @return
	 */
	public String getSessionType() {
		return sessionType;
	}

	/**
	 * @param sessionType
	 */
	public void setSessionType(String sessionType) {
		this.sessionType = sessionType;
	}

	/**
	 * @return
	 */
	public SubsystemClient getSubsystem() {
		return subsystem;
	}

	/**
	 * @throws IOException
	 */
	protected void onChannelClose() throws IOException {
		super.onChannelClose();

		try {
			stderr.close();
		}
		catch(IOException ex) {
		}

		Integer exitCode = getExitCode();

		if(exitCode != null) {
			log.debug("Exit code "+exitCode.toString());
		}
	}

	/**
	 * @throws IOException
	 */
	protected void onChannelOpen() throws IOException {
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public InputStream getStderrInputStream() throws IOException {
		/*if (stderr == null) {
		    throw new IOException("The session must be started first!");
		         }*/
		return stderr;
	}

	/**
	 * @param msg
	 * @throws IOException
	 */
	protected void onChannelExtData(SshMsgChannelExtendedData msg)
	    throws IOException {
		errorMessages.addMessage(msg);
	}

	/**
	 * @param requestType
	 * @param wantReply
	 * @param requestData
	 * @throws IOException
	 */
	protected void onChannelRequest(String requestType, boolean wantReply,
	                                byte[] requestData) throws IOException {
		log.debug("Channel Request received: "+requestType);

		if(requestType.equals("exit-status")) {
			exitCode = new Integer((int)ByteArrayReader.readInt(requestData, 0));
			log.debug("Exit code of "+exitCode.toString()+" received");
		}
		else if(requestType.equals("exit-signal")) {
			ByteArrayReader bar = new ByteArrayReader(requestData);
			String signal = bar.readString();
			boolean coredump = bar.read() != 0;
			String message = bar.readString();
			String language = bar.readString();
			log.debug("Exit signal "+signal+" received");
			log.debug("Signal message: "+message);
			log.debug("Core dumped: "+String.valueOf(coredump));

			if(signalListener != null) {
				signalListener.onExitSignal(signal, coredump, message);
			}
		}
		else if(requestType.equals("xon-xoff")) {
			if(requestData.length >= 1) {
				localFlowControl = (requestData[0] != 0);
			}
		}
		else if(requestType.equals("signal")) {
			String signal = ByteArrayReader.readString(requestData, 0);
			log.debug("Signal "+signal+" received");

			if(signalListener != null) {
				signalListener.onSignal(signal);
			}
		}
		else {
			if(wantReply) {
				connection.sendChannelRequestFailure(this);
			}
		}
	}
}
