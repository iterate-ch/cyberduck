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

import com.sshtools.j2ssh.transport.MessageNotAvailableException;
import com.sshtools.j2ssh.transport.MessageStoreEOFException;
import com.sshtools.j2ssh.transport.SshMessageStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class ChannelInputStream extends InputStream {
    private static Log log = LogFactory.getLog(ChannelInputStream.class);
    int[] filter;
    byte[] msgdata;
    int currentPos = 0;
    private SshMessageStore messageStore;
    private Integer type = null;
    private int interrupt = 5000;
    private boolean isBlocking = false;
    private Object lock = new Object();
    private Thread blockingThread = null;

    /**
     * Creates a new ChannelInputStream object.
     *
     * @param messageStore
     * @param type
     */
    public ChannelInputStream(SshMessageStore messageStore, Integer type) {
        this.messageStore = messageStore;
        filter = new int[1];
        this.type = type;

        if (type != null) {
            filter[0] = SshMsgChannelExtendedData.SSH_MSG_CHANNEL_EXTENDED_DATA;
        } else {
            filter[0] = SshMsgChannelData.SSH_MSG_CHANNEL_DATA;
        }
    }

    /**
     * Creates a new ChannelInputStream object.
     *
     * @param messageStore
     */
    public ChannelInputStream(SshMessageStore messageStore) {
        this(messageStore, null);
    }

    /**
     *
     *
     * @return
     */
    public int available() {
        int available = 0;

        if (msgdata != null) {
            available = msgdata.length - currentPos;

            if (log.isDebugEnabled() && (available > 0)) {
                log.debug(String.valueOf(available) +
                    " bytes of channel data available");
            }

            available = (available >= 0) ? available : 0;
        }

        if (available == 0) {
            try {
                if (type != null) {
                    SshMsgChannelExtendedData msg = (SshMsgChannelExtendedData) messageStore.peekMessage(filter);
                    available = msg.getChannelData().length;
                } else {
                    SshMsgChannelData msg = (SshMsgChannelData) messageStore.peekMessage(filter);
                    available = msg.getChannelData().length;
                }

                if (log.isDebugEnabled()) {
                    log.debug(String.valueOf(available) +
                        " bytes of channel data available");
                }
            } catch (MessageStoreEOFException mse) {
                log.debug("No bytes available since the MessageStore is EOF");
                available = -1;
            } catch (MessageNotAvailableException mna) {
                available = 0;
            } catch (InterruptedException ex) {
                log.info("peekMessage was interrupted, no data available!");
                available = 0;
            }
        }

        return available;
    }

    /**
     *
     *
     * @throws IOException
     */
    public void close() throws IOException {
        log.info("Closing ChannelInputStream");
        messageStore.close();
    }

    /**
     *
     *
     * @return
     */
    public boolean isClosed() {
        return messageStore.isClosed();
    }

    /**
     *
     *
     * @param interrupt
     */
    public void setBlockInterrupt(int interrupt) {
        this.interrupt = (interrupt < 1000) ? 1000 : interrupt;
    }

    /**
     *
     */
    public void interrupt() {
        messageStore.breakWaiting();
    }

    /**
     *
     *
     * @return
     *
     * @throws java.io.IOException
     * @throws InterruptedIOException
     */
    public int read() throws java.io.IOException {
        try {
            block();

            return msgdata[currentPos++] & 0xFF;
        } catch (MessageStoreEOFException mse) {
            return -1;
        } catch (InterruptedException ex) {
            throw new InterruptedIOException(
                "The thread was interrupted whilst waiting for channel data");
        }
    }

    /**
     *
     *
     * @param b
     * @param off
     * @param len
     *
     * @return
     *
     * @throws IOException
     * @throws IOException
     */
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            block();

            int actual = available();

            if (actual > len) {
                actual = len;
            }

            if (actual > 0) {
                System.arraycopy(msgdata, currentPos, b, off, actual);
                currentPos += actual;
            }

            return actual;
        } catch (MessageStoreEOFException mse) {
            return -1;
        } catch (InterruptedException ex) {
            throw new InterruptedIOException(
                "The thread was interrupted whilst waiting for channel data");
        }
    }

    private void block()
        throws MessageStoreEOFException, InterruptedException, IOException {
        if (msgdata == null) {
            collectNextMessage();
        }

        if (currentPos >= msgdata.length) {
            collectNextMessage();
        }
    }

    private void startBlockingOperation() throws IOException {
        synchronized (lock) {
            if (isBlocking) {
                throw new IOException((("Cannot read from InputStream! " +
                    blockingThread) == null) ? "**NULL THREAD**"
                                             : (blockingThread.getName() +
                    " is currently performing a blocking operation"));
            }

            log.debug("Starting blocking operation");
            blockingThread = Thread.currentThread();
            isBlocking = true;
        }
    }

    private void stopBlockingOperation() throws IOException {
        synchronized (lock) {
            log.debug("Completed blocking operation");
            blockingThread = null;
            isBlocking = false;
        }
    }

    private void collectNextMessage()
        throws MessageStoreEOFException, InterruptedException, IOException {
        // Collect the next message
        startBlockingOperation();

        try {
            if (type != null) {
                SshMsgChannelExtendedData msg = null;

                while ((msg == null) && !isClosed()) {
                    try {
                        log.debug("Waiting for extended channel data");
                        msg = (SshMsgChannelExtendedData) messageStore.getMessage(filter,
                                interrupt);
                    } catch (MessageNotAvailableException ex) {
                        // Ignore the timeout but this allows us to review the
                        // InputStreams state once in a while
                    }
                }

                if (msg != null) {
                    msgdata = msg.getChannelData();
                    currentPos = 0;
                } else {
                    throw new MessageStoreEOFException();
                }
            } else {
                SshMsgChannelData msg = null;

                while ((msg == null) && !isClosed()) {
                    try {
                        log.debug("Waiting for channel data");
                        msg = (SshMsgChannelData) messageStore.getMessage(filter,
                                interrupt);
                    } catch (MessageNotAvailableException ex1) {
                        // Ignore the timeout but this allows us to review the
                        // InputStreams state once in a while
                    }
                }

                if (msg != null) {
                    msgdata = msg.getChannelData();
                    currentPos = 0;
                } else {
                    throw new MessageStoreEOFException();
                }
            }
        } finally {
            stopBlockingOperation();
        }
    }
}
