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
package com.sshtools.j2ssh.transport;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.math.BigInteger;

import java.net.Socket;

import com.sshtools.j2ssh.transport.cipher.SshCipher;
import com.sshtools.j2ssh.transport.compression.SshCompression;
import com.sshtools.j2ssh.transport.hmac.SshHmac;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.util.OpenClosedState;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 *  Waits on the socket for data, decrypts and performs message authentication
 *  and then routes the message to the transport protocol for processing.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: TransportProtocolInputStream.java,v 1.11 2002/12/10 00:07:32
 *      martianx Exp $
 */
class TransportProtocolInputStream
         implements Runnable {
    private static Logger log =
            Logger.getLogger(TransportProtocolInputStream.class);

    /**
     *  flag to indicate whether the protocol is disconnecting
     */
    public boolean disconnecting = false;
    private BufferedInputStream in;
    private Object sequenceLock = new Object();
    private Socket socket;
    private Thread thread;
    private TransportProtocolAlgorithmSync algorithms;
    private TransportProtocolCommon listener;
    private OpenClosedState state = new OpenClosedState(OpenClosedState.CLOSED);
    private long sequenceNo = 0;
    private long sequenceWrapLimit = BigInteger.valueOf(2).pow(32).longValue();


    /**
     *  Constructor for the TransportProtocolInputStream object
     *
     *@param  socket           The socket input stream
     *@param  listener         The transport layer for routing messages
     *@param  algorithms       The algorithms in use
     *@exception  IOException  Description of the Exception
     *@throws  IOException     if the InputStream fails to initialize
     */
    public TransportProtocolInputStream(Socket socket,
            TransportProtocolCommon listener,
            TransportProtocolAlgorithmSync algorithms)
             throws IOException {
        this.socket = socket;
        this.in = new BufferedInputStream(socket.getInputStream());
        this.listener = listener;
        this.algorithms = algorithms;
    }


    /**
     *  Gets the sequence no of the last message sent
     *
     *@return    The sequenceNo value
     */
    public synchronized long getSequenceNo() {
        return sequenceNo;
    }


    /**
     *  Main processing method for the SshInputStream object
     */
    public void run() {
        int msglen;
        int padlen;
        int read;
        int remaining;
        int cipherlen = 8;
        int maclen = 0;
        int messageId;

        state.setValue(OpenClosedState.OPEN);

        // Create an input buffer about 1024 bytes but make sure that it
        // is always is a multiple of the block size
        byte buffer[] = new byte[128 * cipherlen];
        ByteArrayWriter message = new ByteArrayWriter();
        byte initial[] = new byte[cipherlen];
        byte data[];

        SshCipher cipher;
        SshHmac hmac;
        SshCompression compression;

        while (state.getValue()==OpenClosedState.OPEN) {
            try {
                // Read the first byte of this message (this is so we block
                // but we will determine the cipher length before reading all
                read = in.read(initial, 0, 1);

                // Make sure we have not closed or reached eof
                if ((read <= 0) && !disconnecting) {
                    log.warn("Socket InputStream is EOF");

                    //listener.onChildThreadException(new Exception("Error reading from socket 1"));
                    return;
                }

                algorithms.lock();
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
                if(initial.length != cipherlen) {
                  // Create a temporary array for the new block size and copy
                  byte tmp[] = new byte[cipherlen];
                  System.arraycopy(initial,0,tmp,0,initial.length);
                  // Now change the initial buffer to our new array
                  initial = tmp;
               }

                // Now read the rest of the first block of data
                read = in.read(initial, 1, cipherlen - 1);

                // Make sure that our buffer size is a multiple of blocksize
                if ((buffer.length % cipherlen) != 0) {
                    buffer = new byte[128 * cipherlen];
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
                msglen = ByteArrayReader.readInt(initial, 0);
                padlen = initial[4];
                messageId = initial[5];

                // Read, decrypt and save the remaining data
                remaining = (msglen - (cipherlen - 4));

                // Loop collecting data untill we have the correct number of
                // bytes
                while (remaining > 0) {
                    // Read up to buffer.length or remaining whichever is lower
                    if (remaining > buffer.length) {
                        read = in.read(buffer);
                    } else {
                        read = in.read(buffer, 0, remaining);
                    }

                    // Check that nothing went wrong on the socket
                    if (read > 0) {
                        // Record how many bytes weve received
                        remaining -= read;

                        // Decrypt the data and/or write it to the message
                        if (cipher != null) {
                            message.write(cipher.transform(buffer, 0, read));
                        } else {
                            message.write(buffer, 0, read);
                        }
                    } else if (!disconnecting) {
                        log.warn("Socket InputStream is EOF");

                        //listener.onChildThreadException(new Exception("Error reading from socket 2"));
                        return;
                    }
                }

                // End of while
                synchronized (sequenceLock) {
                    if (hmac != null) {
                        read = in.read(buffer, 0, maclen);

                        message.write(buffer, 0, read);

                        // Verify the mac
                        if (!hmac.verify(sequenceNo, message.toByteArray())) {
                            listener.onCorruptMac();
                        }
                    }

                    // Increment the sequence no
                    if (sequenceNo < sequenceWrapLimit) {
                        sequenceNo++;
                    } else {
                        sequenceNo = 0;
                    }
                }

                // End of sequence no lock
                // Trim the packet length, padding length and padding fields
                ByteArrayReader msg =
                        new ByteArrayReader(message.toByteArray());

                // Reset the message for the next
                message.reset();

                // Release the algorithms
                algorithms.release();

                // We now have a completed message
                listener.onMessageData(new Integer(messageId), msg);
            } catch (Exception e) {
                if (state.getValue()==OpenClosedState.OPEN) {
                    log.warn("The Transport Protocol InputStream failed", e);
                    listener.stop();
                }
            }
        }

        thread = null;
    }


    /**
     *  Closes the Inputstream.
     */
    public void close() {
        try {
            state.setValue(OpenClosedState.CLOSED);
            in.close();

        } catch (IOException ioe) {
        }

        log.info("Transport protocol inputstream thread is exiting");
    }


    /**
     *  Starts the inputstream.
     */
    protected void open() {
        log.info("Starting TransportProtocolInputStream thread");
        thread = new Thread(this);
        if(ConfigurationLoader.isContextClassLoader())
          thread.setContextClassLoader(ConfigurationLoader.getContextClassLoader());
        thread.setDaemon(true);
        thread.start();
    }
}
