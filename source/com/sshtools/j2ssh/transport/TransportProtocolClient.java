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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.IOException;

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.transport.cipher.SshCipher;
import com.sshtools.j2ssh.transport.cipher.SshCipherFactory;
import com.sshtools.j2ssh.transport.hmac.SshHmac;
import com.sshtools.j2ssh.transport.hmac.SshHmacFactory;
import com.sshtools.j2ssh.transport.kex.KeyExchangeException;
import com.sshtools.j2ssh.transport.kex.SshKeyExchange;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeySignatureException;
import com.sshtools.j2ssh.transport.publickey.SshKeyPair;
import com.sshtools.j2ssh.transport.publickey.SshKeyPairFactory;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.util.InvalidStateException;

/**
 *  Implements the client side of the SSH transport protocol. Specifically this
 *  class initiates client side key exchange operations and provides a mechanism
 *  to request SSH services from the remote server.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: TransportProtocolClient.java,v 1.18 2002/12/10 00:07:32
 *      martianx Exp $
 */
public class TransportProtocolClient
         extends TransportProtocolCommon {
    /**
     *  The public key object used in host key verification
     */
    protected SshPublicKey pk;
    private HostKeyVerification hosts;
    private Map services = new HashMap();


    /**
     *  Constructor for the SshTransportProtocolClient object
     *
     *@param  hosts                           a host key verification
     *      implementation for user interaction to verify the host key should it
     *      be unknown
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    public TransportProtocolClient(HostKeyVerification hosts)
             throws TransportProtocolException {
        super();
        this.hosts = hosts;
    }


    /**
     *  Called by the framework when a registered message is received.
     *
     *@param  msg                          The message received.
     *@throws  TransportProtocolException  if a protocol error occurs
     *@throws  ServiceOperationException   if a service operation fails
     */
    public void onMessageReceived(SshMessage msg)
             throws IOException {
        switch (msg.getMessageId()) {
            case SshMsgServiceAccept.SSH_MSG_SERVICE_ACCEPT:
            {
                onMsgServiceAccept((SshMsgServiceAccept) msg);

                break;
            }
        }
    }


    /**
     *  Registers the clients transport protocol messages
     *
     *@throws  MessageAlreadyRegisteredException  if a message is already
     *      registered
     */
    public void registerTransportMessages()
             throws MessageAlreadyRegisteredException {
        transportMessages.put(new Integer(SshMsgServiceAccept.SSH_MSG_SERVICE_ACCEPT),
                SshMsgServiceAccept.class);
    }


    /**
     *  Requests that the remote computer start the specified service.
     *
     *@param  service                         The service name to start
     *@exception  TransportProtocolException  if a protocol error occurs
     *@exception  ServiceOperationException   if the service does not initialize
     *      properly.
     */
    public void requestService(Service service)
             throws IOException,
            ServiceOperationException {
        // Make sure the service is supported
        if (service.getState().getValue() != ServiceState.SERVICE_UNINITIALIZED) {
            throw new ServiceOperationException("The service instance must be uninitialized");
        }

        if ((state.getValue() != TransportProtocolState.CONNECTED)
                && (state.getValue() != TransportProtocolState.PERFORMING_KEYEXCHANGE)) {
            throw new TransportProtocolException("The transport protocol is not connected");
        }

        try {
            state.waitForState(TransportProtocolState.CONNECTED);
        } catch (InvalidStateException ise) {
        }

        service.init(Service.REQUESTING_SERVICE, this, exchangeHash, null);

        // Put the service on our list awaiting acceptance
        services.put(service.getServiceName(), service);

        // Create and send the message
        SshMsgServiceRequest msg =
                new SshMsgServiceRequest(service.getServiceName());
        sendMessage(msg, this);

        // Wait for the state of the service to be returned
        // If the service is denied the remote end will disconnect
        int ret = service.getState().waitForStateUpdate();

        if (ret == ServiceState.SERVICE_STOPPED) {
            throw new ServiceOperationException("The "
                    + service.getServiceName()
                    + " service failed to start!");
        }
    }


    protected void onDisconnect() {
      Iterator it = services.entrySet().iterator();
      Map.Entry entry;
      while(it.hasNext()) {
        entry = (Map.Entry)it.next();
        ((Service)entry.getValue()).stop();
        services.remove(entry.getKey());
      }
    }


    /**
     *  Returns the decryption algorithm to be used after key exchange. This
     *  method evaluates the supported algorithms of both sides and determines
     *  the correct algorithm.
     *
     *@return                                  The algorithm name.
     *@exception  AlgorithmNotAgreedException  if the algorithm cannot be
     *      agreed.
     */
    protected String getDecryptionAlgorithm()
             throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedSCEncryption(),
                serverKexInit.getSupportedSCEncryption());
    }


    /**
     *  Returns the encryption algorithm to be used after key exchange. This
     *  method evaluates the supported algorithms of both sides and determines
     *  the correct algorithm.
     *
     *@return                                  The algortihm name
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed
     */
    protected String getEncryptionAlgorithm()
             throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedCSEncryption(),
                serverKexInit.getSupportedCSEncryption());
    }


    /**
     *  Returns the input stream compression algorithm to be used after key
     *  exchange. This method evaluates the supported algorithms of both sides
     *  and determines the correct algorithm.
     *
     *@return                                  The algorithm name.
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed.
     */
    protected String getInputStreamCompAlgortihm()
             throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedSCComp(),
                serverKexInit.getSupportedSCComp());
    }


    /**
     *  Returns the input stream MAC algorithm to be used after key exchange.
     *  This method evaluates the supported algorithms of both sides and
     *  determines the correct algorithm.
     *
     *@return                                  The algorithm name.
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed.
     */
    protected String getInputStreamMacAlgorithm()
             throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedSCMac(),
                serverKexInit.getSupportedSCMac());
    }


    /**
     *  Sets the local sides identification string. This is sent initially to
     *  determine the protocol version. As the common layer cannot determine
     *  whether it is running as a client or server, this method allows the
     *  subclasses to correctly set the relevant protected member variable.
     */
    protected void setLocalIdent() {
        clientIdent =
                "SSH-" + PROTOCOL_VERSION + "-" + SOFTWARE_VERSION_COMMENTS
                + " [CLIENT]";
    }


    /**
     *  Returns the local protocol identification string. We return the relevant
     *  protected member variable for the subclass implementation.
     *
     *@return    The local identification string
     */
    protected String getLocalIdent() {
        return clientIdent;
    }


    /**
     *  Called by the abstract super class to set the relevant kex init member
     *  variable according to the subclasses operation (client or server)
     *
     *@param  msg  The kex init message sent by the local machine
     */
    protected void setLocalKexInit(SshMsgKexInit msg) {
        log.debug(msg.toString());
        clientKexInit = msg;
    }


    /**
     *  Gets the local sides kex init message.
     *
     *@return    The local sides kex init message
     */
    protected SshMsgKexInit getLocalKexInit() {
        return clientKexInit;
    }


    /**
     *  Returns the output stream compression algorithm to be used after key
     *  exchange. This method evaluates the supported algorithms of both sides
     *  and determines the correct algorithm.
     *
     *@return                                  The algorithm name.
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed.
     */
    protected String getOutputStreamCompAlgorithm()
             throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedCSComp(),
                serverKexInit.getSupportedCSComp());
    }


    /**
     *  Returns the output stream MAC algorithm to be used after key exchange.
     *  This method evaluates the supported algorithms of both sides and
     *  determines the correct algorithm.
     *
     *@return                                  The algorithm name.
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed.
     */
    protected String getOutputStreamMacAlgorithm()
             throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedCSMac(),
                serverKexInit.getSupportedCSMac());
    }


    /**
     *  Sets the remote sides identification string.
     *
     *@param  ident  The protocol version string received
     */
    protected void setRemoteIdent(String ident) {
        serverIdent = ident;
    }


    /**
     *  Returns the remote computers protocol identification string.
     *
     *@return    The remote identificaiton string
     */
    protected String getRemoteIdent() {
        return serverIdent;
    }


    /**
     *  Called by the abstract super class to set the remote sides kex init
     *  message.
     *
     *@param  msg  The kex init message received from the remote computer.
     */
    protected void setRemoteKexInit(SshMsgKexInit msg) {
        serverKexInit = msg;
    }


    /**
     *  Gets the remote computers kex init message
     *
     *@return    The remote computers kex init
     */
    protected SshMsgKexInit getRemoteKexInit() {
        return serverKexInit;
    }


    /**
     *  Called by the abstract super class when key exchange begins.
     *
     *@param  kex                             Description of the Parameter
     *@exception  TransportProtocolException  if a protocol error occurs
     *@throws  KeyExchangeException           if a key exchange error occurs
     */
    protected void performKeyExchange(SshKeyExchange kex)
             throws IOException {
        try {
            // Start the key exchange instance
            kex.performClientExchange(clientIdent, serverIdent,
                    clientKexInit.toByteArray(),
                    serverKexInit.toByteArray());

            // Verify the hoskey
            if (!verifyHostKey(kex.getHostKey(),
                          kex.getSignature(),
                          kex.getExchangeHash())) {
                log.fatal("Sending disconnect: Host key invalid");

                sendDisconnect(SshMsgDisconnect.HOST_KEY_NOT_VERIFIABLE,
                        "The host key supplied was not valid");
            }

            return;
        } catch (SshException ex) {
            log.fatal(ex);
        }

        throw new KeyExchangeException("Key exhange failed");
    }


    /**
     *  Handles the SSH_MSG_SERVICE_ACCEPT message
     *
     *@param  msg                          The message received
     *@throws  TransportProtocolException  if a protocol error occurs
     *@throws  ServiceOperationException   if a service operation fails
     */
    protected void onMsgServiceAccept(SshMsgServiceAccept msg)
             throws IOException,
            ServiceOperationException {
        Service service = (Service) services.get(msg.getServiceName());
        service.start();
    }


    /**
     *  Sets up the new keys for the IOStreams
     *
     *@param  encryptCSKey                       the client->server encryption
     *      key
     *@param  encryptCSIV                        the client->server encrytioon
     *      IV
     *@param  encryptSCKey                       the server->client encryption
     *      key
     *@param  encryptSCIV                        the server->client encryption
     *      IV
     *@param  macCSKey                           the client->server message
     *      authentication key
     *@param  macSCKey                           the server->client message
     *      authentication key
     *@throws  AlgorithmNotAgreedException       if an algorithm cannot be
     *      agreed
     *@throws  AlgorithmOperationException       if an algorithm fails
     *@throws  AlgorithmNotSupportedException    if an algorithm agreed is not
     *      supported
     *@throws  AlgorithmInitializationException  if an algorithm fails to
     *      initialize
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
        sshCipher.init(SshCipher.ENCRYPT_MODE, encryptCSIV, encryptCSKey);
        algorithmsOut.setCipher(sshCipher);

        // Setup the decryption cipher
        sshCipher = SshCipherFactory.newInstance(getDecryptionAlgorithm());
        sshCipher.init(SshCipher.DECRYPT_MODE, encryptSCIV, encryptSCKey);
        algorithmsIn.setCipher(sshCipher);

        // Create and put our macs into operation
        SshHmac hmac =
                SshHmacFactory.newInstance(getOutputStreamMacAlgorithm());
        hmac.init(macCSKey);
        algorithmsOut.setHmac(hmac);

        hmac = SshHmacFactory.newInstance(getInputStreamMacAlgorithm());
        hmac.init(macSCKey);
        algorithmsIn.setHmac(hmac);
    }


    /**
     *  Verifies the server host key using the public key algorithm negotiated
     *  during key exchange.
     *
     *@return                                      The result of the
     *      verification
     *@exception  InvalidSshKeyException           if the key is an invalid host
     *      key
     *@exception  InvalidSshKeySignatureException  if the signature is invalid
     *@exception  AlgorithmNotAgreedException      if an algoithm cannot be
     *      agreed
     *@exception  AlgorithmNotSupportedException   if an algorithm is not
     *      supported
     *@exception  InvalidHostFileException         if the hosts file is invalid
     */
    protected boolean verifyHostKey(byte[] key, byte[] sig, byte[] sigdata)
             throws TransportProtocolException {
        // Determine the public key algorithm and obtain an instance
        SshKeyPair pair =
                SshKeyPairFactory.newInstance(determineAlgorithm(clientKexInit.getSupportedPublicKeys(),
                serverKexInit.getSupportedPublicKeys()));

        // Iniialize the public key instance
        pk = pair.setPublicKey(key);

        // We have a valid key so verify it against the allowed hosts
        if (!hosts.verifyHost(properties.getHost(), pk.getFingerprint())) {
            return false;
        }

        // Verify the host key signauture
        return pk.verifySignature(sig, sigdata);
    }
}
