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
package com.sshtools.j2ssh.transport;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.cipher.SshCipher;
import com.sshtools.j2ssh.transport.compression.SshCompression;
import com.sshtools.j2ssh.transport.hmac.SshHmac;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import java.math.BigInteger;

import java.net.SocketException;

import java.util.Iterator;


class TransportProtocolInputStream {
    private static Log log = LogFactory.getLog(TransportProtocolInputStream.class);
    private long bytesTransfered = 0;
    private BufferedInputStream in;
    private Object sequenceLock = new Object();
    private TransportProtocolCommon transport;
    private TransportProtocolAlgorithmSync algorithms;
    private long sequenceNo = 0;
    private long sequenceWrapLimit = BigInteger.valueOf(2).pow(32).longValue();
    private SshCipher cipher;
    private SshHmac hmac;
    private SshCompression compression;
    int msglen;
    int padlen;
    int read;
    int remaining;
    int cipherlen = 8;
    int maclen = 0;

    //byte[] buffer = new byte[128 * cipherlen];
    ByteArrayWriter message = new ByteArrayWriter();
    byte[] initial = new byte[cipherlen];
    byte[] data = new byte[65535];
    byte[] buffered = new byte[65535];
    int startpos = 0;
    int endpos = 0;

    /**
     * Creates a new TransportProtocolInputStream object.
     *
     * @param transport
     * @param in
     * @param algorithms
     *
     * @throws IOException
     */
    public TransportProtocolInputStream(TransportProtocolCommon transport,
        
    /*Socket socket,*/
    InputStream in, TransportProtocolAlgorithmSync algorithms)
        throws IOException {
        this.transport = transport;

        this.in = new BufferedInputStream(in); //socket.getInputStream());

        this.algorithms = algorithms;
    }

    /**
     *
     *
     * @return
     */
    public synchronized long getSequenceNo() {
        return sequenceNo;
    }

    /**
     *
     *
     * @return
     */
    protected long getNumBytesTransfered() {
        return bytesTransfered;
    }

    /**
     *
     *
     * @return
     */
    protected int available() {
        return endpos - startpos;
    }

    /**
     *
     *
     * @param buf
     * @param off
     * @param len
     *
     * @return
     *
     * @throws IOException
     */
    protected int readBufferedData(byte[] buf, int off, int len)
        throws IOException {
        int read;

        if ((endpos - startpos) < len) {
            // Double check the buffer has enough room for the data
            if ((buffered.length - endpos) < len) {
                /*if (log.isDebugEnabled()) {
                      log.debug("Trimming used data from buffer");
                                 }*/

                // no it does not odds are that the startpos is too high
                System.arraycopy(buffered, startpos, buffered, 0,
                    endpos - startpos);

                endpos -= startpos;

                startpos = 0;

                if ((buffered.length - endpos) < len) {
                    //log.debug("Resizing message buffer");
                    // Last resort resize the buffer to the required length
                    // this should stop any chance of error
                    byte[] tmp = new byte[buffered.length + len];

                    System.arraycopy(buffered, 0, tmp, 0, endpos);

                    buffered = tmp;
                }
            }

            // If there is not enough data then block and read until there is (if still connected)
            while (((endpos - startpos) < len) &&
                    (transport.getState().getValue() != TransportProtocolState.DISCONNECTED)) {
                try {
                    read = in.read(buffered, endpos, (buffered.length - endpos));
                } catch (InterruptedIOException ex) {
                    // We have an interrupted io; inform the event handler
                    read = ex.bytesTransferred;

                    Iterator it = transport.getEventHandlers().iterator();

                    TransportProtocolEventHandler eventHandler;

                    while (it.hasNext()) {
                        eventHandler = (TransportProtocolEventHandler) it.next();

                        eventHandler.onSocketTimeout(transport);
                    }
                }

                if (read < 0) {
                    throw new IOException("The socket is EOF");
                }

                endpos += read;
            }
        }

        try {
            System.arraycopy(buffered, startpos, buf, off, len);
        } catch (Throwable t) {
            System.out.println();
        }

        startpos += len;

        /*if (log.isDebugEnabled()) {
               log.debug("Buffer StartPos=" + String.valueOf(startpos)
                + " EndPos=" + String.valueOf(endpos));
         }*/

        // Try to reset the buffer
        if (startpos >= endpos) {
            //if (log.isDebugEnabled()) {
            // log.debug("Buffer has been reset");
            // }*/
            endpos = 0;

            startpos = 0;
        }

        return len;
    }

    /**
     *
     *
     * @return
     *
     * @throws SocketException
     * @throws IOException
     */
    public byte[] readMessage() throws SocketException, IOException {
        // Reset the message for the next
        message.reset();

        // Read the first byte of this message (this is so we block
        // but we will determine the cipher length before reading all
        read = readBufferedData(initial, 0, cipherlen);

        cipher = algorithms.getCipher();

        hmac = algorithms.getHmac();

        compression = algorithms.getCompression();

        // If the cipher object has been set then make sure
        // we have the correct blocksize
        if (cipher != null) {
            cipherlen = cipher.getBlockSize();
        } else {
            cipherlen = 8;
        }

        // Verify we have enough buffer size for the inital block
        if (initial.length != cipherlen) {
            // Create a temporary array for the new block size and copy
            byte[] tmp = new byte[cipherlen];

            System.arraycopy(initial, 0, tmp, 0, initial.length);

            // Now change the initial buffer to our new array
            initial = tmp;
        }

        // Now read the rest of the first block of data if necersary
        int count = read;

        if (count < initial.length) {
            count += readBufferedData(initial, count, initial.length - count);
        }

        // Record the mac length
        if (hmac != null) {
            maclen = hmac.getMacLength();
        } else {
            maclen = 0;
        }

        // Decrypt the data if we have a valid cipher
        if (cipher != null) {
            initial = cipher.transform(initial);
        }

        // Save the initial data
        message.write(initial);

        // Preview the message length
        msglen = (int) ByteArrayReader.readInt(initial, 0);

        padlen = initial[4];

        // Read, decrypt and save the remaining data
        remaining = (msglen - (cipherlen - 4));

        while (remaining > 0) {
            read = readBufferedData(data, 0,
                    (remaining < data.length)
                    ? ((remaining / cipherlen) * cipherlen)
                    : ((data.length / cipherlen) * cipherlen));
            remaining -= read;

            // Decrypt the data and/or write it to the message
            message.write((cipher == null) ? data
                                           : cipher.transform(data, 0, read),
                0, read);
        }

        synchronized (sequenceLock) {
            if (hmac != null) {
                read = readBufferedData(data, 0, maclen);

                message.write(data, 0, read);

                // Verify the mac
                if (!hmac.verify(sequenceNo, message.toByteArray())) {
                    throw new IOException("Corrupt Mac on input");
                }
            }

            // Increment the sequence no
            if (sequenceNo < sequenceWrapLimit) {
                sequenceNo++;
            } else {
                sequenceNo = 0;
            }
        }

        bytesTransfered += message.size();

        byte[] msg = message.toByteArray();

        // Uncompress the message payload if necersary
        if (compression != null) {
            return compression.uncompress(msg, 5, (msglen + 4) - padlen - 5);
        }

        return msg;
    }
}
