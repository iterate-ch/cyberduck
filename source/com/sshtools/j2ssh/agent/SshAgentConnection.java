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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;


/**
 * This class provides a connection using the SSH agent protocol.
 *
 * @author $author$
 * @version $Revision$
 */
public class SshAgentConnection implements Runnable {
    private static Log log = LogFactory.getLog(SshAgentConnection.class);
    InputStream in;
    OutputStream out;
    KeyStore keystore;
    Thread thread;
    Vector forwardingNodes = new Vector();
    Socket socket;

    SshAgentConnection(KeyStore keystore, InputStream in, OutputStream out) {
        this.keystore = keystore;
        this.in = in;
        this.out = out;
        thread = new Thread(this);
        thread.start();
    }

    SshAgentConnection(KeyStore keystore, Socket socket)
            throws IOException {
        this.keystore = keystore;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.socket = socket;
        socket.setSoTimeout(5000);
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Send a success message.
     *
     * @throws IOException if an IO error occurs
     */
    protected void sendAgentSuccess() throws IOException {
        SshAgentSuccess msg = new SshAgentSuccess();
        sendMessage(msg);
    }

    /**
     * Send a failure message
     *
     * @param errorcode the error code of the failure
     * @throws IOException if an IO error occurs
     */
    protected void sendAgentFailure(int errorcode) throws IOException {
        SshAgentFailure msg = new SshAgentFailure(errorcode);
        sendMessage(msg);
    }

    /**
     * Send the version response; this class currently implements version 2
     *
     * @throws IOException if an IO error occurs
     */
    protected void sendVersionResponse() throws IOException {
        SshAgentVersionResponse msg = new SshAgentVersionResponse(2);
        sendMessage(msg);
    }

    /**
     * Send the agents key list to the remote side. This supplies all the
     * public keys.
     *
     * @throws IOException if an IO error occurs
     */
    protected void sendAgentKeyList() throws IOException {
        SshAgentKeyList msg = new SshAgentKeyList(keystore.getPublicKeys());
        sendMessage(msg);
    }

    /**
     * Send the completed signing operation data.
     *
     * @param data the data generating from the signing operation
     * @throws IOException if an IO error occurs
     */
    protected void sendOperationComplete(byte[] data) throws IOException {
        SshAgentOperationComplete msg = new SshAgentOperationComplete(data);
        sendMessage(msg);
    }

    /**
     * Send some random data to the remote side.
     *
     * @param data some random data
     * @throws IOException if an IO error occurs
     */
    protected void sendRandomData(byte[] data) throws IOException {
        SshAgentRandomData msg = new SshAgentRandomData(data);
        sendMessage(msg);
    }

    /**
     * Send the agent alive message. This is sent to test whether the agent is
     * still active
     *
     * @param padding some random padding for the message
     * @throws IOException if an IO error occurs
     */
    protected void sendAgentAlive(byte[] padding) throws IOException {
        SshAgentAlive msg = new SshAgentAlive(padding);
        sendMessage(msg);
    }

    /**
     * Sends a subsystem message.
     *
     * @param msg the subsystem message to send
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
     * Called when a forwarding notice is recceived from the remote side.
     *
     * @param msg the forwarding notice
     */
    protected void onForwardingNotice(SshAgentForwardingNotice msg) {
        forwardingNodes.add(new ForwardingNotice(msg.getRemoteHostname(),
                msg.getRemoteIPAddress(), msg.getRemotePort()));
    }

    /**
     * Called when the remote side requests the version number of this
     * protocol.
     *
     * @param msg the version request message
     * @throws IOException if an IO error occurs
     */
    protected void onRequestVersion(SshAgentRequestVersion msg)
            throws IOException {
        sendVersionResponse();
    }

    /**
     * Called when the remote side adds a key the agent.
     *
     * @param msg the message containing the key
     * @throws IOException if an IO error occurs
     */
    protected void onAddKey(SshAgentAddKey msg) throws IOException {
        if (keystore.addKey(msg.getPrivateKey(), msg.getPublicKey(),
                msg.getDescription(), msg.getKeyConstraints())) {
            sendAgentSuccess();
        }
        else {
            sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_FAILURE);
        }
    }

    /**
     * Called when the remote side requests that all keys be removed from the
     * agent.
     *
     * @param msg the delete all keys message
     * @throws IOException if an IO error occurs
     */
    protected void onDeleteAllKeys(SshAgentDeleteAllKeys msg)
            throws IOException {
        keystore.deleteAllKeys();
        sendAgentSuccess();
    }

    /**
     * Called by the remote side when a list of the agents keys is required
     *
     * @param msg the list all keys message
     * @throws IOException if an IO error occurs
     */
    protected void onListKeys(SshAgentListKeys msg) throws IOException {
        sendAgentKeyList();
    }

    /**
     * Called by the remote side to initiate a private key operation.
     *
     * @param msg the private key operation message
     * @throws IOException if an IO error occurs
     */
    protected void onPrivateKeyOp(SshAgentPrivateKeyOp msg)
            throws IOException {
        try {
            if (msg.getOperation().equals("sign")) {
                sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_KEY_NOT_SUITABLE);
            }
            else if (msg.getOperation().equals("hash-and-sign")) {
                byte[] sig = keystore.performHashAndSign(msg.getPublicKey(),
                        forwardingNodes, msg.getOperationData());
                sendOperationComplete(sig);
            }
            else if (msg.getOperation().equals("decrypt")) {
                sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_KEY_NOT_SUITABLE);
            }
            else if (msg.getOperation().equals("ssh1-challenge-response")) {
                sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_KEY_NOT_SUITABLE);
            }
            else {
                sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_UNSUPPORTED_OP);
            }
        }
        catch (KeyTimeoutException ex) {
            sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_TIMEOUT);
        }
        catch (InvalidSshKeyException ex) {
            sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_KEY_NOT_FOUND);
        }
    }

    /**
     * Called by the remote side to delete a key from the agent
     *
     * @param msg the message containin the key to delete
     * @throws IOException if an IO error occurs
     */
    protected void onDeleteKey(SshAgentDeleteKey msg) throws IOException {
        if (keystore.deleteKey(msg.getPublicKey(), msg.getDescription())) {
            sendAgentSuccess();
        }
        else {
            sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_KEY_NOT_FOUND);
        }
    }

    /**
     * Called by the remote side when the agent is to be locked
     *
     * @param msg the message containing a password
     * @throws IOException if an IO error occurs
     */
    protected void onLock(SshAgentLock msg) throws IOException {
        if (keystore.lock(msg.getPassword())) {
            sendAgentSuccess();
        }
        else {
            sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_DENIED);
        }
    }

    /**
     * Called by the remote side when the agent is to be unlocked
     *
     * @param msg the message containin the password
     * @throws IOException if an IO error occurs
     */
    protected void onUnlock(SshAgentUnlock msg) throws IOException {
        if (keystore.unlock(msg.getPassword())) {
            sendAgentSuccess();
        }
        else {
            sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_DENIED);
        }
    }

    /**
     * Called when a ping message is received
     *
     * @param msg the ping message containing some padding
     * @throws IOException if an IO error occurs
     */
    protected void onPing(SshAgentPing msg) throws IOException {
        sendAgentAlive(msg.getPadding());
    }

    /**
     * Called when the remote side sends a random message
     *
     * @param msg the random message
     * @throws IOException if an IO error occurs
     */
    protected void onRandom(SshAgentRandom msg) throws IOException {
        if (msg.getLength() > 0) {
            byte[] random = new byte[msg.getLength()];
            ConfigurationLoader.getRND().nextBytes(random);
            sendRandomData(random);
        }
        else {
            sendAgentFailure(SshAgentFailure.SSH_AGENT_ERROR_FAILURE);
        }
    }

    /**
     * The connection thread
     */
    public void run() {
        try {
            log.info("Starting agent connection thread");

            byte[] lendata = new byte[4];
            byte[] msgdata;
            int len;
            int read;
            boolean alive = true;

            while (alive) {
                // Read the first 4 bytes to determine the length of the message
                len = 0;

                while (len < lendata.length) {
                    try {
                        read = 0;
                        read = in.read(lendata, len, lendata.length - len);

                        if (read >= 0) {
                            len += read;
                        }
                        else {
                            alive = false;

                            break;
                        }
                    }
                    catch (InterruptedIOException ex) {
                        if (ex.bytesTransferred > 0) {
                            len += ex.bytesTransferred;
                        }
                    }
                }

                if (alive) {
                    len = (int)ByteArrayReader.readInt(lendata, 0);
                    msgdata = new byte[len];
                    len = 0;

                    while (len < msgdata.length) {
                        try {
                            len += in.read(msgdata, len, msgdata.length - len);
                        }
                        catch (InterruptedIOException ex1) {
                            len += ex1.bytesTransferred;
                        }
                    }

                    onMessageReceived(msgdata);
                }
            }
        }
        catch (IOException ex) {
            log.info("The agent connection terminated");
        }
        finally {
            try {
                socket.close();
            }
            catch (Exception ex) {
            }
        }

        log.info("Exiting agent connection thread");
    }

    /**
     * Process a message and route to the handler method
     *
     * @param msgdata the raw message received
     * @throws IOException if an IO error occurs
     */
    protected void onMessageReceived(byte[] msgdata) throws IOException {
        switch ((int)(msgdata[0] & 0xFF)) {
            case SshAgentForwardingNotice.SSH_AGENT_FORWARDING_NOTICE:
                {
                    log.info("Agent forwarding notice received");

                    SshAgentForwardingNotice msg = new SshAgentForwardingNotice();
                    msg.fromByteArray(msgdata);
                    onForwardingNotice(msg);

                    break;
                }

            case SshAgentRequestVersion.SSH_AGENT_REQUEST_VERSION:
                {
                    log.info("Agent version request received");

                    SshAgentRequestVersion msg = new SshAgentRequestVersion();
                    msg.fromByteArray(msgdata);
                    onRequestVersion(msg);

                    break;
                }

            case SshAgentAddKey.SSH_AGENT_ADD_KEY:
                {
                    log.info("Adding key to agent");

                    SshAgentAddKey msg = new SshAgentAddKey();
                    msg.fromByteArray(msgdata);
                    onAddKey(msg);

                    break;
                }

            case SshAgentDeleteAllKeys.SSH_AGENT_DELETE_ALL_KEYS:
                {
                    log.info("Deleting all keys from agent");

                    SshAgentDeleteAllKeys msg = new SshAgentDeleteAllKeys();
                    msg.fromByteArray(msgdata);
                    onDeleteAllKeys(msg);

                    break;
                }

            case SshAgentListKeys.SSH_AGENT_LIST_KEYS:
                {
                    log.info("Listing agent keys");

                    SshAgentListKeys msg = new SshAgentListKeys();
                    msg.fromByteArray(msgdata);
                    onListKeys(msg);

                    break;
                }

            case SshAgentPrivateKeyOp.SSH_AGENT_PRIVATE_KEY_OP:
                {
                    log.info("Performing agent private key operation");

                    SshAgentPrivateKeyOp msg = new SshAgentPrivateKeyOp();
                    msg.fromByteArray(msgdata);
                    onPrivateKeyOp(msg);

                    break;
                }

            case SshAgentDeleteKey.SSH_AGENT_DELETE_KEY:
                {
                    log.info("Deleting key from agent");

                    SshAgentDeleteKey msg = new SshAgentDeleteKey();
                    msg.fromByteArray(msgdata);
                    onDeleteKey(msg);

                    break;
                }

            case SshAgentLock.SSH_AGENT_LOCK:
                {
                    log.info("Locking agent");

                    SshAgentLock msg = new SshAgentLock();
                    msg.fromByteArray(msgdata);
                    onLock(msg);

                    break;
                }

            case SshAgentUnlock.SSH_AGENT_UNLOCK:
                {
                    log.info("Unlocking agent");

                    SshAgentUnlock msg = new SshAgentUnlock();
                    msg.fromByteArray(msgdata);
                    onUnlock(msg);

                    break;
                }

            case SshAgentPing.SSH_AGENT_PING:
                {
                    log.info("Ping Ping Ping Ping Ping");

                    SshAgentPing msg = new SshAgentPing();
                    msg.fromByteArray(msgdata);
                    onPing(msg);

                    break;
                }

            case SshAgentRandom.SSH_AGENT_RANDOM:
                {
                    log.info("Random message received");

                    SshAgentRandom msg = new SshAgentRandom();
                    msg.fromByteArray(msgdata);
                    onRandom(msg);

                    break;
                }

            default:
                throw new IOException("Unrecognized message type " +
                        String.valueOf(msgdata[0]) + " received");
        }
    }
}
