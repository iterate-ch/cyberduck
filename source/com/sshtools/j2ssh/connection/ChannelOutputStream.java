/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.connection;

import java.io.IOException;
import java.io.OutputStream;

import com.sshtools.j2ssh.transport.TransportProtocolException;

/**
 * This class implements a <code>OutputStream</code> for a connection protocol
 * channel.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class ChannelOutputStream extends OutputStream {
	private Channel channel;
	private boolean isClosed = false;
	private Integer type = null;
	/**
	 * Creates a new ChannelOutputStream object.
	 *
	 * @param channel the owner of this OutputStream
	 * @param type the extended data type
	 */
	public ChannelOutputStream(Channel channel, Integer type) {
		this.channel = channel;
		this.type = type;
	}

	/**
	 * Creates a new ChannelOutputStream object.
	 *
	 * @param channel the owner of this OutputStream
	 */
	public ChannelOutputStream(Channel channel) {
		this(channel, null);
	}

	/**
	 * Closes the OutputStream
	 *
	 * @throws IOException if an IO error occurs
	 */
	public void close() throws IOException {
		isClosed = true;
	}

	/**
	 * Writes a number of bytes to the OutputStream
	 *
	 * @param b the byte array to write
	 * @param off the offset starting position in the array
	 * @param len the number of bytes to write
	 *
	 * @throws IOException if an IO error occurs
	 */
	public void write(byte b[], int off, int len) throws IOException {

		if (isClosed)
			throw new IOException("The ChannelOutputStream is closed!");

		byte data[] = null;

		if ((off > 0) || (len < b.length)) {
			data = new byte[len];
			System.arraycopy(b, off, data, 0, len);
		} else {
			data = b;
		}

		sendChannelData(data);

	}

	/**
	 * Writes an individual byte to the OutputStream
	 *
	 * @param b the byte value to write
	 *
	 * @throws IOException if an IO error occurs
	 */
	public void write(int b) throws IOException {
		if (isClosed) {
			throw new IOException("The ChannelOutputStream is closed!");
		}

		byte data[] = new byte[1];
		data[0] = (byte) b;
		sendChannelData(data);
	}

	/**
	 * Send channel data, waiting for window space if its required
	 * @param data  the data to send
	 * @throws TransportProtocolException if a transport protocol error occurs
	 */
	private void sendChannelData(byte data[]) throws IOException {

		int sent = 0;
		int block;
		int remaining;
		long max;
		byte buffer[];
		ChannelDataWindow window = channel.getRemoteWindow();
		/**
		 * We will send the data in chunks of the lesser value of the
		 * available window size and the maximum packet size
		 */
		while (sent < data.length) {
			remaining = data.length - sent;
			max =
				window.getWindowSpace() < channel.getRemotePacketSize()
					? window.getWindowSpace()
					: channel.getRemotePacketSize();
			block = max < remaining ? (int) max : remaining;
			channel.remoteWindow.consumeWindowSpace(block);
			buffer = new byte[block];
			System.arraycopy(data, sent, buffer, 0, block);
			if (type != null)
				channel.sendChannelExtData(type.intValue(), buffer);
			else
				channel.sendChannelData(buffer);
			sent += block;

		}

	}
}
