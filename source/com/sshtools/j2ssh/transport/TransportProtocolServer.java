/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.ServerConfiguration;
import com.sshtools.j2ssh.transport.cipher.SshCipher;
import com.sshtools.j2ssh.transport.cipher.SshCipherFactory;
import com.sshtools.j2ssh.transport.hmac.SshHmac;
import com.sshtools.j2ssh.transport.hmac.SshHmacFactory;
import com.sshtools.j2ssh.transport.kex.KeyExchangeException;
import com.sshtools.j2ssh.transport.kex.SshKeyExchange;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import com.sshtools.j2ssh.transport.publickey.SshtoolsPrivateKeyFormat;
import com.sshtools.j2ssh.transport.publickey.SshKeyPairFactory;


/**
 * Implements the server side of the Transport Protocol
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class TransportProtocolServer
    extends TransportProtocolCommon {
    private static Logger log = Logger.getLogger(TransportProtocolServer.class);
    private Map acceptServices = new HashMap();
    private ServerConfiguration config =
        ConfigurationLoader.getServerConfiguration();
    private boolean refuse = false;

    /**
     * Constructor
     *
     * @throws TransportProtocolException if a protocol error occurs
     */
    public TransportProtocolServer()
                            throws IOException {
        if (config==null) {
            throw new TransportProtocolException("No valid server configuration was found!");
        }
    }

    /**
     * Constructor
     *
     * @throws TransportProtocolException if a protocol error occurs
     */
    public TransportProtocolServer(boolean refuse)
                            throws IOException {
        this();
        this.refuse = refuse;
        if (config==null) {
            throw new TransportProtocolException("No valid server configuration was found!");
        }
    }

    protected void onDisconnect() {
      acceptServices.clear();
    }

    /**
     * Adds the service to the available services that the client can request
     *
     * @param service an uninitialized service
     *
     * @throws TransportProtocolException if a protocol error occurs
     */
    public void acceptService(Service service)
                       throws IOException {
        acceptServices.put(service.getServiceName(), service);
    }

    /**
     * Refused the current connection due to too many connections
     *
     * @throws TransportProtocolException if a protocol error occurs
     * @throws ServiceOperationException if a service operation fails
     */
    public void refuseConnection()
                          throws IOException {
        log.info("Refusing connection");

        // disconnect with max_connections reason
        sendDisconnect(SshMsgDisconnect.TOO_MANY_CONNECTIONS,
                       "Too many connections");
    }

    /**
     * Registers the server side messages
     *
     * @throws MessageAlreadyRegisteredException if the message id is already
     *         registered
     */
    public void registerTransportMessages()
                                   throws MessageAlreadyRegisteredException {
        transportMessages.put(new Integer(SshMsgServiceRequest.SSH_MSG_SERVICE_REQUEST),
                              SshMsgServiceRequest.class);
    }

    /**
     * Starts the transport protocols binary messaging
     *
     * @throws TransportProtocolException if a protocol error occurs
     * @throws ServiceOperationException if an operation fails
     */
    protected void startBinaryPacketProtocol()
                                      throws IOException {
        if(refuse) {
          sendKeyExchangeInit();
          sshIn.open();
          refuseConnection();
        }
        else
          super.startBinaryPacketProtocol();

    }

    /**
     * Returns the decryption algorithm
     *
     * @return the current decryption algorithm name
     *
     * @throws AlgorithmNotAgreedException if an algorithm cannot be agreed
     */
    protected String getDecryptionAlgorithm()
                                     throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedCSEncryption(),
                                  serverKexInit.getSupportedCSEncryption());
    }

    /**
     * Returns the encryption algorithm to use
     *
     * @return the current encryption algorithm name
     *
     * @throws AlgorithmNotAgreedException if an algorithm cannot be agreed
     */
    protected String getEncryptionAlgorithm()
                                     throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedSCEncryption(),
                                  serverKexInit.getSupportedSCEncryption());
    }

    /**
     * Returns the inputstream comrpression algorithm
     *
     * @return the compression algorithm name
     *
     * @throws AlgorithmNotAgreedException if an algorithm cannot be agreed
     */
    protected String getInputStreamCompAlgortihm()
                                          throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedCSComp(),
                                  serverKexInit.getSupportedCSComp());
    }

    /**
     * Returns the inputstream mac algorithm
     *
     * @return the message authentication algortihm name
     *
     * @throws AlgorithmNotAgreedException if an algorithm cannot be agreed
     */
    protected String getInputStreamMacAlgorithm()
                                         throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedCSMac(),
                                  serverKexInit.getSupportedCSMac());
    }

    /**
     * Sets the local ident string
     */
    protected void setLocalIdent() {
        serverIdent =
            "SSH-" + PROTOCOL_VERSION + "-" + SOFTWARE_VERSION_COMMENTS
            + " [SERVER]";
    }

    /**
     * Gets the local ident string
     *
     * @return the protocol negotiation indentification string
     */
    protected String getLocalIdent() {
        return serverIdent;
    }

    /**
     * Sets the local kex init message
     *
     * @param msg the local key exchange init message
     */
    protected void setLocalKexInit(SshMsgKexInit msg) {
        log.debug(msg.toString());
        serverKexInit = msg;
    }

    /**
     * Gets the local kex init message
     *
     * @return the local key exchange init message
     */
    protected SshMsgKexInit getLocalKexInit() {
        return serverKexInit;
    }

    /**
     * Get the outputstreams compression algorithm
     *
     * @return the outputstreams compression algorithm name
     *
     * @throws AlgorithmNotAgreedException if an algorithm cannot be agreed
     */
    protected String getOutputStreamCompAlgorithm()
                                           throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedSCComp(),
                                  serverKexInit.getSupportedSCComp());
    }

    /**
     * Returns the outputstream mac algorithm
     *
     * @return the outputstreams message authentication algorithm name
     *
     * @throws AlgorithmNotAgreedException if an algorithm cannot be agreed
     */
    protected String getOutputStreamMacAlgorithm()
                                          throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedSCMac(),
                                  serverKexInit.getSupportedSCMac());
    }

    /**
     * Sets the remote sides indent string
     *
     * @param ident the remote sides protocol negotiation identification string
     */
    protected void setRemoteIdent(String ident) {
        clientIdent = ident;
    }

    /**
     * Get the remote ident
     *
     * @return the remote computers protocol negotiation identification string
     */
    protected String getRemoteIdent() {
        return clientIdent;
    }

    /**
     * Sets the remote kex init message
     *
     * @param msg the remote computers key exchange init message
     */
    protected void setRemoteKexInit(SshMsgKexInit msg) {
        log.debug(msg.toString());
        clientKexInit = msg;
    }

    /**
     * Returns the remote kex init
     *
     * @return the remote computers key exchange init message
     */
    protected SshMsgKexInit getRemoteKexInit() {
        return clientKexInit;
    }

    /**
     * Creates the local kex init message
     *
     * @return the initialized kex init message
     *
     * @throws TransportProtocolException if a protocol error occurs
     */
    protected SshMsgKexInit createLocalKexInit()
                                        throws IOException {
        SshMsgKexInit msg = new SshMsgKexInit(properties);

        /** Set the available server host keys instead of the supported ones */
        ServerConfiguration config =
            ConfigurationLoader.getServerConfiguration();

        if (config==null) {
            throw new TransportProtocolException("No server configuration available");
        } else {
            Map keys = config.getServerHostKeys();

            if (keys.size()>0) {
                Iterator it = keys.entrySet().iterator();
                List available = new ArrayList();
                while(it.hasNext()) {
                  Map.Entry entry = (Map.Entry)it.next();
                  if(SshKeyPairFactory.supportsKey(entry.getKey().toString()))
                    available.add(entry.getKey());
                  else
                    log.warn("Server host key algorithm '" + entry.getKey().toString() + "' not supported");

                }

                if(available.size() > 0)
                  msg.setSupportedPK(available);
                else
                  throw new TransportProtocolException("No server host keys available");
            } else {
                throw new TransportProtocolException("No server host keys available");
            }
        }

        return msg;
    }

    /**
     * Begins key exchange
     *
     * @param kex the key exchange instance in progress
     *
     * @throws TransportProtocolException if a protocol error occurs
     * @throws KeyExchangeException if key exchange fails
     */
    protected void performKeyExchange(SshKeyExchange kex)
                               throws IOException {
        // Determine the public key algorithm and obtain an instance
        String keyType =
            determineAlgorithm(clientKexInit.getSupportedPublicKeys(),
                               serverKexInit.getSupportedPublicKeys());

        // Create an instance of the public key from the factory
        //SshKeyPair pair = SshKeyPairFactory.newInstance(keyType);
        // Get the configuration and get the relevant host key
        ServerConfiguration server =
            ConfigurationLoader.getServerConfiguration();

        if (server==null) {
            throw new TransportProtocolException("Server configuration unavailable");
        }

        Map keys = server.getServerHostKeys();

        Iterator it = keys.entrySet().iterator();
        String privateKeyFile = null;

        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();

            if (entry.getKey().equals(keyType)) {
                privateKeyFile = entry.getValue().toString();
                break;
            }
        }

        // Perform the key exchange with the determined host key
        if (privateKeyFile!=null) {
            InputStream in;

            //byte pubData[] = null;
            byte prvData[] = null;

            SshPrivateKey pk = null;

           in = new FileInputStream(privateKeyFile);
            byte buffer[] = new byte[in.available()];
            in.read(buffer);
            in.close();

            SshPrivateKeyFile pkf =
                SshPrivateKeyFile.parse(buffer,
                                        new SshtoolsPrivateKeyFormat());
            pk = pkf.toPrivateKey(null);

            kex.performServerExchange(clientIdent, serverIdent,
                                  clientKexInit.toByteArray(),
                                  serverKexInit.toByteArray(), pk);

            return;
        }

        throw new KeyExchangeException("No host key available for the determined public key algorithm");
    }

    /**
     * Handles a received message
     *
     * @param msg the message received
     *
     * @throws TransportProtocolException if a protocol error occurs
     * @throws ServiceOperationException if a service operation fails
     */
    protected void onMessageReceived(SshMessage msg)
                              throws IOException {
        switch (msg.getMessageId()) {
            case SshMsgServiceRequest.SSH_MSG_SERVICE_REQUEST: {
                onMsgServiceRequest((SshMsgServiceRequest) msg);

                break;
            }
        }
    }

    /**
     * Sets up the new keys for the IOStreams
     *
     * @param encryptCSKey the client->server encryption key
     * @param encryptCSIV the client->server encrytioon IV
     * @param encryptSCKey the server->client encryption key
     * @param encryptSCIV the server->client encryption IV
     * @param macCSKey the client->server message authentication key
     * @param macSCKey the server->client message authentication key
     *
     * @throws AlgorithmNotAgreedException if an algorithm cannot be agreed
     * @throws AlgorithmOperationException if an algorithm fails
     * @throws AlgorithmNotSupportedException if an algorithm agreed is not
     *         supported
     * @throws AlgorithmInitializationException if an algorithm fails to
     *         initialize
     */
    protected void setupNewKeys(byte encryptCSKey[], byte encryptCSIV[],
                                byte encryptSCKey[], byte encryptSCIV[],
                                byte macCSKey[], byte macSCKey[])
                         throws AlgorithmNotAgreedException,
                                AlgorithmOperationException,
                                AlgorithmNotSupportedException,
                                AlgorithmInitializationException {
        // Setup the encryption cipher
        SshCipher sshCipher =
            SshCipherFactory.newInstance(getEncryptionAlgorithm());
        sshCipher.init(SshCipher.ENCRYPT_MODE, encryptSCIV, encryptSCKey);
        algorithmsOut.setCipher(sshCipher);

        // Setup the decryption cipher
        sshCipher = SshCipherFactory.newInstance(getDecryptionAlgorithm());
        sshCipher.init(SshCipher.DECRYPT_MODE, encryptCSIV, encryptCSKey);
        algorithmsIn.setCipher(sshCipher);

        // Create and put our macs into operation
        SshHmac hmac =
            SshHmacFactory.newInstance(getOutputStreamMacAlgorithm());
        hmac.init(macSCKey);
        algorithmsOut.setHmac(hmac);

        hmac = SshHmacFactory.newInstance(getInputStreamMacAlgorithm());
        hmac.init(macCSKey);
        algorithmsIn.setHmac(hmac);
    }

    /**
     * Handles the request to start a service.
     *
     * @param msg the service request
     *
     * @throws TransportProtocolException if a protocol error occurs
     * @throws ServiceOperationException if a service operation fails
     */
    private void onMsgServiceRequest(SshMsgServiceRequest msg)
                              throws IOException {
        if (acceptServices.containsKey(msg.getServiceName())) {
            Service service =
                (Service) acceptServices.get(msg.getServiceName());
            service.init(Service.ACCEPTING_SERVICE, this, exchangeHash, null);
            service.start();
        } else {
            this.sendDisconnect(SshMsgDisconnect.SERVICE_NOT_AVAILABLE,
                                msg.getServiceName() + " is not available");
        }
    }
}
