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

package com.sshtools.j2ssh.transport.kex;

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;
import com.sshtools.j2ssh.transport.AlgorithmOperationException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.util.Hash;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author $author$
 * @version $Revision$
 */
public class DhGroup1Sha1 extends SshKeyExchange {
    private static Log log = LogFactory.getLog(DhGroup1Sha1.class);
    private static BigInteger g = new BigInteger("2");
    private static BigInteger p = new BigInteger(new byte[]{
        (byte) 0x00, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF,
        (byte) 0xC9,
        (byte) 0x0F, (byte) 0xDA,
        (byte) 0xA2, (byte) 0x21,
        (byte) 0x68,
        (byte) 0xC2, (byte) 0x34,
        (byte) 0xC4, (byte) 0xC6,
        (byte) 0x62,
        (byte) 0x8B, (byte) 0x80,
        (byte) 0xDC, (byte) 0x1C,
        (byte) 0xD1,
        (byte) 0x29, (byte) 0x02,
        (byte) 0x4E, (byte) 0x08,
        (byte) 0x8A,
        (byte) 0x67, (byte) 0xCC,
        (byte) 0x74, (byte) 0x02,
        (byte) 0x0B,
        (byte) 0xBE, (byte) 0xA6,
        (byte) 0x3B, (byte) 0x13,
        (byte) 0x9B,
        (byte) 0x22, (byte) 0x51,
        (byte) 0x4A, (byte) 0x08,
        (byte) 0x79,
        (byte) 0x8E, (byte) 0x34,
        (byte) 0x04, (byte) 0xDD,
        (byte) 0xEF,
        (byte) 0x95, (byte) 0x19,
        (byte) 0xB3, (byte) 0xCD,
        (byte) 0x3A,
        (byte) 0x43, (byte) 0x1B,
        (byte) 0x30, (byte) 0x2B,
        (byte) 0x0A,
        (byte) 0x6D, (byte) 0xF2,
        (byte) 0x5F, (byte) 0x14,
        (byte) 0x37,
        (byte) 0x4F, (byte) 0xE1,
        (byte) 0x35, (byte) 0x6D,
        (byte) 0x6D,
        (byte) 0x51, (byte) 0xC2,
        (byte) 0x45, (byte) 0xE4,
        (byte) 0x85,
        (byte) 0xB5, (byte) 0x76,
        (byte) 0x62, (byte) 0x5E,
        (byte) 0x7E,
        (byte) 0xC6, (byte) 0xF4,
        (byte) 0x4C, (byte) 0x42,
        (byte) 0xE9,
        (byte) 0xA6, (byte) 0x37,
        (byte) 0xED, (byte) 0x6B,
        (byte) 0x0B,
        (byte) 0xFF, (byte) 0x5C,
        (byte) 0xB6, (byte) 0xF4,
        (byte) 0x06,
        (byte) 0xB7, (byte) 0xED,
        (byte) 0xEE, (byte) 0x38,
        (byte) 0x6B,
        (byte) 0xFB, (byte) 0x5A,
        (byte) 0x89, (byte) 0x9F,
        (byte) 0xA5,
        (byte) 0xAE, (byte) 0x9F,
        (byte) 0x24, (byte) 0x11,
        (byte) 0x7C,
        (byte) 0x4B, (byte) 0x1F,
        (byte) 0xE6, (byte) 0x49,
        (byte) 0x28,
        (byte) 0x66, (byte) 0x51,
        (byte) 0xEC, (byte) 0xE6,
        (byte) 0x53,
        (byte) 0x81, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF
    });
    private BigInteger e = null;
    private BigInteger f = null;

    //private static BigInteger q = p.subtract(BigInteger.ONE).divide(g);
    private BigInteger x = null;
    private BigInteger y = null;
    private String clientId;
    private String serverId;
    private byte[] clientKexInit;
    private byte[] serverKexInit;
    private KeyPairGenerator dhKeyPairGen;
    private KeyAgreement dhKeyAgreement;

    /**
     * Creates a new DhGroup1Sha1 object.
     */
    public DhGroup1Sha1() {
    }

    /**
     * @throws IOException
     * @throws AlgorithmNotSupportedException
     */
    protected void onInit() throws IOException {
        transport.getMessageStore().registerMessage(SshMsgKexDhInit.SSH_MSG_KEXDH_INIT,
                SshMsgKexDhInit.class);

        transport.getMessageStore().registerMessage(SshMsgKexDhReply.SSH_MSG_KEXDH_REPLY,
                SshMsgKexDhReply.class);

        try {
            dhKeyPairGen = KeyPairGenerator.getInstance("DH");
            dhKeyAgreement = KeyAgreement.getInstance("DH");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new AlgorithmNotSupportedException(ex.getMessage());
        }
    }

    /**
     * @param clientId
     * @param serverId
     * @param clientKexInit
     * @param serverKexInit
     * @throws IOException
     * @throws AlgorithmOperationException
     * @throws KeyExchangeException
     */
    public void performClientExchange(String clientId, String serverId,
                                      byte[] clientKexInit, byte[] serverKexInit,
                                      boolean firstPacketFollows,
                                      boolean useFirstPacket) throws
            IOException {
        log.info("Starting client side key exchange.");

        this.clientId = clientId;
        this.serverId = serverId;
        this.clientKexInit = clientKexInit;
        this.serverKexInit = serverKexInit;

        //int minBits = g.bitLength();
        //int maxBits = q.bitLength();
        //Random rnd = ConfigurationLoader.getRND();
        // Generate a random bit count for the random x value

        /*int genBits = (int) ( ( (maxBits - minBits + 1) * rnd.nextFloat())
            + minBits);
x = new BigInteger(genBits, rnd);
// Calculate e
e = g.modPow(x, p);*/
        try {
            DHParameterSpec dhSkipParamSpec = new DHParameterSpec(p, g);
            dhKeyPairGen.initialize(dhSkipParamSpec);

            KeyPair dhKeyPair = dhKeyPairGen.generateKeyPair();
            dhKeyAgreement.init(dhKeyPair.getPrivate());
            x = ((DHPrivateKey) dhKeyPair.getPrivate()).getX();
            e = ((DHPublicKey) dhKeyPair.getPublic()).getY();
        }
        catch (InvalidKeyException ex) {
            throw new AlgorithmOperationException("Failed to generate DH value");
        }
        catch (InvalidAlgorithmParameterException ex) {
            throw new AlgorithmOperationException("Failed to generate DH value");
        }

        // Prepare the message
        SshMsgKexDhInit msg = new SshMsgKexDhInit(e);

        // Send it
        try {
            transport.sendMessage(msg, this);
        }
        catch (SshException tpe) {
            throw new KeyExchangeException("Failed to send key exchange initailaztion message");
        }

        int[] messageId = new int[1];
        messageId[0] = SshMsgKexDhReply.SSH_MSG_KEXDH_REPLY;

        SshMsgKexDhReply reply = (SshMsgKexDhReply) transport.readMessage(messageId);

        hostKey = reply.getHostKey();
        signature = reply.getSignature();

        f = reply.getF();

        // Calculate diffe hellman k value
        secret = f.modPow(x, p);

        // Calculate the exchange hash
        calculateExchangeHash();
    }

    /**
     * @param clientId
     * @param serverId
     * @param clientKexInit
     * @param serverKexInit
     * @param prvKey
     * @throws IOException
     * @throws KeyExchangeException
     */
    public void performServerExchange(String clientId, String serverId,
                                      byte[] clientKexInit, byte[] serverKexInit,
                                      SshPrivateKey prvKey,
                                      boolean firstPacketFollows,
                                      boolean useFirstPacket) throws IOException {
        try {
            this.clientId = clientId;
            this.serverId = serverId;
            this.clientKexInit = clientKexInit;
            this.serverKexInit = serverKexInit;

            /*int minBits = g.bitLength();
            int maxBits = q.bitLength();
            Random rnd = ConfigurationLoader.getRND();
            // Generate a random bit count for the random x value
            int genBits = (int) ( ( (maxBits - minBits + 1) * rnd.nextFloat())
                                  + minBits);
            y = new BigInteger(genBits, rnd);*/
            try {
                DHParameterSpec dhSkipParamSpec = new DHParameterSpec(p, g);
                dhKeyPairGen.initialize(dhSkipParamSpec);

                KeyPair dhKeyPair = dhKeyPairGen.generateKeyPair();
                dhKeyAgreement.init(dhKeyPair.getPrivate());
                y = ((DHPrivateKey) dhKeyPair.getPrivate()).getX();
                f = ((DHPublicKey) dhKeyPair.getPublic()).getY();
            }
            catch (InvalidKeyException ex) {
                throw new AlgorithmOperationException("Failed to generate DH y value");
            }
            catch (InvalidAlgorithmParameterException ex) {
                throw new AlgorithmOperationException("Failed to generate DH y value");
            }

            // Calculate f
            //f = g.modPow(y, p);
            // Wait for the e value and calculate the other parameters
            int[] messageId = new int[1];
            messageId[0] = SshMsgKexDhInit.SSH_MSG_KEXDH_INIT;

            SshMsgKexDhInit msg = (SshMsgKexDhInit) transport.readMessage(messageId);

            if (firstPacketFollows && !useFirstPacket) {
                // Ignore the first packet since the guess was incorrect
                msg = (SshMsgKexDhInit) transport.readMessage(messageId);
            }

            e = msg.getE();

            // Calculate k
            secret = e.modPow(y, p);

            hostKey = prvKey.getPublicKey().getEncoded();

            calculateExchangeHash();

            signature = prvKey.generateSignature(exchangeHash);

            SshMsgKexDhReply reply = new SshMsgKexDhReply(hostKey, f, signature);

            transport.sendMessage(reply, this);
        }
        catch (SshException e) {
            throw new KeyExchangeException(e.getMessage());
        }
    }

    /**
     * @throws KeyExchangeException
     */
    protected void calculateExchangeHash() throws KeyExchangeException {
        Hash hash;

        try {
            // Start a SHA hash
            hash = new Hash("SHA");
        }
        catch (NoSuchAlgorithmException nsae) {
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
