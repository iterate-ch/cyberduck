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
package com.sshtools.j2ssh.transport.kex;

import org.apache.log4j.Logger;

import java.math.BigInteger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.transport.MessageAlreadyRegisteredException;
import com.sshtools.j2ssh.transport.TransportProtocol;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.util.Hash;

import java.io.IOException;

/**
 *  Implements the diffie-hellman-group1-sha1 key exchange method as described
 *  in the transport protocol specification [SSH-TRANS]
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public class DhGroup1Sha1
         extends SshKeyExchange {
    private static Logger log = Logger.getLogger(DhGroup1Sha1.class);
    private BigInteger e = null;

    /**
     *  The value f produced during diffie hellman key exchange
     */
    private BigInteger f = null;

    /**
     *  The value g used during diffie hellman key exchange
     */
    private BigInteger g = new BigInteger("2");

    /**
     *  The prime used in diffie hellman key exchange
     */
    private BigInteger p =
            new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234"
            + "C4C6628B80DC1CD129024E088A67CC74"
            + "020BBEA63B139B22514A08798E3404DD"
            + "EF9519B3CD3A431B302B0A6DF25F1437"
            + "4FE1356D6D51C245E485B576625E7EC6"
            + "F44C42E9A637ED6B0BFF5CB6F406B7ED"
            + "EE386BFB5A899FA5AE9F24117C4B1FE6"
            + "49286651ECE65381FFFFFFFFFFFFFFFF", 16);

    /**
     *  The value q used in diffie hellman key exchange
     */
    private BigInteger q =
            new BigInteger("7FFFFFFFFFFFFFFFE487ED5110B4611A"
            + "62633145C06E0E68948127044533E63A"
            + "0105DF531D89CD9128A5043CC71A026E"
            + "F7CA8CD9E69D218D98158536F92F8A1B"
            + "A7F09AB6B6A8E122F242DABB312F3F63"
            + "7A262174D31BF6B585FFAE5B7A035BF6"
            + "F71C35FDAD44CFD2D74F9208BE258FF3"
            + "24943323F67329C0FFFFFFFFFFFFFFFF", 16);

    /**
     *  The random value x used during diffie hellman key exchange
     */
    private BigInteger x = null;

    /**
     *  The value y used during diffie hellman key exchange
     */
    private BigInteger y = null;
    private String clientId;
    private String serverId;
    private TransportProtocol transport;
    private byte clientKexInit[];
    private byte serverKexInit[];


    /**
     *  Constructor for the DhGroup1Sha1 object
     */
    public DhGroup1Sha1() { }


    /**
     *  Called by the framework to initate the key exchange.
     *
     *@param  transport              The transport protocol object for
     *      sending/receiving
     *@throws  KeyExchangeException  if the initialization fails
     */
    public void init(TransportProtocol transport)
             throws KeyExchangeException {
        try {
            this.transport = transport;

            transport.registerMessage(new Integer(SshMsgKexDhInit.SSH_MSG_KEXDH_INIT),
                    SshMsgKexDhInit.class, messageStore);

            transport.registerMessage(new Integer(SshMsgKexDhReply.SSH_MSG_KEXDH_REPLY),
                    SshMsgKexDhReply.class, messageStore);
        } catch (MessageAlreadyRegisteredException e) {
            throw new KeyExchangeException("diffie-hellman-group1-sha1 message already registered!");
        }
    }


    /**
     *  Called by the framework to start the client side of the key exchange
     *  method.
     *
     *@param  clientId                  the clients protocol negotiation
     *      identification
     *@param  serverId                  the servers protocol negotiation
     *      identification
     *@param  clientKexInit             the clients SSH_MSG_KEX_INIT message
     *@param  serverKexInit             the servers SSH_MSG_KEX_INIT message
     *@exception  KeyExchangeException  if a key exchange fails
     */
    public void performClientExchange(String clientId, String serverId,
            byte clientKexInit[], byte serverKexInit[])
             throws IOException {
        log.info("Starting client side key exchange.");

        this.clientId = clientId;
        this.serverId = serverId;
        this.clientKexInit = clientKexInit;
        this.serverKexInit = serverKexInit;

        int minBits = g.bitLength();
        int maxBits = q.bitLength();

        SecureRandom rnd = new SecureRandom();

        // Generate a random bit count for the random x value
        int genBits =
                (int) (((maxBits - minBits + 1) * rnd.nextFloat()) + minBits);

        x = new BigInteger(genBits, rnd);

        // Calculate e
        e = g.modPow(x, p);

        // Prepare the message
        SshMsgKexDhInit msg = new SshMsgKexDhInit(e);

        // Send it
        try {
            transport.sendMessage(msg, this);
        } catch (SshException tpe) {
            throw new KeyExchangeException("Failed to send key exchange initailaztion message");
        }

        int messageId[] = new int[1];
        messageId[0] = SshMsgKexDhReply.SSH_MSG_KEXDH_REPLY;

        SshMsgKexDhReply reply =
                (SshMsgKexDhReply) messageStore.getMessage(messageId);
        hostKey = reply.getHostKey();
        signature = reply.getSignature();

        f = reply.getF();

        // Calculate diffe hellman k value
        secret = f.modPow(x, p);

        // Calculate the exchange hash
        calculateExchangeHash();
    }


    /**
     *  Called by the framework to start the server side of the key exchange
     *
     *@param  clientId                  the clients protocol negotiation
     *      identification
     *@param  serverId                  the servers protocol negotiation
     *      identification
     *@param  clientKexInit             the clients SSH_MSG_KEX_INIT message
     *@param  serverKexInit             the servers SSH_MSG_KEX_INIT message
     *@param  prvKey                    the servers private host key for signing
     *@exception  KeyExchangeException  if key exchange fails
     */
    public void performServerExchange(String clientId, String serverId,
            byte clientKexInit[],
            byte serverKexInit[], SshPrivateKey prvKey)
             throws IOException {
        try {
            this.clientId = clientId;
            this.serverId = serverId;
            this.clientKexInit = clientKexInit;
            this.serverKexInit = serverKexInit;

            int minBits = g.bitLength();
            int maxBits = q.bitLength();

            SecureRandom rnd = new SecureRandom();

            // Generate a random bit count for the random x value
            int genBits =
                    (int) (((maxBits - minBits + 1) * rnd.nextFloat()) + minBits);

            y = new BigInteger(genBits, rnd);

            // Calculate f
            f = g.modPow(y, p);

            // Wait for the e value and calculate the other parameters
            int messageId[] = new int[1];
            messageId[0] = SshMsgKexDhInit.SSH_MSG_KEXDH_INIT;

            SshMsgKexDhInit msg =
                    (SshMsgKexDhInit) messageStore.getMessage(messageId);

            e = msg.getE();

            // Calculate k
            secret = e.modPow(y, p);

            hostKey = prvKey.getPublicKey().getEncoded();

            calculateExchangeHash();

            signature = prvKey.generateSignature(exchangeHash);

            SshMsgKexDhReply reply =
                    new SshMsgKexDhReply(hostKey, f, signature);

            transport.sendMessage(reply, this);
        } catch (SshException e) {
            throw new KeyExchangeException(e.getMessage());
        }
    }


    /**
     *  This method is during key exchange to calculate the exchange hash The
     *  exchange hash is computed as the concatenation of the following: The
     *  clients identification string, The servers identification string, The
     *  payload of the clients SSH_MSG_KEXINIT, The payload of the servers
     *  SSH_MSG_KEX_INIT, The servers host key, The diffie hellman e value, The
     *  diffie hellman f value, The diffie hellman k value.
     *
     *@exception  KeyExchangeException  if key exchange fails
     */
    protected void calculateExchangeHash()
             throws KeyExchangeException {
        Hash hash;

        try {
            // Start a SHA hash
            hash = new Hash("SHA");
        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyExchangeException("SHA algorithm not supported");
        }

        int i;

        // The local software version comments
        hash.putString(clientId);

        // The remote software version comments
        hash.putString(serverId);

        // The local kex init payload
        hash.putInt(clientKexInit.length);
        hash.putBytes(clientKexInit);

        // The remote kex init payload
        hash.putInt(serverKexInit.length);
        hash.putBytes(serverKexInit);

        // The host key
        hash.putInt(hostKey.length);
        hash.putBytes(hostKey);

        // The diffie hellman e value
        hash.putBigInteger(e);

        // The diffie hellman f value
        hash.putBigInteger(f);

        // The diffie hellman k value
        hash.putBigInteger(secret);

        // Do the final output
        exchangeHash = hash.doFinal();
    }
}
