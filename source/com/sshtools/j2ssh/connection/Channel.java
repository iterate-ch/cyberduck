/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.connection;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;

import java.util.Map;

import java.io.IOException;

import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.SshMessageStore;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.util.InvalidStateException;

/**
 *  This abstract class implements a connection protocol channel.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public abstract class Channel {
    private static Logger log = Logger.getLogger(Channel.class);

    /**
     *  Manages the local window space
     */
    protected ChannelDataWindow localWindow = new ChannelDataWindow();

    /**
     *  Manages the remote window space
     */
    protected ChannelDataWindow remoteWindow = new ChannelDataWindow();

    /**
     *  The channels connection
     */
    protected ConnectionProtocol connection;

    /**
     *  Native settings for advanced platform use
     */
    protected Map nativeSettings;

    /**
     *  The incoming message store that receives channel messages
     */
    protected SshMessageStore incoming = new SshMessageStore();

    /**
     *  The channels id on the local computer
     */
    protected long localChannelId;

    /**
     *  The channels maximum packet size to receive
     */
    protected long localPacketSize;

    /**
     *  The channels id on the remote computer
     */
    protected long remoteChannelId;

    /**
     *  The channels maximum packet size to send
     */
    protected long remotePacketSize;
    private ChannelInputStream in;
    private ChannelOutputStream out;
    private ChannelState state = new ChannelState();
    private boolean isClosed = false;
    private boolean isLocalEOF = false;
    private boolean isRemoteEOF = false;


    /**
     *  The constructor for the Channel when the local side request the channel
     *  to be opened.
     */
    public Channel() {
        this.localPacketSize = getMaximumPacketSize();
        this.localWindow.increaseWindowSpace(getMaximumWindowSpace());
    }


    /**
     *  Gets the SSH_MSG_CHANNEL_OPEN message data.
     *
     *@return
     */
    public abstract byte[] getChannelOpenData();


    /**
     *  Gets the channel type.
     *
     *@return
     */
    public abstract String getChannelType();


    /**
     *  Get the minimum number of bytes of window space that should always be
     *  available. When this minimum is reached the connection protocol will
     *  automatically send a window adjust message.
     *
     *@return    the minimum number of window space bytes
     */
    protected abstract int getMinimumWindowSpace();


    /**
     *  Get the maximum number of bytes that should be available for window
     *  space
     *
     *@return    the maximum number of bytes for window space
     */
    protected abstract int getMaximumWindowSpace();


    /**
     *  Gets the maximum number of bytes the remote side can send at once
     *
     *@return    the maximum packet size
     */
    protected abstract int getMaximumPacketSize();


    /**
     *  Called by the framework when channel data arrives
     *
     *@param  msg                          The new channel data message
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if no window space is available for
     *      the data
     */
    public void onChannelData(SshMsgChannelData msg)
             throws IOException {

        if (msg.getChannelDataLength() > localWindow.getWindowSpace()) {
            throw new ServiceOperationException("More data recieved than is allowed by the channel data window");
        }

        long windowSpace = localWindow.consumeWindowSpace(msg.getChannelData().length);

        if (windowSpace < getMinimumWindowSpace()) {
            log.debug("Channel " + String.valueOf(localChannelId) + " requires more window space");
            windowSpace = getMaximumWindowSpace() - windowSpace;
            connection.sendChannelWindowAdjust(this, windowSpace);
            localWindow.increaseWindowSpace(windowSpace);
        }

        incoming.addMessage(msg);
    }

    /**
     * Sends data to the remote side through the parent connection
     * @param data    the data to send
     * @throws TransportProtocolException if a protocol error occurs
     */
    protected void sendChannelData(byte data[]) throws IOException {
      connection.sendChannelData(this, data);
    }

    /**
     * Sends extended data to the remote side through the parent connection
     * @param type  the type of extended data
     * @param data  the data to send
     * @throws TransportProtocolException if a protocol error occurs
     */
    protected void sendChannelExtData(int type, byte data[])
                                      throws IOException {
      connection.sendChannelExtData(this,type,data);
    }
    /**
     *  Called by the framework to add extended data to the channel
     *
     *@param  msg                          the extended data message
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if window space is not available
     */
    public void onChannelExtData(SshMsgChannelExtendedData msg)
             throws IOException {

        if (msg.getChannelData().length > localWindow.getWindowSpace()) {
            throw new ServiceOperationException("More data recieved than is allowed by the channel data window");
        }

        long windowSpace = localWindow.consumeWindowSpace(msg.getChannelData().length);

        if (windowSpace < getMinimumWindowSpace()) {
            log.debug("Channel " + String.valueOf(localChannelId) + " requires more window space");
            windowSpace = getMaximumWindowSpace() - windowSpace;
            connection.sendChannelWindowAdjust(this, windowSpace);
            localWindow.increaseWindowSpace(windowSpace);
        }

        incoming.addMessage(msg);
    }


    /**
     *  Gets the <code>ChannelInputStream</code> for this channel
     *
     *@return    the channels input stream
     */
    public InputStream getInputStream() {
        return in;
    }


    /**
     *  Gets the local channel id.
     *
     *@return    the channel id number
     */
    public long getLocalChannelId() {
        return localChannelId;
    }


    /**
     *  Gets the local maximum packet size.
     *
     *@return    the total bytes of packet data that can be received at once
     */
    public long getLocalPacketSize() {
        return localPacketSize;
    }


    /**
     *  Returns the current local window.
     *
     *@return    the channels local window
     */
    public synchronized ChannelDataWindow getLocalWindow() {
        return localWindow;
    }


    /**
     *  Gets the <code>ChannelOutputStream</code> for this channel
     *
     *@return    the channels output stream
     */
    public OutputStream getOutputStream() {
        return out;
    }


    /**
     *  Gets the remote channel id.
     *
     *@return    the remote channel id number
     */
    public long getRemoteChannelId() {
        return remoteChannelId;
    }


    /**
     *  Gets the remote maximum packet size.
     *
     *@return    the maximum size of packet that the remote computer will accept
     */
    public long getRemotePacketSize() {
        return remotePacketSize;
    }


    /**
     *  Gets the current remote window.
     *
     *@return    the remote computers window
     */
    public synchronized ChannelDataWindow getRemoteWindow() {
        return remoteWindow;
    }


    /**
     *  Gets the <code>ChannelState</code> instance for this channel.
     *
     *@return    a <code>State</code> object representing the current state of
     *      the channel
     */
    public ChannelState getState() {
        return state;
    }


    /**
     *  Closes the channel
     *
     *@throws  TransportProtocolException  if an error occurs in the Transport
     *      Protocol
     *@throws  ServiceOperationException   if a critical service operation error
     *      occurs
     */
    public void close()
             throws IOException {

        state.setValue(ChannelState.CHANNEL_CLOSED);

        onChannelClose();

        connection.closeChannel(this);

    }


    /**
     *  Initiates the channel
     *
     *@param  connection                  The connection protocol parent
     *      instance
     *@param  nativeSettings              A Map of name/value pairs
     *@param  localChannelId              The local channel id for this channel
     *@param  senderChannelId             The remote channel id for this channel
     *@param  initialWindowSize           The initial remote windowsize
     *@param  maximumPacketSize           The maximum packet size
     *@throws  ServiceOperationException  if the channel fails to initialize
     */
    public void init(ConnectionProtocol connection, Map nativeSettings,
            long localChannelId, long senderChannelId,
            long initialWindowSize, long maximumPacketSize)
             throws IOException {
        this.localChannelId = localChannelId;
        this.remoteChannelId = senderChannelId;
        this.remotePacketSize = maximumPacketSize;
        this.remoteWindow.increaseWindowSpace(initialWindowSize);
        this.connection = connection;
        this.nativeSettings = nativeSettings;
        this.in = new ChannelInputStream(incoming);
        this.out = new ChannelOutputStream(this);

        onChannelOpen();
    }


    /**
     *  Implement this method to perform channel specific operation when the
     *  channel closes
     *
     *@exception  ServiceOperationException  if any criticial service operation
     *      fails
     */
    protected abstract void onChannelClose()
             throws IOException;


    /**
     *  Implements this method to perform channel specific operations when the
     *  channel has been opened.
     *
     *@exception  ServiceOperationException  if any critical service operation
     *      occurs
     */
    protected abstract void onChannelOpen()
             throws IOException;


    /**
     *  Implement this method to handle channel requests.
     *
     *@param  requestType                    The request type
     *@param  wantReply                      <tt>true</tt> if the remote side
     *      requires a reply. Use <code>sendChannelRequestSuccess</code> or
     *      <code>sendChannelRequestFailure</code>
     *@param  requestData                    The request specific data
     *@exception  ServiceOperationException  if any critical service operation
     *      fails
     */
    protected abstract void onChannelRequest(String requestType,
            boolean wantReply,
            byte requestData[])
             throws IOException;
}
