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

import java.io.IOException;
import java.io.OutputStream;

import java.math.BigInteger;

import java.net.Socket;

import java.security.SecureRandom;

import com.sshtools.j2ssh.transport.cipher.SshCipher;
import com.sshtools.j2ssh.transport.compression.SshCompression;
import com.sshtools.j2ssh.transport.hmac.SshHmac;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  Sends messages through the socket first encrypting and then adding message
 *  authentication.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: TransportProtocolOutputStream.java,v 1.10 2002/12/10 00:07:32
 *      martianx Exp $
 */
class TransportProtocolOutputStream {
    //implements Runnable {

    private static Logger log =
            Logger.getLogger(TransportProtocolOutputStream.class);
    private OutputStream out;
    private Socket socket;
    private TransportProtocolAlgorithmSync algorithms;
    private TransportProtocolCommon transport;
    private long sequenceNo = 0;
    private long sequenceWrapLimit = BigInteger.valueOf(2).pow(32).longValue();
    private SecureRandom rnd = new SecureRandom();

    /**
     *  Constructor for the TransportProtocolOutputStream object
     *
     *@param  socket                          The sockets outputstream
     *@param  transport                       Description of the Parameter
     *@param  algorithms                      the synchronized algorithms object
     *@exception  TransportProtocolException  Description of the Exception
     *@throws  TransportProtocolException     if a protocol error occurs
     */
    public TransportProtocolOutputStream(Socket socket,
            TransportProtocolCommon transport,
            TransportProtocolAlgorithmSync algorithms)
             throws TransportProtocolException {
        try {
            this.socket = socket;
            this.out = socket.getOutputStream();
            this.transport = transport;
            this.algorithms = algorithms;
        } catch (IOException ioe) {
            throw new TransportProtocolException("Failed to obtain socket output stream");
        }
    }


    /**
     *  Sends a message
     *
     *@param  msg                             An SshMessage derived class
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    public void sendMessage(SshMessage msg)
             throws TransportProtocolException {
        try {
            // Get the algorithm objects
            algorithms.lock();

            SshCipher cipher = algorithms.getCipher();
            SshHmac hmac = algorithms.getHmac();
            SshCompression compression = algorithms.getCompression();

            // Get the message payload data
            byte msgdata[] = msg.toByteArray();
            int padding = 4;
            int cipherlen = 8;

            // Determine the cipher length
            if (cipher != null) {
                cipherlen = cipher.getBlockSize();
            }

            // Determine the padding length
            padding += ((cipherlen
                    - ((msgdata.length + 5 + padding) % cipherlen)) % cipherlen);

            // Write the data into a byte array
            ByteArrayWriter message = new ByteArrayWriter();

            // Write the packet length field
            message.writeInt(msgdata.length + 1 + padding);

            // Write the padding length
            message.write(padding);

            // Write the message payload
            message.write(msgdata);

            // Create some random data for the padding
            byte pad[] = new byte[padding];
            rnd.nextBytes(pad);

            // Write the padding
            message.write(pad);

            // Get the unencrypted packet data
            byte packet[] = message.toByteArray();
            byte mac[] = null;

            // Generate the MAC
            if (hmac != null) {
                mac = hmac.generate(sequenceNo, packet, 0, packet.length);
            }

            // Do some compression
            if (compression != null) {
                packet = compression.compress(packet);
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

            // Send!
            if (socket.isConnected()) {
                out.write(message.toByteArray());
            }

            out.flush();
            algorithms.release();

            // Increment the sequence no
            if (sequenceNo < sequenceWrapLimit) {
                sequenceNo++;
            } else {
                sequenceNo = 0;
            }
        } catch (IOException ioe) {
            throw new TransportProtocolException("IO Error on socket: "
                    + ioe.getMessage());
        }
    }
}
