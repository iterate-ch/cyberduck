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
package com.sshtools.j2ssh.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


/**
 * Provides a client connection to the ssh agent.
 *
 * @author $author$
 * @version $Revision$
 */
public class SshAgentClient {
    private static Log log = LogFactory.getLog(SshAgentClient.class);

    /**
     * The hash and sign private key operation
     */
    public static final String HASH_AND_SIGN = "hash-and-sign";
    InputStream in;
    OutputStream out;
    boolean isForwarded = false;
    HashMap messages = new HashMap();
    Socket socket;

    SshAgentClient(boolean isForwarded, String application, InputStream in,
                   OutputStream out) throws IOException {
        log.info("New SshAgentClient instance created");
        this.in = in;
        this.out = out;
        this.isForwarded = isForwarded;
        registerMessages();

        if (isForwarded) {
            sendForwardingNotice();
        }
        else {
            sendVersionRequest(application);
        }
    }

    SshAgentClient(boolean isForwarded, String application, Socket socket)
            throws IOException {
        log.info("New SshAgentClient instance created");
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.isForwarded = isForwarded;
        registerMessages();

        if (isForwarded) {
            sendForwardingNotice();
        }
        else {
            sendVersionRequest(application);
        }
    }

    /**
     * Connect to the local agent.
     *
     * @param application the application connecting
     * @param location    the location of the agent, in the form "localhost:port"
     * @return a connected agent client
     * @throws AgentNotAvailableException if the agent is not available at the
     *                                    location specified
     * @throws IOException                if an IO error occurs
     */
    public static SshAgentClient connectLocalAgent(String application,
                                                   String location) throws AgentNotAvailableException, IOException {
        try {
            Socket socket = connectAgentSocket(location);

            return new SshAgentClient(false, application, socket);
        }
        catch (IOException ex) {
            throw new AgentNotAvailableException();
        }
    }

    /**
     * Connect a socket to the agent at the location specified.
     *
     * @param location the location of the agent, in the form "localhost:port"
     * @return the connected socket
     * @throws AgentNotAvailableException if an agent is not available at the
     *                                    location specified
     * @throws IOException                if an IO error occurs
     */
    public static Socket connectAgentSocket(String location)
            throws AgentNotAvailableException, IOException {
        try {
            if (location == null) {
                throw new AgentNotAvailableException();
            }

            int idx = location.indexOf(":");

            if (idx == -1) {
                throw new AgentNotAvailableException();
            }

            String host = location.substring(0, idx);
            int port = Integer.parseInt(location.substring(idx + 1));
            Socket socket = new Socket(host, port);

            return socket;
        }
        catch (IOException ex) {
            throw new AgentNotAvailableException();
        }
    }

    /**
     * Close the agent
     */
    public void close() {
        log.info("Closing agent client");

        try {
            in.close();
        }
        catch (IOException ex) {
        }

        try {
            out.close();
        }
        catch (IOException ex1) {
        }

        try {
            if (socket != null) {
                socket.close();
            }
        }
        catch (IOException ex2) {
        }
    }

    /**
     * Register the subsystem messages
     */
    protected void registerMessages() {
        messages.put(new Integer(SshAgentVersionResponse.SSH_AGENT_VERSION_RESPONSE),
                SshAgentVersionResponse.class);
        messages.put(new Integer(SshAgentSuccess.SSH_AGENT_SUCCESS),
                SshAgentSuccess.class);
        messages.put(new Integer(SshAgentFailure.SSH_AGENT_FAILURE),
                SshAgentFailure.class);
        messages.put(new Integer(SshAgentKeyList.SSH_AGENT_KEY_LIST),
                SshAgentKeyList.class);
        messages.put(new Integer(SshAgentRandomData.SSH_AGENT_RANDOM_DATA),
                SshAgentRandomData.class);
        messages.put(new Integer(SshAgentAlive.SSH_AGENT_ALIVE),
                SshAgentAlive.class);
        messages.put(new Integer(SshAgentOperationComplete.SSH_AGENT_OPERATION_COMPLETE),
                SshAgentOperationComplete.class);
    }

    /**
     * Request the agent version.
     *
     * @param application the application connecting
     * @throws IOException if an IO error occurs
     */
    protected void sendVersionRequest(String application)
            throws IOException {
        SubsystemMessage msg = new SshAgentRequestVersion(application);
        sendMessage(msg);
        msg = readMessage();

        if (msg instanceof SshAgentVersionResponse) {
            SshAgentVersionResponse reply = (SshAgentVersionResponse)msg;

            if (reply.getVersion() != 2) {
                throw new IOException("The agent verison is not compatible with verison 2");
            }
        }
        else {
            throw new IOException("The agent did not respond with the appropriate version");
        }
    }

    /**
     * Add a key to the agent
     *
     * @param prvkey      the private key to add
     * @param pubkey      the private keys public key
     * @param description a description of the key
     * @param constraints a set of contraints for key use
     * @throws IOException if an IO error occurs
     */
    public void addKey(SshPrivateKey prvkey, SshPublicKey pubkey,
                       String description, KeyConstraints constraints)
            throws IOException {
        SubsystemMessage msg = new SshAgentAddKey(prvkey, pubkey, description,
                constraints);
        sendMessage(msg);
        msg = readMessage();

        if (!(msg instanceof SshAgentSuccess)) {
            throw new IOException("The key could not be added");
        }
    }

    /**
     * Request a hash and sign operation be performed for a given public key.
     *
     * @param key  the public key of the required private key
     * @param data the data to has and sign
     * @return the hashed and signed data
     * @throws IOException if an IO error occurs
     */
    public byte[] hashAndSign(SshPublicKey key, byte[] data)
            throws IOException {
        SubsystemMessage msg = new SshAgentPrivateKeyOp(key, HASH_AND_SIGN, data);
        sendMessage(msg);
        msg = readMessage();

        if (msg instanceof SshAgentOperationComplete) {
            return ((SshAgentOperationComplete)msg).getData();
        }
        else {
            throw new IOException("The operation failed");
        }
    }

    /**
     * List all the keys on the agent.
     *
     * @return a map of public keys and descriptions
     * @throws IOException if an IO error occurs
     */
    public Map listKeys() throws IOException {
        SubsystemMessage msg = new SshAgentListKeys();
        sendMessage(msg);
        msg = readMessage();

        if (msg instanceof SshAgentKeyList) {
            return ((SshAgentKeyList)msg).getKeys();
        }
        else {
            throw new IOException("The agent responsed with an invalid message");
        }
    }

    /**
     * Lock the agent
     *
     * @param password password that will be required to unlock
     * @return true if the agent was locked, otherwise false
     * @throws IOException if an IO error occurs
     */
    public boolean lockAgent(String password) throws IOException {
        SubsystemMessage msg = new SshAgentLock(password);
        sendMessage(msg);
        msg = readMessage();

        return (msg instanceof SshAgentSuccess);
    }

    /**
     * Unlock the agent
     *
     * @param password the password to unlock
     * @return true if the agent was unlocked, otherwise false
     * @throws IOException if an IO error occurs
     */
    public boolean unlockAgent(String password) throws IOException {
        SubsystemMessage msg = new SshAgentUnlock(password);
        sendMessage(msg);
        msg = readMessage();

        return (msg instanceof SshAgentSuccess);
    }

    /**
     * Request some random data from the remote side
     *
     * @param count the number of bytes needed
     * @return the random data received
     * @throws IOException if an IO error occurs
     */
    public byte[] getRandomData(int count) throws IOException {
        SubsystemMessage msg = new SshAgentRandom(count);
        sendMessage(msg);
        msg = readMessage();

        if (msg instanceof SshAgentRandomData) {
            return ((SshAgentRandomData)msg).getRandomData();
        }
        else {
            throw new IOException("Agent failed to provide the request random data");
        }
    }

    /**
     * Ping the remote side with some random padding data
     *
     * @param padding the padding data
     * @throws IOException if an IO error occurs
     */
    public void ping(byte[] padding) throws IOException {
        SubsystemMessage msg = new SshAgentPing(padding);
        sendMessage(msg);
        msg = readMessage();

        if (msg instanceof SshAgentAlive) {
            if (!Arrays.equals(padding, ((SshAgentAlive)msg).getPadding())) {
                throw new IOException("Agent failed to reply with expected data");
            }
        }
        else {
            throw new IOException("Agent failed to provide the request random data");
        }
    }

    /**
     * Delete a key held by the agent
     *
     * @param key         the public key of the private key to delete
     * @param description the description of the key
     * @throws IOException if an IO error occurs
     */
    public void deleteKey(SshPublicKey key, String description)
            throws IOException {
        SubsystemMessage msg = new SshAgentDeleteKey(key, description);
        sendMessage(msg);
        msg = readMessage();

        if (!(msg instanceof SshAgentSuccess)) {
            throw new IOException("The agent failed to delete the key");
        }
    }

    /**
     * Delete all the keys held by the agent.
     *
     * @throws IOException if an IO error occurs
     */
    public void deleteAllKeys() throws IOException {
        SubsystemMessage msg = new SshAgentDeleteAllKeys();
        sendMessage(msg);
        msg = readMessage();

        if (!(msg instanceof SshAgentSuccess)) {
            throw new IOException("The agent failed to delete all keys");
        }
    }

    /**
     * Send a forwarding notice.
     *
     * @throws IOException if an IO error occurs
     */
    protected void sendForwardingNotice() throws IOException {
        InetAddress addr = InetAddress.getLocalHost();
        SshAgentForwardingNotice msg = new SshAgentForwardingNotice(addr.getHostName(),
                addr.getHostAddress(), 22);
        sendMessage(msg);
    }

    /**
     * Send a subsystem message
     *
     * @param msg the message to send
     * @throws IOException if an IO error occurs
     */
    protected void sendMessage(SubsystemMessage msg) throws IOException {
        log.info("Sending message " + msg.getMessageName());

        byte[] msgdata = msg.toByteArray();
        out.write(ByteArrayWriter.encodeInt(msgdata.length));
        out.write(msgdata);
        out.flush();
    }

    /**
     * Read a single message from the inputstream and convert into a valid
     * subsystem message
     *
     * @return the next available subsystem message
     * @throws InvalidMessageException if the message received is invalid
     */
    protected SubsystemMessage readMessage() throws InvalidMessageException {
        try {
            byte[] lendata = new byte[4];
            byte[] msgdata;
            int len;

            // Read the first 4 bytes to determine the length of the message
            len = 0;

            while (len < 3) {
                len += in.read(lendata, len, lendata.length - len);
            }

            len = (int)ByteArrayReader.readInt(lendata, 0);
            msgdata = new byte[len];
            len = 0;

            while (len < msgdata.length) {
                len += in.read(msgdata, len, msgdata.length - len);
            }

            Integer id = new Integer((int)msgdata[0] & 0xFF);

            if (messages.containsKey(id)) {
                Class cls = (Class)messages.get(id);
                SubsystemMessage msg = (SubsystemMessage)cls.newInstance();
                msg.fromByteArray(msgdata);
                log.info("Received message " + msg.getMessageName());

                return msg;
            }
            else {
                throw new InvalidMessageException("Unrecognised message id " +
                        id.toString());
            }
        }
        catch (Exception ex) {
            throw new InvalidMessageException(ex.getMessage());
        }
    }
}
