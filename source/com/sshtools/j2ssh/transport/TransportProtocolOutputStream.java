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

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.cipher.SshCipher;
import com.sshtools.j2ssh.transport.compression.SshCompression;
import com.sshtools.j2ssh.transport.hmac.SshHmac;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;

import java.math.BigInteger;

import java.util.Random;


class TransportProtocolOutputStream {
    //implements Runnable {
    private static Log log = LogFactory.getLog(TransportProtocolOutputStream.class);
    private OutputStream out;

    //private Socket socket;
    private TransportProtocolAlgorithmSync algorithms;
    private TransportProtocolCommon transport;
    private long sequenceNo = 0;
    private long sequenceWrapLimit = BigInteger.valueOf(2).pow(32).longValue();
    private Random rnd = ConfigurationLoader.getRND();
    private long bytesTransfered = 0;

    /**
     * Creates a new TransportProtocolOutputStream object.
     *
     * @param out
     * @param transport
     * @param algorithms
     *
     * @throws TransportProtocolException
     */
    public TransportProtocolOutputStream( /*Socket socket,*/
        OutputStream out, TransportProtocolCommon transport,
        TransportProtocolAlgorithmSync algorithms)
        throws TransportProtocolException {
        // try {
        //this.socket = socket;
        this.out = out; //socket.getOutputStream();
        this.transport = transport;
        this.algorithms = algorithms;

        /* } catch (IOException ioe) {
               throw new TransportProtocolException(
          "Failed to obtain socket output stream");
           }*/
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
     * @param msg
     *
     * @throws TransportProtocolException
     */
    protected synchronized void sendMessage(SshMessage msg)
        throws TransportProtocolException {
        try {
            // Get the algorithm objects
            algorithms.lock();

            SshCipher cipher = algorithms.getCipher();
            SshHmac hmac = algorithms.getHmac();
            SshCompression compression = algorithms.getCompression();

            // Write the data into a byte array
            ByteArrayWriter message = new ByteArrayWriter();

            // Get the message payload data
            byte[] msgdata = msg.toByteArray();

            //int payload = msgdata.length;
            int padding = 4;
            int cipherlen = 8;

            // Determine the cipher length
            if (cipher != null) {
                cipherlen = cipher.getBlockSize();
            }

            // Compress the payload if necersary
            if (compression != null) {
                msgdata = compression.compress(msgdata, 0, msgdata.length);
            }

            //Determine the padding length
            padding += ((cipherlen -
            ((msgdata.length + 5 + padding) % cipherlen)) % cipherlen);

            // Write the packet length field
            message.writeInt(msgdata.length + 1 + padding);

            // Write the padding length
            message.write(padding);

            // Write the message payload
            message.write(msgdata, 0, msgdata.length);

            // Create some random data for the padding
            byte[] pad = new byte[padding];
            rnd.nextBytes(pad);

            // Write the padding
            message.write(pad);

            // Get the unencrypted packet data
            byte[] packet = message.toByteArray();
            byte[] mac = null;

            // Generate the MAC
            if (hmac != null) {
                mac = hmac.generate(sequenceNo, packet, 0, packet.length);
            }

            // Perfrom encrpytion
            if (cipher != null) {
                packet = cipher.transform(packet);
            }

            // Reset the message
            message.reset();

            // Write the packet data
            message.write(packet);

            // Combine the packet and MAC
            if (mac != null) {
                message.write(mac);
            }

            bytesTransfered += message.size();

            // Send!
            out.write(message.toByteArray());

            out.flush();
            algorithms.release();

            // Increment the sequence no
            if (sequenceNo < sequenceWrapLimit) {
                sequenceNo++;
            } else {
                sequenceNo = 0;
            }
        } catch (IOException ioe) {
            if (transport.getState().getValue() != TransportProtocolState.DISCONNECTED) {
                throw new TransportProtocolException("IO Error on socket: " +
                    ioe.getMessage());
            }
        }
    }
}
