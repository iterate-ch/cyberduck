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

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sshtools.j2ssh.transport.MessageAlreadyRegisteredException;
import com.sshtools.j2ssh.transport.Service;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.util.InvalidStateException;
import com.sshtools.j2ssh.transport.MessageStoreEOFException;

/**
 *  Implementation for the SSH connection protocol as a transport protocol
 *  service.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: ConnectionProtocol.java,v 1.22 2002/12/18 19:27:29 martianx
 *      Exp $
 */
public class ConnectionProtocol
         extends Service {
    private static Logger log = Logger.getLogger(ConnectionProtocol.class);
    private Map activeChannels = new HashMap();
    private Map allowedChannels = new HashMap();
    private Map globalRequests = new HashMap();
    private long nextChannelId = 0;


    /**
     *  Constructor for the SshConnectionProtocol object
     */
    public ConnectionProtocol() {
        super("ssh-connection");
    }


    /**
     *  Adds a <code>ChannelFactory</code> to the connection protocol to allow
     *  the factory to create instances of its channels upon demand
     *
     *@param  cf                          a factory that can create at least one
     *      type of channel
     *@throws  ServiceOperationException  if a critical service operation occurs
     */
    public void allowChannelOpen(ChannelFactory cf)
             throws IOException {
        // Create a new instance to test the channel is valid
        Iterator it = cf.getChannelType().iterator();

        while (it.hasNext()) {
            allowedChannels.put((String) it.next(), cf);
        }
    }


    /**
     *  Adds a <code>GlobalRequestHandler</code> to the connection protocol to
     *  allow the processing of global requests
     *
     *@param  requestName  the name of the request to add
     *@param  handler      the handler of the request
     */
    public void allowGlobalRequest(String requestName,
            GlobalRequestHandler handler) {
        globalRequests.put(requestName, handler);
    }


    /**
     *  Opens a channel by sending the SSH_MSG_CHANNEL_OPEN message and waits
     *  for either the SSH_MSG_CHANNEL_OPEN_FAILURE or
     *  SSH_MSG_CHANNEL_OPEN_CONFIRMATION messages. If the request succeeds the
     *  method returns true
     *
     *@param  channel                         an uninitialized channel to open
     *@return                                 <tt>true</tt> if the channel is
     *      open otherwise <tt>false</tt>
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     *@exception  ServiceOperationException   if a critical service operation
     *      fails
     */
    public synchronized boolean openChannel(Channel channel)
             throws IOException {
        long channelId = nextChannelId++;

        // Create the message
        SshMsgChannelOpen msg =
                new SshMsgChannelOpen(channel.getChannelType(), channelId,
                channel.getLocalWindow().getWindowSpace(),
                channel.getLocalPacketSize(),
                channel.getChannelOpenData());

        // Send the message
        transport.sendMessage(msg, this);

        // Wait for the next message to confirm the open channel (or not)
        int messageIdFilter[] = new int[2];
        messageIdFilter[0] =
                SshMsgChannelOpenConfirmation.SSH_MSG_CHANNEL_OPEN_CONFIRMATION;

        messageIdFilter[1] =
                SshMsgChannelOpenFailure.SSH_MSG_CHANNEL_OPEN_FAILURE;

        try {

          SshMessage result = waitForSingleMessage(messageIdFilter);

          if (result.getMessageId() == SshMsgChannelOpenConfirmation.SSH_MSG_CHANNEL_OPEN_CONFIRMATION) {
            log.info("Channel is open");

            SshMsgChannelOpenConfirmation conf =
                    (SshMsgChannelOpenConfirmation) result;
            activeChannels.put(new Long(channelId), channel);

            channel.init(this, nativeSettings, channelId,
                    conf.getSenderChannel(), conf.getInitialWindowSize(),
                    conf.getMaximumPacketSize());
            log.debug("Channel initiated");

            channel.getState().setValue(ChannelState.CHANNEL_OPEN);

            return true;
          } else {
            // Make sure the channels state is closed
            channel.getState().setValue(ChannelState.CHANNEL_CLOSED);

            return false;
          }

        } catch(MessageStoreEOFException mse) {
            throw new ServiceOperationException(mse.getMessage());
        }


    }


    /**
     *  Sends data for the channel
     *
     *@param  channel                      the channel for which to send data
     *@param  data                         the data to send
     *@throws  TransportProtocolException  if a transport protocol error occurs
     */
    public synchronized void sendChannelData(Channel channel, byte data[])
             throws IOException {

        if (log.isDebugEnabled()) {
            log.debug("Sending " + String.valueOf(data.length)
                    + " bytes for channel id "
                    + String.valueOf(channel.getLocalChannelId()));
        }

        SshMsgChannelData msg =
                new SshMsgChannelData(channel.getRemoteChannelId(), data);

        transport.sendMessage(msg, this);
    }


    /**
     *  Sets the local channel as End Of File by sending the SSH_MSG_CHANNEL_EOF
     *  message.
     *
     *@param  channel                         The channel to set EOF
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     *@throws  ServiceOperationException      if a critical service operation
     *      fails
     */
    public synchronized void sendChannelEOF(Channel channel)
             throws IOException {
        if (!activeChannels.containsValue(channel)) {
            throw new ServiceOperationException("Attempt to send EOF for a non existent channel "
                    + String.valueOf(channel.getLocalChannelId()));
        }

        SshMsgChannelEOF msg =
                new SshMsgChannelEOF(channel.getRemoteChannelId());

        transport.sendMessage(msg, this);
    }


    /**
     *  Sends data to the channel using the SSH_MSG_CHANNEL_EXT_DATA.
     *
     *@param  channel                         The channel for which to send data
     *@param  extendedType                    the extended data type
     *@param  data                            The data to send
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     */
    public synchronized void sendChannelExtData(Channel channel, int extendedType, byte data[])
             throws IOException {
        channel.getRemoteWindow().consumeWindowSpace(data.length);

        SshMsgChannelExtendedData msg =
                new SshMsgChannelExtendedData(channel.getRemoteChannelId(),
                extendedType,
                data);

        transport.sendMessage(msg, this);
    }


    /**
     *  Sends the SSH_MSG_CHANNEL_REQUEST message.
     *
     *@param  channel                      The requests channel
     *@param  requestType                  The request type
     *@param  wantReply                    Whether a reply is needed
     *@param  requestData                  The request specific data
     *@return                              <tt>true</tt> if the request
     *      succeeded or want reply is false otherwise <tt>false</tt>
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    public synchronized boolean sendChannelRequest(Channel channel, String requestType,
            boolean wantReply, byte requestData[])
             throws IOException {
        boolean success = true;

        log.debug("Sending " + requestType + " request for the "
                + channel.getChannelType() + " channel");

        SshMsgChannelRequest msg =
                new SshMsgChannelRequest(channel.getRemoteChannelId(), requestType,
                wantReply, requestData);

        transport.sendMessage(msg, this);

        // If the user requests a reply then wait for the message and return result
        if (wantReply) {
            // Set up our message filter
            int messageIdFilter[] = new int[2];
            messageIdFilter[0] = SshMsgChannelSuccess.SSH_MSG_CHANNEL_SUCCESS;
            messageIdFilter[1] = SshMsgChannelFailure.SSH_MSG_CHANNEL_FAILURE;

            log.debug("Waiting for channel request reply");

            // Wait for either success or failure
            SshMessage reply = waitForSingleMessage(messageIdFilter);

            switch (reply.getMessageId()) {
                case SshMsgChannelSuccess.SSH_MSG_CHANNEL_SUCCESS:
                {
                    log.debug("Channel request succeeded");
                    success = true;

                    break;
                }

                case SshMsgChannelFailure.SSH_MSG_CHANNEL_FAILURE:
                {
                    log.debug("Channel request failed");
                    success = false;

                    break;
                }
            }

        }

        return success;
    }


    /**
     *  Sends the SSH_MSG_CHANNEL_FAILURE message
     *
     *@param  channel                      the requests channel
     *@throws  TransportProtocolException  if a transport protocol error occurs
     */
    public synchronized void sendChannelRequestFailure(Channel channel)
             throws IOException {
        SshMsgChannelFailure msg =
                new SshMsgChannelFailure(channel.getRemoteChannelId());

        transport.sendMessage(msg, this);
    }


    /**
     *  Sends the SSH_MSG_CHANNEL_SUCCES message.
     *
     *@param  channel                         The requests channel
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     */
    public synchronized void sendChannelRequestSuccess(Channel channel)
             throws IOException {
        SshMsgChannelSuccess msg =
                new SshMsgChannelSuccess(channel.getRemoteChannelId());

        transport.sendMessage(msg, this);
    }


    /**
     *  Sends the SSH_MSG_CHANNEL_WINDOW_ADJUST message.
     *
     *@param  channel                         the windows channel
     *@param  bytesToAdd                      The number of bytes to add to the
     *      window space
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     */
    public synchronized void sendChannelWindowAdjust(Channel channel, long bytesToAdd)
             throws IOException {
        SshMsgChannelWindowAdjust msg =
                new SshMsgChannelWindowAdjust(channel.getRemoteChannelId(),
                bytesToAdd);

        transport.sendMessage(msg, this);
    }


    /**
     *  Sends a global request
     *
     *@param  requestName                  the request name
     *@param  wantReply                    <tt>true</tt> if you want a reply
     *      otherwise <tt>false</tt>
     *@param  requestData                  the request data
     *@return                              <tt>true</tt> if the request
     *      succeeded or want reply is false otherwise <tt>false</tt>
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    public synchronized boolean sendGlobalRequest(String requestName, boolean wantReply,
            byte requestData[])
             throws IOException {
        boolean success = true;

        SshMsgGlobalRequest msg =
                new SshMsgGlobalRequest(requestName, true, requestData);

        transport.sendMessage(msg, this);

        if (wantReply) {
            // Set up our message filter
            int messageIdFilter[] = new int[2];
            messageIdFilter[0] = SshMsgRequestSuccess.SSH_MSG_REQUEST_SUCCESS;
            messageIdFilter[1] = SshMsgRequestFailure.SSH_MSG_REQUEST_FAILURE;

            log.debug("Waiting for global request reply");

            // Wait for either success or failure

            SshMessage reply = waitForSingleMessage(messageIdFilter);

            switch (reply.getMessageId()) {
                case SshMsgRequestSuccess.SSH_MSG_REQUEST_SUCCESS:
                {
                    log.debug("Global request succeeded");
                    success = true;

                    break;
                }

                case SshMsgRequestFailure.SSH_MSG_REQUEST_FAILURE:
                {
                    log.debug("Global request failed");
                    success = false;

                    break;
                }
            }
        }

        return success;
    }


    /**
     *  Called by the service thread framework to retreive a list of message ids
     *  for which asyncronous messaging will be activated. Any messages in the
     *  array will be forwared through <code>onMessageReceived</code>
     *
     *@return    an array of message ids
     */
    protected int[] getAsyncMessageFilter() {
        int messageFilter[] = new int[10];

        messageFilter[0] = SshMsgGlobalRequest.SSH_MSG_GLOBAL_REQUEST;
        messageFilter[3] = SshMsgChannelOpen.SSH_MSG_CHANNEL_OPEN;
        messageFilter[4] = SshMsgChannelClose.SSH_MSG_CHANNEL_CLOSE;
        messageFilter[5] = SshMsgChannelEOF.SSH_MSG_CHANNEL_EOF;
        messageFilter[6] =
                SshMsgChannelExtendedData.SSH_MSG_CHANNEL_EXTENDED_DATA;
        messageFilter[7] = SshMsgChannelData.SSH_MSG_CHANNEL_DATA;
        messageFilter[8] = SshMsgChannelRequest.SSH_MSG_CHANNEL_REQUEST;
        messageFilter[9] =
                SshMsgChannelWindowAdjust.SSH_MSG_CHANNEL_WINDOW_ADJUST;

        return messageFilter;
    }


    /**
     *  Closes the channel by sending the SSH_MSG_CHANNEL_CLOSE message.
     *
     *@param  channel                         The channel to close
     *@exception  TransportProtocolException  if an error occurs in the
     *      Transport Protocol
     */
    protected synchronized void closeChannel(Channel channel)
             throws IOException {
        SshMsgChannelClose msg =
                new SshMsgChannelClose(channel.getRemoteChannelId());

        transport.sendMessage(msg, this);
    }


    /**
     *  When a global request is received it is routed to this method which
     *  attempts to forward the request to the registered handler. A <code>
     * GlobalRequestHandler</code> can be registered by calling <code>
     * allowGlobalRequest</code>. If no handler is available the request fails
     *
     *@param  requestName                    the name of the request
     *@param  wantReply                      whether the remote computer wants a
     *      reply
     *@param  requestData                    the request specific data
     *@exception  ServiceOperationException  if a critical service operation
     *      fails
     *@throws  TransportProtocolException    if a transport protocol error
     *      occurs
     */
    protected void onGlobalRequest(String requestName, boolean wantReply,
            byte requestData[])
             throws IOException {
        log.debug("Processing " + requestName + " global request");

        if (!globalRequests.containsKey(requestName)) {
            sendGlobalRequestFailure();
        } else {
            GlobalRequestHandler handler =
                    (GlobalRequestHandler) globalRequests.get(requestName);

            GlobalRequestResponse response =
                    handler.processGlobalRequest(requestName, requestData);

            if (wantReply) {
                if (response.hasSucceeded()) {
                    sendGlobalRequestSuccess(response.getResponseData());
                } else {
                    sendGlobalRequestFailure();
                }
            }
        }
    }


    /**
     *  Receives notifications from the service thread that a new message has
     *  arrived.
     *
     *@param  msg                             The message received
     *@exception  ServiceOperationException   if a critical service operation
     *      occurs
     *@exception  TransportProtocolException  if an error occurs in th transport
     *      protocol
     */
    protected void onMessageReceived(SshMessage msg)
             throws IOException {
        // Route the message to the correct handling function
        switch (msg.getMessageId()) {
            case SshMsgGlobalRequest.SSH_MSG_GLOBAL_REQUEST:
            {
                onMsgGlobalRequest((SshMsgGlobalRequest) msg);

                break;
            }

            case SshMsgChannelOpen.SSH_MSG_CHANNEL_OPEN:
            {
                onMsgChannelOpen((SshMsgChannelOpen) msg);

                break;
            }

            case SshMsgChannelClose.SSH_MSG_CHANNEL_CLOSE:
            {
                onMsgChannelClose((SshMsgChannelClose) msg);

                break;
            }

            case SshMsgChannelEOF.SSH_MSG_CHANNEL_EOF:
            {
                onMsgChannelEOF((SshMsgChannelEOF) msg);

                break;
            }

            case SshMsgChannelData.SSH_MSG_CHANNEL_DATA:
            {
                onMsgChannelData((SshMsgChannelData) msg);

                break;
            }

            case SshMsgChannelExtendedData.SSH_MSG_CHANNEL_EXTENDED_DATA:
            {
                onMsgChannelExtendedData((SshMsgChannelExtendedData) msg);

                break;
            }

            case SshMsgChannelRequest.SSH_MSG_CHANNEL_REQUEST:
            {
                onMsgChannelRequest((SshMsgChannelRequest) msg);

                break;
            }

            case SshMsgChannelWindowAdjust.SSH_MSG_CHANNEL_WINDOW_ADJUST:
            {
                onMsgChannelWindowAdjust((SshMsgChannelWindowAdjust) msg);

                break;
            }

            default:
            {
                // If we never registered it why are we getting it?
                log.debug("Message not handled");
                throw new ServiceOperationException("Unregistered message received!");
            }
        }
    }


    /**
     *  Called by the service framework when the service is accepted
     */
    protected void onServiceAccept() { }


    /**
     *  Called by the framework once is initializing the service
     *
     *@param  startMode                      Indicates the start mode of the
     *      service
     *@exception  ServiceOperationException  if a critical service operation
     *      fails
     */
    protected void onServiceInit(int startMode)
             throws IOException {
        try {
            log.info("Registering connection protocol messages");

            transport.registerMessage(new Integer(SshMsgChannelOpenConfirmation.SSH_MSG_CHANNEL_OPEN_CONFIRMATION),
                    SshMsgChannelOpenConfirmation.class,
                    messageStore);

            transport.registerMessage(new Integer(SshMsgChannelOpenFailure.SSH_MSG_CHANNEL_OPEN_FAILURE),
                    SshMsgChannelOpenFailure.class,
                    messageStore);

            transport.registerMessage(new Integer(SshMsgChannelOpen.SSH_MSG_CHANNEL_OPEN),
                    SshMsgChannelOpen.class, messageStore);

            transport.registerMessage(new Integer(SshMsgChannelClose.SSH_MSG_CHANNEL_CLOSE),
                    SshMsgChannelClose.class, messageStore);

            transport.registerMessage(new Integer(SshMsgChannelEOF.SSH_MSG_CHANNEL_EOF),
                    SshMsgChannelEOF.class, messageStore);

            transport.registerMessage(new Integer(SshMsgChannelData.SSH_MSG_CHANNEL_DATA),
                    SshMsgChannelData.class, messageStore);

            transport.registerMessage(new Integer(SshMsgChannelExtendedData.SSH_MSG_CHANNEL_EXTENDED_DATA),
                    SshMsgChannelExtendedData.class,
                    messageStore);

            transport.registerMessage(new Integer(SshMsgChannelFailure.SSH_MSG_CHANNEL_FAILURE),
                    SshMsgChannelFailure.class, messageStore);

            transport.registerMessage(new Integer(SshMsgChannelRequest.SSH_MSG_CHANNEL_REQUEST),
                    SshMsgChannelRequest.class, messageStore);

            transport.registerMessage(new Integer(SshMsgChannelSuccess.SSH_MSG_CHANNEL_SUCCESS),
                    SshMsgChannelSuccess.class, messageStore);

            transport.registerMessage(new Integer(SshMsgChannelWindowAdjust.SSH_MSG_CHANNEL_WINDOW_ADJUST),
                    SshMsgChannelWindowAdjust.class,
                    messageStore);

            transport.registerMessage(new Integer(SshMsgGlobalRequest.SSH_MSG_GLOBAL_REQUEST),
                    SshMsgGlobalRequest.class, messageStore);

            transport.registerMessage(new Integer(SshMsgRequestFailure.SSH_MSG_REQUEST_FAILURE),
                    SshMsgRequestFailure.class, messageStore);

            transport.registerMessage(new Integer(SshMsgRequestSuccess.SSH_MSG_REQUEST_SUCCESS),
                    SshMsgRequestSuccess.class, messageStore);
        } catch (MessageAlreadyRegisteredException e) {
            throw new ServiceOperationException("A required message is already registered by another service");
        }
    }


    /**
     *  Called by the service framework when the service is requested
     */
    protected void onServiceRequest() { }


    /**
     *  Sends the SSH_MSG_CHANNEL_FAILURE message.
     *
     *@param  channel                         The failed channel
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     */
    protected void sendChannelFailure(Channel channel)
             throws IOException {
        SshMsgChannelFailure msg =
                new SshMsgChannelFailure(channel.getRemoteChannelId());

        transport.sendMessage(msg, this);
    }


    /**
     *  Sends the SSH_MSG_CHANNEL_OPEN_CONFIRMATION message.
     *
     *@param  channel                         The channel to confirm open
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     */
    protected void sendChannelOpenConfirmation(Channel channel)
             throws IOException {
        SshMsgChannelOpenConfirmation msg =
                new SshMsgChannelOpenConfirmation(channel.getRemoteChannelId(),
                channel.getLocalChannelId(),
                channel.getLocalWindow()
                .getWindowSpace(),
                channel.getLocalPacketSize(),
                channel.getChannelOpenData());

        transport.sendMessage(msg, this);
    }


    /**
     *  Sends the SSH_MSG_CHANNEL_OPEN_FAILURE message.
     *
     *@param  remoteChannelId                 The channel to inform of failure
     *@param  reasonCode                      The reason code
     *@param  additionalInfo                  Additional information
     *@param  languageTag                     The language tag
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     */
    protected void sendChannelOpenFailure(long remoteChannelId,
            long reasonCode,
            String additionalInfo,
            String languageTag)
             throws IOException {
        SshMsgChannelOpenFailure msg =
                new SshMsgChannelOpenFailure(remoteChannelId, reasonCode,
                additionalInfo, languageTag);

        transport.sendMessage(msg, this);
    }


    /**
     *  Sends the connection protocol message SSH_MSG_REQUEST_FAILURE.
     *
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     */
    protected void sendGlobalRequestFailure()
             throws IOException {
        SshMsgRequestFailure msg = new SshMsgRequestFailure();

        transport.sendMessage(msg, this);
    }


    /**
     *  Sends the connection protocol message SSH_MSG_REQUEST_SUCCESS.
     *
     *@param  requestData                     The request specific data
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     */
    protected void sendGlobalRequestSuccess(byte requestData[])
             throws IOException {
        SshMsgRequestSuccess msg = new SshMsgRequestSuccess(requestData);

        transport.sendMessage(msg, this);
    }


    /**
     *  Gets the Channel instance for the channel Id provided.
     *
     *@param  channelId  The local channel id
     *@return            The Channel instance
     */
    private Channel getChannel(long channelId) {
        return (Channel) activeChannels.get(new Long(channelId));
    }


    /**
     *  Handles the SSH_MSG_CHANNEL_CLOSE message. This method sets the channel
     *  object for the closed channel to a closed state.
     *
     *@param  msg                             The message received
     *@exception  ServiceOperationException   if a critical serice operation
     *      fails
     *@exception  TransportProtocolException  if a transport protocol error
     *      occurs
     */
    private void onMsgChannelClose(SshMsgChannelClose msg)
             throws IOException {
        Channel channel = getChannel(msg.getRecipientChannel());

        // If we have not already closed it then inform the subclasses
        if (channel == null) {
            throw new ServiceOperationException("Remote computer tried to close a "
                    + "non existent channel!");
        }

        // If the channel is not already closed then close it
        if (channel.getState().getValue() != ChannelState.CHANNEL_CLOSED) {
            channel.close();
        }

        // Remove the channel
        removeChannel(channel);
    }


    /**
     *  Handles the SSH_MSG_CHANNEL_DATA message. This method consumes window
     *  space for the data's channel and adds the data to the channels
     *  InputStream
     *
     *@param  msg                          The message received
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    private void onMsgChannelData(SshMsgChannelData msg)
             throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Received " + String.valueOf(msg.getChannelData().length)
                    + " bytes of data for channel id "
                    + String.valueOf(msg.getRecipientChannel()));
        }

        // Get the data's channel
        Channel channel = getChannel(msg.getRecipientChannel());

        if (channel == null) {
            throw new ServiceOperationException("Remote computer sent data for non existent channel");
        }

        channel.onChannelData(msg);
    }


    /**
     *  Handles the SSH_MSG_CHANNEL_EOF message. This method sets the channels
     *  InputStream to EOF
     *
     *@param  msg                         The message received
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    private void onMsgChannelEOF(SshMsgChannelEOF msg)
             throws IOException {
        Channel channel = getChannel(msg.getRecipientChannel());

        if (channel == null) {
            throw new ServiceOperationException("Remote side tried to set a non "
                    + "existent channel to EOF!");
        }

        try {
            channel.getInputStream().close();
        } catch (IOException ioe) {
            throw new ServiceOperationException("Failed to close the ChannelInputStream");
        }
    }


    /**
     *  TODO: This implementation is incomplete, we need to add an extended
     *  channel IO streams to the channel
     *
     *@param  msg                         The message received
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    private void onMsgChannelExtendedData(SshMsgChannelExtendedData msg)
             throws IOException {

        Channel channel = getChannel(msg.getRecipientChannel());

        if (channel == null) {
            throw new ServiceOperationException("Remote computer sent data for non existent channel");
        }

        channel.getLocalWindow().consumeWindowSpace(msg.getChannelData().length);

        channel.incoming.addMessage(msg);
    }


    /**
     *  Handles the SSH_MSG_CHANNEL_OPEN message by looking up the registered
     *  <code>ChannelFactory</code> for the channel type and if available
     *  requests a new instance of the channel type.
     *
     *@param  msg                          The message received
     *@throws  ServiceOperationException   if a critical service operation fails
     *@throws  TransportProtocolException  if a transport protocol error occurs
     */
    private void onMsgChannelOpen(SshMsgChannelOpen msg)
             throws IOException {
        // Try to get the channel implementation from the allowed channels
        ChannelFactory cf =
                (ChannelFactory) allowedChannels.get(msg.getChannelType());

        if (cf == null) {
            sendChannelOpenFailure(msg.getSenderChannelId(),
                    SshMsgChannelOpenFailure.SSH_OPEN_CONNECT_FAILED,
                    "The channel type is not supported", "");
            log.info("Request for channel type " + msg.getChannelType()
                    + " refused");
            return;
        }


        try {
            Channel channel =
                    cf.createChannel(msg.getChannelType(), msg.getChannelData());

            channel.init(this, nativeSettings, nextChannelId++,
                    msg.getSenderChannelId(), msg.getInitialWindowSize(),
                    msg.getMaximumPacketSize());

            activeChannels.put(new Long(channel.getLocalChannelId()), channel);

            sendChannelOpenConfirmation(channel);
        } catch (InvalidChannelException ice) {
            sendChannelOpenFailure(msg.getSenderChannelId(),
                    SshMsgChannelOpenFailure.SSH_OPEN_CONNECT_FAILED,
                    ice.getMessage(), "");
        }
    }


    /**
     *  Handles the SSH_MSG_CHANNEL_REQUEST message by passing the request onto
     *  the appropriate channel.
     *
     *@param  msg                         the message received
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    private void onMsgChannelRequest(SshMsgChannelRequest msg)
             throws IOException {
        Channel channel = getChannel(msg.getRecipientChannel());

        if (channel == null) {
            log.warn("Remote computer tried to make a request for "
                    + "a non existence channel!");
        }

        channel.onChannelRequest(msg.getRequestType(), msg.getWantReply(),
                msg.getChannelData());
    }


    /**
     *  Handles the SSH_MSG_CHANNEL_WINDOW_ADJUST message. This method increases
     *  the remote sides window space by the number of bytes specified in the
     *  message.
     *
     *@param  msg                         the message received
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    private void onMsgChannelWindowAdjust(SshMsgChannelWindowAdjust msg)
             throws IOException {
        Channel channel = getChannel(msg.getRecipientChannel());

        if (channel == null) {
            throw new ServiceOperationException("Remote computer tried to increase "
                    + "window space for a non existent channel!");
        }

        channel.getRemoteWindow().increaseWindowSpace(msg.getBytesToAdd());
        log.debug(String.valueOf(msg.getBytesToAdd())
                + " bytes added to remote window");
        log.debug("Remote window space is "
                + String.valueOf(channel.getRemoteWindow().getWindowSpace()));
    }


    /**
     *  Handles the SSH_MSG_GLOBAL_REQUEST message.
     *
     *@param  msg                          The message received
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    private void onMsgGlobalRequest(SshMsgGlobalRequest msg)
             throws IOException {
        onGlobalRequest(msg.getRequestName(), msg.getWantReply(),
                msg.getRequestData());
    }


    /**
     *  Removes a channel.
     *
     *@param  channel  The channel to remove
     */
    private void removeChannel(Channel channel) {
        activeChannels.remove(new Long(channel.getLocalChannelId()));
    }
}
