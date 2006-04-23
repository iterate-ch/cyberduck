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
package com.sshtools.j2ssh.io;

import com.sshtools.j2ssh.SshThread;

import org.apache.log4j.Logger;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author $author$
 * @version $Revision$
 */
public class IOStreamConnector {
	private static Logger log = Logger.getLogger(IOStreamConnector.class);
	private IOStreamConnectorState state = new IOStreamConnectorState();
	private InputStream in = null;
	private OutputStream out = null;
	private Thread thread;
	private long bytes;
	private boolean closeInput = true;
	private boolean closeOutput = true;

	/**  */
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * Creates a new IOStreamConnector object.
	 */
	public IOStreamConnector() {
	}

	/**
	 * Creates a new IOStreamConnector object.
	 *
	 * @param in
	 * @param out
	 */
	public IOStreamConnector(InputStream in, OutputStream out) {
		connect(in, out);
	}

	/**
	 * @return
	 */
	public IOStreamConnectorState getState() {
		return state;
	}

	/**
	 * @throws IOException
	 */
	public void close() throws IOException {
		log.info("Closing IOStreamConnector");
		state.setValue(IOStreamConnectorState.CLOSED);

		if(closeInput) {
			in.close();
		}

		if(closeOutput) {
			out.close();
		}

		thread = null;
	}

	/**
	 * @param closeInput
	 */
	public void setCloseInput(boolean closeInput) {
		this.closeInput = closeInput;
	}

	/**
	 * @param closeOutput
	 */
	public void setCloseOutput(boolean closeOutput) {
		this.closeOutput = closeOutput;
	}

	/**
	 * @param in
	 * @param out
	 */
	public void connect(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
		log.info("Connecting InputStream to OutputStream");
		state.setValue(IOStreamConnectorState.CONNECTED);
		thread = new SshThread(new IOStreamConnectorThread(),
		    "IOStream connector", true);
		thread.start();
	}

	/**
	 * @return
	 */
	public long getBytes() {
		return bytes;
	}

	/**
	 * @param l
	 */
	public void addIOStreamConnectorListener(IOStreamConnectorListener l) {
		listenerList.add(IOStreamConnectorListener.class, l);
	}

	/**
	 * @param l
	 */
	public void removeIOStreamConnectorListener(IOStreamConnectorListener l) {
		listenerList.remove(IOStreamConnectorListener.class, l);
	}

	class IOStreamConnectorThread implements Runnable {
		private Logger log = Logger.getLogger(IOStreamConnectorThread.class);

		public void run() {
			byte[] buffer = new byte[4096];
			int read = 0;
			int count;
			int available;
			log.info("Starting IOStreamConnectorThread thread");

			while(state.getValue() == IOStreamConnectorState.CONNECTED) {
				try {
					// Block
					read = in.read(buffer, 0, 1);

					if(read > 0) {
						count = read;
						available = in.available();

						// Verify the buffer length and adjust if necersary
						if((available > 0) &&
						    ((buffer.length-1) < available)) {
							byte[] tmp = new byte[available+1];
							System.arraycopy(buffer, 0, tmp, 0, 1);
							buffer = tmp;
						}

						// Read the remaining available bytes of the message
						if(available > 0) {
							read = in.read(buffer, 1, available);
							count += read;
						}

						// Write the message to the output stream
						out.write(buffer, 0, count);
						bytes += count;

						// Flush it
						out.flush();

						// Inform all of the listeners
						IOStreamConnectorListener[] l = (IOStreamConnectorListener[])listenerList.getListeners(IOStreamConnectorListener.class);

						for(int i = (l.length-1); i >= 0; i--) {
							l[i].data(buffer, count);
						}
					}
					else {
						log.debug("Blocking read returned with "+
						    String.valueOf(read));

						if(read < 0) {
							state.setValue(IOStreamConnectorState.EOF);
						}
					}
				}
				catch(IOException ioe) {
					// only warn if were supposed to be still connected, as we will ignore close exceptions
					if(state.getValue() == IOStreamConnectorState.CONNECTED) {
						log.debug(ioe.getMessage());
						state.setValue(IOStreamConnectorState.EOF);
					}
				}
			}

			try {
				// if were not already closed then close the connector
				if(state.getValue() != IOStreamConnectorState.CLOSED) {
					close();
				}
			}
			catch(IOException ioe) {
			}

			log.info("IOStreamConnectorThread is exiting");
		}
	}
}
