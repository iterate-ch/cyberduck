/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
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

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.transport.AsyncService;
import com.sshtools.j2ssh.transport.MessageStoreEOFException;
import com.sshtools.j2ssh.transport.ServiceState;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.transport.TransportProtocolState;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * @author $author$
 * @version $Revision$
 */
public class ConnectionProtocol extends AsyncService {
    private static Logger log = Logger.getLogger(ConnectionProtocol.class);
    private HashSet reusableChannels = new HashSet();
    private Map activeChannels = new HashMap();
    private Map allowedChannels = new HashMap();
    private Map globalRequests = new HashMap();
    private long nextChannelId = 0;

    /**
     * Creates a new ConnectionProtocol object.
     */
    public ConnectionProtocol() {
        super("ssh-connection");
    }

    /**
     * @param channelName
     * @param cf
     * @throws IOException
     */
    public void addChannelFactory(String channelName, ChannelFactory cf) throws
            IOException {
        allowedChannels.put(channelName, cf);
    }

    /**
     * @param channelName
     */
    public void removeChannelFactory(String channelName) {
        allowedChannels.remove(channelName);
    }

    /**
     * @param channelName
     * @return
     */
    public boolean containsChannelFactory(String channelName) {
        return allowedChannels.containsKey(channelName);
    }

    /**
     * @param requestName
     * @param handler
     */
    public void allowGlobalRequest(String requestName,
                                   GlobalRequestHandler handler) {
        globalRequests.put(requestName, handler);
    }


    public void onStart() {

    }

    /**
     * @param channel
     * @return
     * @throws IOException
     */
    public synchronized boolean openChannel(Channel channel) throws IOException {
        return openChannel(channel, null);
    }

    /**
     * @return
     */
    public boolean isConnected() {
        return ((transport.getState().getValue() ==
                TransportProtocolState.CONNECTED)
                ||
                (transport.getState().getValue() ==
                TransportProtocolState.PERFORMING_KEYEXCHANGE))
                && (getState().getValue() == ServiceState.SERVICE_STARTED);
    }

    private Long getChannelId() {
        synchronized (activeChannels) {
            if (reusableChannels.size() <= 0) {
                return new Long(nextChannelId++);
            }
            else {
                return (Long) reusableChannels.iterator().next();
            }
        }
    }

    /**
     * @param channel
     * @param eventListener
     * @return
     * @throws IOException
     * @throws SshException
     */
    public synchronized boolean openChannel(Channel channel,
                                            ChannelEventListener eventListener) throws
            IOException {
        synchronized (activeChannels) {
            Long channelId = getChannelId();

            // Create the message
            SshMsgChannelOpen msg = new SshMsgChannelOpen(channel
                    .getChannelType(), channelId.longValue(),
                    channel.getLocalWindow().getWindowSpace(),
                    channel.getLocalPacketSize(), channel.getChannelOpenData());

            // Send the message
            transport.sendMessage(msg, this);

            // Wait for the next message to confirm the open channel (or not)
            int[] messageIdFilter = new int[2];
            messageIdFilter[0] = SshMsgChannelOpenConfirmation.
                    SSH_MSG_CHANNEL_OPEN_CONFIRMATION;

            messageIdFilter[1] = SshMsgChannelOpenFailure.
                    SSH_MSG_CHANNEL_OPEN_FAILURE;

            try {
                SshMessage result = transport.getMessageStore().getMessage(messageIdFilter);

                if (result.getMessageId() ==
                        SshMsgChannelOpenConfirmation.SSH_MSG_CHANNEL_OPEN_CONFIRMATION) {
                    SshMsgChannelOpenConfirmation conf = (SshMsgChannelOpenConfirmation)
                            result;
                    activeChannels.put(channelId, channel);

                    log.debug("Initiating channel");
                    channel.init(this, channelId.longValue(),
                            conf.getSenderChannel(), conf.getInitialWindowSize(),
                            conf.getMaximumPacketSize(), eventListener);

                    channel.open();
                    log.info("Channel "
                            + String.valueOf(channel.getLocalChannelId())
                            + " is open [" + channel.getName() + "]");

                    return true;
                }
                else {
                    // Make sure the channels state is closed
                    channel.getState().setValue(ChannelState.CHANNEL_CLOSED);

                    return false;
                }
            }
            catch (MessageStoreEOFException mse) {
                throw new IOException(mse.getMessage());
            }
            catch (InterruptedException ex) {
                throw new SshException(ex.getMessage());
            }
        }
    }

    /**
     *
     */
    protected void onStop() {
        log.info("Closing all active channels");

        try {
            Channel channel;

            for (Iterator x = activeChannels.values().iterator(); x.hasNext();) {
                channel = (Channel) x.next();

                if (channel != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Closing " + channel.getName() + " id="
                                + String.valueOf(channel.getLocalChannelId()));
                    }

                    channel.close();
                }
            }
        }
        catch (Throwable t) {
        }

        activeChannels.clear();
    }

    /**
     * @param channel
     * @param data
     * @throws IOException
     */
    public synchronized void sendChannelData(Channel channel, byte[] data)
            throws IOException {
        synchronized (channel.getState()) {
            if (log.isDebugEnabled()) {
                log.debug("Sending " + String.valueOf(data.length)
                        + " bytes for channel id "
                        + String.valueOf(channel.getLocalChannelId()));
            }

            int sent = 0;
            int block;
            int remaining;
            long max;
            byte[] buffer;
            ChannelDataWindow window = channel.getRemoteWindow();

            while (sent < data.length) {
                remaining = data.length - sent;
                max = ((window.getWindowSpace() < channel.getRemotePacketSize())
                        && (window.getWindowSpace() > 0)) ? window.getWindowSpace()
                        : channel
                        .getRemotePacketSize();
                block = (max < remaining) ? (int) max : remaining;
                channel.remoteWindow.consumeWindowSpace(block);
                buffer = new byte[block];
                System.arraycopy(data, sent, buffer, 0, block);

                SshMsgChannelData msg = new SshMsgChannelData(channel
                        .getRemoteChannelId(), buffer);
                transport.sendMessage(msg, this);

                /*                if (type != null) {
                     channel.sendChannelExtData(type.intValue(), buffer);
                                } else {
                                    channel.sendChannelData(buffer);
                                }*/
                sent += block;
            }
        }
    }

    /**
     * @param channel
     * @throws IOException
     */
    public void sendChannelEOF(Channel channel) throws IOException {
        synchronized (activeChannels) {
            if (!activeChannels.containsValue(channel)) {
                throw new IOException("Attempt to send EOF for a non existent channel "
                        + String.valueOf(channel.getLocalChannelId()));
            }

            log.info("Local computer has set channel "
                    + String.valueOf(channel.getLocalChannelId()) + " to EOF ["
                    + channel.getName() + "]");

            SshMsgChannelEOF msg = new SshMsgChannelEOF(channel
                    .getRemoteChannelId());

            transport.sendMessage(msg, this);
        }
    }

    /**
     * @param channel
     * @param extendedType
     * @param data
     * @throws IOException
     */
    public synchronized void sendChannelExtData(Channel channel,
                                                int extendedType, byte[] data) throws
            IOException {
        channel.getRemoteWindow().consumeWindowSpace(data.length);

        int sent = 0;
        int block;
        int remaining;
        long max;
        byte[] buffer;
        ChannelDataWindow window = channel.getRemoteWindow();

        while (sent < data.length) {
            remaining = data.length - sent;
            max = ((window.getWindowSpace() < channel.getRemotePacketSize())
                    && (window.getWindowSpace() > 0)) ? window.getWindowSpace()
                    : channel.getRemotePacketSize();
            block = (max < remaining) ? (int) max : remaining;
            channel.remoteWindow.consumeWindowSpace(block);
            buffer = new byte[block];
            System.arraycopy(data, sent, buffer, 0, block);

            SshMsgChannelExtendedData msg = new SshMsgChannelExtendedData(channel
                    .getRemoteChannelId(), extendedType, buffer);

            transport.sendMessage(msg, this);

            /*                if (type != null) {
                            channel.sendChannelExtData(type.intValue(), buffer);
                        } else {
                            channel.sendChannelData(buffer);
                        }*/
            sent += block;
        }
    }

    /**
     * @param channel
     * @param requestType
     * @param wantReply
     * @param requestData
     * @return
     * @throws IOException
     * @throws SshException
     */
    public synchronized boolean sendChannelRequest(Channel channel,
                                                   String requestType,
                                                   boolean wantReply,
                                                   byte[] requestData) throws
            IOException {
        boolean success = true;

        log.debug("Sending " + requestType + " request for the "
                + channel.getChannelType() + " channel");

        SshMsgChannelRequest msg = new SshMsgChannelRequest(channel
                .getRemoteChannelId(), requestType, wantReply, requestData);

        transport.sendMessage(msg, this);

        // If the user requests a reply then wait for the message and return result
        if (wantReply) {
            // Set up our message filter
            int[] messageIdFilter = new int[2];
            messageIdFilter[0] = SshMsgChannelSuccess.SSH_MSG_CHANNEL_SUCCESS;
            messageIdFilter[1] = SshMsgChannelFailure.SSH_MSG_CHANNEL_FAILURE;

            log.debug("Waiting for channel request reply");

            try {
                // Wait for either success or failure
                SshMessage reply = transport.getMessageStore().getMessage(messageIdFilter);

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
            catch (InterruptedException ex) {
                throw new SshException(ex.getMessage());
            }
        }

        return success;
    }

    /**
     * @param channel
     * @throws IOException
     */
    public void sendChannelRequestFailure(Channel channel) throws IOException {
        SshMsgChannelFailure msg = new SshMsgChannelFailure(channel
                .getRemoteChannelId());

        transport.sendMessage(msg, this);
    }

    /**
     * @param channel
     * @throws IOException
     */
    public void sendChannelRequestSuccess(Channel channel) throws IOException {
        SshMsgChannelSuccess msg = new SshMsgChannelSuccess(channel
                .getRemoteChannelId());

        transport.sendMessage(msg, this);
    }

    /**
     * @param channel
     * @param bytesToAdd
     * @throws IOException
     */
    public void sendChannelWindowAdjust(Channel channel,
                                        long bytesToAdd) throws IOException {

        log.debug("Increasing window size by " + String.valueOf(bytesToAdd) +
                " bytes");

        SshMsgChannelWindowAdjust msg = new SshMsgChannelWindowAdjust(channel
                .getRemoteChannelId(), bytesToAdd);

        transport.sendMessage(msg, this);
    }

    /**
     * @param requestName
     * @param wantReply
     * @param requestData
     * @return
     * @throws IOException
     * @throws SshException
     */
    public synchronized byte[] sendGlobalRequest(String requestName,
                                                 boolean wantReply,
                                                 byte[] requestData) throws
            IOException {
        boolean success = true;

        SshMsgGlobalRequest msg = new SshMsgGlobalRequest(requestName, true,
                requestData);

        transport.sendMessage(msg, this);

        if (wantReply) {
            // Set up our message filter
            int[] messageIdFilter = new int[2];
            messageIdFilter[0] = SshMsgRequestSuccess.SSH_MSG_REQUEST_SUCCESS;
            messageIdFilter[1] = SshMsgRequestFailure.SSH_MSG_REQUEST_FAILURE;

            log.debug("Waiting for global request reply");

            try {
                // Wait for either success or failure
                SshMessage reply = transport.getMessageStore().getMessage(messageIdFilter);

                switch (reply.getMessageId()) {
                    case SshMsgRequestSuccess.SSH_MSG_REQUEST_SUCCESS:
                        {
                            log.debug("Global request succeeded");

                            return ((SshMsgRequestSuccess) reply).getRequestData();
                        }

                    case SshMsgRequestFailure.SSH_MSG_REQUEST_FAILURE:
                        {
                            log.debug("Global request failed");
                            throw new SshException("The request failed");
                        }
                }
            }
            catch (InterruptedException ex) {
                throw new SshException(ex.getMessage());
            }
        }

        return null;
    }

    /**
     *
     *
     * @return
     */
    /* protected int[] getAsyncMessageFilter() {
       int[] messageFilter = new int[10];

       messageFilter[0] = SshMsgGlobalRequest.SSH_MSG_GLOBAL_REQUEST;
       messageFilter[3] = SshMsgChannelOpen.SSH_MSG_CHANNEL_OPEN;
       messageFilter[4] = SshMsgChannelClose.SSH_MSG_CHANNEL_CLOSE;
       messageFilter[5] = SshMsgChannelEOF.SSH_MSG_CHANNEL_EOF;
       messageFilter[6] = SshMsgChannelExtendedData.SSH_MSG_CHANNEL_EXTENDED_DATA;
       messageFilter[7] = SshMsgChannelData.SSH_MSG_CHANNEL_DATA;
       messageFilter[8] = SshMsgChannelRequest.SSH_MSG_CHANNEL_REQUEST;
       messageFilter[9] = SshMsgChannelWindowAdjust.SSH_MSG_CHANNEL_WINDOW_ADJUST;

       return messageFilter;
     }*/

    /**
     * @param channel
     * @throws IOException
     */
    protected void closeChannel(Channel channel) throws IOException {
        SshMsgChannelClose msg = new SshMsgChannelClose(channel
                .getRemoteChannelId());

        log.info("Local computer has closed channel "
                + String.valueOf(channel.getLocalChannelId()) + "["
                + channel.getName() + "]");

        transport.sendMessage(msg, this);
    }

    /**
     * @param requestName
     * @param wantReply
     * @param requestData
     * @throws IOException
     */
    protected void onGlobalRequest(String requestName, boolean wantReply,
                                   byte[] requestData) throws IOException {
        log.debug("Processing " + requestName + " global request");

        if (!globalRequests.containsKey(requestName)) {
            sendGlobalRequestFailure();
        }
        else {
            GlobalRequestHandler handler = (GlobalRequestHandler) globalRequests
                    .get(requestName);

            GlobalRequestResponse response = handler.processGlobalRequest(requestName,
                    requestData);

            if (wantReply) {
                if (response.hasSucceeded()) {
                    sendGlobalRequestSuccess(response.getResponseData());
                }
                else {
                    sendGlobalRequestFailure();
                }
            }
        }
    }

    /**
     * @param msg
     * @throws IOException
     */
    public void onMessageReceived(SshMessage msg) throws IOException {
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
                    throw new IOException("Unregistered message received!");
                }
        }
    }

    /**
     *
     */
    protected void onServiceAccept() {
    }

    /**
     * @param startMode
     * @throws IOException
     */
    protected void onServiceInit(int startMode) throws IOException {
        log.info("Registering connection protocol messages");

        transport.getMessageStore().registerMessage(SshMsgChannelOpenConfirmation.
                SSH_MSG_CHANNEL_OPEN_CONFIRMATION,
                SshMsgChannelOpenConfirmation.class);

        transport.getMessageStore().registerMessage(SshMsgChannelOpenFailure.
                SSH_MSG_CHANNEL_OPEN_FAILURE,
                SshMsgChannelOpenFailure.class);

        transport.getMessageStore().registerMessage(SshMsgChannelOpen.SSH_MSG_CHANNEL_OPEN,
                SshMsgChannelOpen.class, this);

        transport.getMessageStore().registerMessage(SshMsgChannelClose.SSH_MSG_CHANNEL_CLOSE,
                SshMsgChannelClose.class, this);

        transport.getMessageStore().registerMessage(SshMsgChannelEOF.SSH_MSG_CHANNEL_EOF,
                SshMsgChannelEOF.class, this);

        transport.getMessageStore().registerMessage(SshMsgChannelData.SSH_MSG_CHANNEL_DATA,
                SshMsgChannelData.class, this);

        transport.getMessageStore().registerMessage(SshMsgChannelExtendedData.
                SSH_MSG_CHANNEL_EXTENDED_DATA,
                SshMsgChannelExtendedData.class, this);

        transport.getMessageStore().registerMessage(SshMsgChannelFailure.SSH_MSG_CHANNEL_FAILURE,
                SshMsgChannelFailure.class);

        transport.getMessageStore().registerMessage(SshMsgChannelRequest.SSH_MSG_CHANNEL_REQUEST,
                SshMsgChannelRequest.class, this);

        transport.getMessageStore().registerMessage(SshMsgChannelSuccess.SSH_MSG_CHANNEL_SUCCESS,
                SshMsgChannelSuccess.class);

        transport.getMessageStore().registerMessage(SshMsgChannelWindowAdjust.
                SSH_MSG_CHANNEL_WINDOW_ADJUST,
                SshMsgChannelWindowAdjust.class, this);

        transport.getMessageStore().registerMessage(SshMsgGlobalRequest.SSH_MSG_GLOBAL_REQUEST,
                SshMsgGlobalRequest.class, this);

        transport.getMessageStore().registerMessage(SshMsgRequestFailure.SSH_MSG_REQUEST_FAILURE,
                SshMsgRequestFailure.class);

        transport.getMessageStore().registerMessage(SshMsgRequestSuccess.SSH_MSG_REQUEST_SUCCESS,
                SshMsgRequestSuccess.class);
    }

    /**
     *
     */
    protected void onServiceRequest() {
    }

    /**
     * @param channel
     * @throws IOException
     */
    protected void sendChannelFailure(Channel channel) throws IOException {
        SshMsgChannelFailure msg = new SshMsgChannelFailure(channel
                .getRemoteChannelId());

        transport.sendMessage(msg, this);
    }

    /**
     * @param channel
     * @throws IOException
     */
    protected void sendChannelOpenConfirmation(Channel channel) throws
            IOException {
        SshMsgChannelOpenConfirmation msg = new SshMsgChannelOpenConfirmation(channel
                .getRemoteChannelId(), channel.getLocalChannelId(),
                channel.getLocalWindow().getWindowSpace(),
                channel.getLocalPacketSize(),
                channel.getChannelConfirmationData());

        transport.sendMessage(msg, this);
    }

    /**
     * @param remoteChannelId
     * @param reasonCode
     * @param additionalInfo
     * @param languageTag
     * @throws IOException
     */
    protected void sendChannelOpenFailure(long remoteChannelId,
                                          long reasonCode, String additionalInfo,
                                          String languageTag) throws IOException {
        SshMsgChannelOpenFailure msg = new SshMsgChannelOpenFailure(remoteChannelId,
                reasonCode, additionalInfo, languageTag);

        transport.sendMessage(msg, this);
    }

    /**
     * @throws IOException
     */
    protected void sendGlobalRequestFailure() throws IOException {
        SshMsgRequestFailure msg = new SshMsgRequestFailure();

        transport.sendMessage(msg, this);
    }

    /**
     * @param requestData
     * @throws IOException
     */
    protected void sendGlobalRequestSuccess(byte[] requestData) throws
            IOException {
        SshMsgRequestSuccess msg = new SshMsgRequestSuccess(requestData);

        transport.sendMessage(msg, this);
    }

    private Channel getChannel(long channelId) throws IOException {
        synchronized (activeChannels) {
            Long l = new Long(channelId);

            if (!activeChannels.containsKey(l)) {
                throw new IOException("Non existent channel " + l.toString()
                        + " requested");
            }

            return (Channel) activeChannels.get(l);
        }
    }

    private void onMsgChannelClose(SshMsgChannelClose msg) throws IOException {
        Channel channel = getChannel(msg.getRecipientChannel());

        // If we have not already closed it then inform the subclasses
        if (channel == null) {
            throw new IOException("Remote computer tried to close a "
                    + "non existent channel "
                    + String.valueOf(msg.getRecipientChannel()));
        }

        log.info("Remote computer has closed channel "
                + String.valueOf(channel.getLocalChannelId()) + "["
                + channel.getName() + "]");

        // If the channel is not already closed then close it
        if (channel.getState().getValue() != ChannelState.CHANNEL_CLOSED) {
            channel.remoteClose();
        }
    }

    private void onMsgChannelData(SshMsgChannelData msg) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Received " + String.valueOf(msg.getChannelData().length)
                    + " bytes of data for channel id "
                    + String.valueOf(msg.getRecipientChannel()));
        }

        // Get the data's channel
        Channel channel = getChannel(msg.getRecipientChannel());

        channel.processChannelData(msg);
    }

    private void onMsgChannelEOF(SshMsgChannelEOF msg) throws IOException {
        Channel channel = getChannel(msg.getRecipientChannel());

        try {
            log.info("Remote computer has set channel "
                    + String.valueOf(msg.getRecipientChannel()) + " to EOF ["
                    + channel.getName() + "]");

            channel.setRemoteEOF();
        }
        catch (IOException ioe) {
            log.info("Failed to close the ChannelInputStream after EOF event");
        }
    }

    private void onMsgChannelExtendedData(SshMsgChannelExtendedData msg) throws
            IOException {
        Channel channel = getChannel(msg.getRecipientChannel());

        if (channel == null) {
            throw new IOException("Remote computer sent data for non existent channel");
        }

        channel.getLocalWindow().consumeWindowSpace(msg.getChannelData().length);

        channel.processChannelData(msg);
    }

    private void onMsgChannelOpen(SshMsgChannelOpen msg) throws IOException {
        synchronized (activeChannels) {
            log.info("Request for " + msg.getChannelType()
                    + " channel recieved");

            // Try to get the channel implementation from the allowed channels
            ChannelFactory cf = (ChannelFactory) allowedChannels.get(msg
                    .getChannelType());

            if (cf == null) {
                sendChannelOpenFailure(msg.getSenderChannelId(),
                        SshMsgChannelOpenFailure.SSH_OPEN_CONNECT_FAILED,
                        "The channel type is not supported", "");
                log.info("Request for channel type " + msg.getChannelType()
                        + " refused");

                return;
            }

            try {
                log.info("Creating channel " + msg.getChannelType());

                Channel channel = cf.createChannel(msg.getChannelType(),
                        msg.getChannelData());

                // Initialize the channel
                log.info("Initiating channel");

                Long channelId = getChannelId();
                channel.init(this, channelId.longValue(),
                        msg.getSenderChannelId(), msg.getInitialWindowSize(),
                        msg.getMaximumPacketSize());

                activeChannels.put(channelId, channel);

                log.info("Sending channel open confirmation");

                // Send the confirmation message
                sendChannelOpenConfirmation(channel);

                // Open the channel for real
                channel.open();
            }
            catch (InvalidChannelException ice) {
                sendChannelOpenFailure(msg.getSenderChannelId(),
                        SshMsgChannelOpenFailure.SSH_OPEN_CONNECT_FAILED,
                        ice.getMessage(), "");
            }
        }
    }

    private void onMsgChannelRequest(SshMsgChannelRequest msg) throws IOException {
        Channel channel = getChannel(msg.getRecipientChannel());

        if (channel == null) {
            log.warn("Remote computer tried to make a request for "
                    + "a non existence channel!");
        }

        channel.onChannelRequest(msg.getRequestType(), msg.getWantReply(),
                msg.getChannelData());
    }

    private void onMsgChannelWindowAdjust(SshMsgChannelWindowAdjust msg) throws
            IOException {
        Channel channel = getChannel(msg.getRecipientChannel());

        if (channel == null) {
            throw new IOException("Remote computer tried to increase "
                    + "window space for non existent channel " +
                    String.valueOf(msg.getRecipientChannel()));
        }

        channel.getRemoteWindow().increaseWindowSpace(msg.getBytesToAdd());

        if (log.isDebugEnabled()) {
            log.debug(String.valueOf(msg.getBytesToAdd())
                    + " bytes added to remote window");
            log.debug("Remote window space is "
                    + String.valueOf(channel.getRemoteWindow().getWindowSpace()));
        }
    }

    private void onMsgGlobalRequest(SshMsgGlobalRequest msg) throws IOException {
        onGlobalRequest(msg.getRequestName(), msg.getWantReply(),
                msg.getRequestData());
    }

    /**
     * @param channel
     */
    protected void freeChannel(Channel channel) {
        synchronized (activeChannels) {
            log.info("Freeing channel "
                    + String.valueOf(channel.getLocalChannelId()) + " ["
                    + channel.getName() + "]");

            Long channelId = new Long(channel.getLocalChannelId());
            activeChannels.remove(channelId);

            //reusableChannels.add(channelId);
        }
    }
}
