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


/**
 * @author $author$
 * @version $Revision$
 */
public interface ChannelEventListener {
	/**
	 * @param channel
	 */
	public void onChannelOpen(Channel channel);

	/**
	 * @param channel
	 */
	public void onChannelEOF(Channel channel);

	/**
	 * @param channel
	 */
	public void onChannelClose(Channel channel);

	/**
	 * @param channel
	 * @param data
	 */
	public void onDataReceived(Channel channel, byte[] data);

	/**
	 * @param channel
	 * @param data
	 */
	public void onDataSent(Channel channel, byte[] data);
}
