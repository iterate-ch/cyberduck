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
package com.sshtools.j2ssh.subsystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.SshThread;
import com.sshtools.j2ssh.connection.ChannelState;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.InvalidMessageException;
import com.sshtools.j2ssh.util.StartStopState;


/**
 * @author $author$
 * @version $Revision$
 */
public abstract class SubsystemClient implements Runnable {
    private static Log log = LogFactory.getLog(SubsystemClient.class);
    private InputStream in;
    private OutputStream out;
    private Thread thread;
    private String name;
    private StartStopState state = new StartStopState(StartStopState.STOPPED);

    /**  */
    protected SubsystemMessageStore messageStore;

    /**  */
    protected SessionChannelClient session;

    /**
     * Creates a new SubsystemClient object.
     *
     * @param name
     */
    public SubsystemClient(String name) {
        this.name = name;
        messageStore = new SubsystemMessageStore();
    }

    /**
     * Creates a new SubsystemClient object.
     *
     * @param name
     * @param messageStore
     */
    public SubsystemClient(String name, SubsystemMessageStore messageStore) {
        this.name = name;
        this.messageStore = messageStore;
    }

    /**
     * @return
     */
    public boolean isClosed() {
        return state.getValue() == StartStopState.STOPPED;
    }

    /**
     * @param session
     */
    public void setSessionChannel(SessionChannelClient session) {
        this.session = session;
        this.in = session.getInputStream();
        this.out = session.getOutputStream();
        session.setName(name);
    }

    /**
     * @return
     */
    public SessionChannelClient getSessionChannel() {
        return this.session;
    }

    /**
     * @return
     * @throws IOException
     */
    public boolean start() throws IOException {
        thread = new SshThread(this, name + " subsystem", true);

        if (session == null) {
            throw new IOException("No valid session is attached to the subsystem!");
        }

        if (session.getState().getValue() != ChannelState.CHANNEL_OPEN) {
            throw new IOException("The session is not open!");
        }

        thread.start();

        return onStart();
    }

    /**
     * @return
     * @throws IOException
     */
    protected abstract boolean onStart() throws IOException;

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param msg
     * @throws InvalidMessageException
     * @throws IOException
     */
    protected void sendMessage(SubsystemMessage msg) throws InvalidMessageException, IOException {

        if (log.isDebugEnabled()) {
            log.debug("Sending " + msg.getMessageName() + " subsystem message");
        }

        byte[] msgdata = msg.toByteArray();

        // Write the message length
        out.write(ByteArrayWriter.encodeInt(msgdata.length));

        // Write the message data
        out.write(msgdata);
    }

    /**
     *
     */
    public void run() {
        int read;
        int len;
        int pos;
        byte[] buffer = new byte[4];
        byte[] msg;
        state.setValue(StartStopState.STARTED);

        try {
            // read the first four bytes of data to determine the susbsytem
            // message length
            while ((state.getValue() == StartStopState.STARTED) &&
                    (session.getState().getValue() == ChannelState.CHANNEL_OPEN)) {
                read = in.read(buffer);

                if (read > 0) {
                    len = (int)ByteArrayReader.readInt(buffer, 0);
                    msg = new byte[len];
                    pos = 0;

                    while (pos < len) {
                        read = in.read(msg, pos, msg.length - pos);

                        if (read > 0) {
                            pos += read;
                        }
                        else if (read == -1) {
                            break;
                        }
                    }

                    messageStore.addMessage(msg);
                    msg = null;
                }
                else if (read == -1) {
                    break;
                }
            }
        }
        catch (IOException ioe) {
            log.fatal("Subsystem message loop failed!", ioe);
        }
        finally {
            state.setValue(StartStopState.STOPPED);
        }

        thread = null;
    }

    /**
     * @throws IOException
     */
    public void stop() throws IOException {
        state.setValue(StartStopState.STOPPED);
        in.close();
        out.close();
        session.close();
    }
}
