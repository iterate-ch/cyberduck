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

import java.math.BigInteger;

import java.net.Socket;

import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.transport.kex.KeyExchangeException;
import com.sshtools.j2ssh.transport.kex.SshKeyExchange;
import com.sshtools.j2ssh.transport.kex.SshKeyExchangeFactory;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.util.Hash;
import com.sshtools.j2ssh.util.InvalidStateException;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 *  <p>
 *
 *  The main transport protocol implementation. This abstract class provides the
 *  common functionality of both client and server implementations. </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: TransportProtocolCommon.java,v 1.24 2002/12/12 20:03:33
 *      martianx Exp $
 */
public abstract class TransportProtocolCommon
         implements TransportProtocol, Runnable {
    // Flag to keep on running
    //private boolean keepRunning = true;

    /**
     *  The log4j log object
     */
    protected static Logger log =
            Logger.getLogger(TransportProtocolCommon.class);

    /**
     *  End of line setting for CR+LF
     */
    public final static int EOL_CRLF = 1;

    /**
     *  End of Line setting for LF
     */
    public final static int EOL_LF = 2;

    /**
     *  The protocol version supported
     */
    public final String PROTOCOL_VERSION = "2.0";

    /**
     *  The software version comments that are sent during protocol negotiation
     */
    public final String SOFTWARE_VERSION_COMMENTS =
            "Sshtools.com Java SSH2 API (J2SSH) Version 0.0.5 alpha";

    /**
     *  The secret value k produced during key exchange
     */
    protected BigInteger k = null;

    /**
     *  Indicates when either the remote or local side has completed key
     *  exchange
     */
    protected Boolean completeOnNewKeys = new Boolean(false);

    /**
     *  The host verification instance for verifying host's and host keys
     */
    protected HostKeyVerification hosts;

    /**
     *  The key exchange engine
     */
    protected Map kexs = new HashMap();

    /**
     *  Map of transport message id's to implementation class
     */
    protected Map transportMessages = new HashMap();

    /**
     *  The connection properties for the current connection
     */
    protected SshConnectionProperties properties;

    /**
     *  The transport layer's message store
     */
    protected SshMessageStore messageStore = new SshMessageStore();

    /**
     *  The key exchange init message sent by the client
     */
    protected SshMsgKexInit clientKexInit = null;

    /**
     *  The key exchange init message sent by the server
     */
    protected SshMsgKexInit serverKexInit = null;

    /**
     *  The identification string sent by the client
     */
    protected String clientIdent = null;

    /**
     *  The identification string sent by the server
     */
    protected String serverIdent = null;

    /**
     *  Sync object containing the cipher, mac and compression objects for the
     *  transport protocol input stream
     */
    protected TransportProtocolAlgorithmSync algorithmsIn;

    /**
     *  Snyc object containing the cipher, mac and compression objects for the
     *  transport protocol output stream
     */
    protected TransportProtocolAlgorithmSync algorithmsOut;

    /**
     *  The transport protocols state instance
     */
    protected TransportProtocolState state = new TransportProtocolState();

    /**
     *  The exchange hash output from key exchange
     */
    protected byte exchangeHash[] = null;

    /**
     *  The servers host key data
     */
    protected byte hostKey[] = null;

    /**
     *  The servers signature supplied to verify the host key
     */
    protected byte signature[] = null;

    // Storage of messages whilst in key exchange
    private List messageStack = new ArrayList();

    // Message notification registry
    private Map messageNotifications = new HashMap();

    // Key exchange lock for accessing the kex init messages
    private Object kexLock = new Object();

    // Object to synchronize key changing
    private Object keyLock = new Object();

    // The connected socket
    private Socket socket;

    // The thread object
    private Thread thread;

    // The input stream for recieving data
    /**
     *  Description of the Field
     */
    protected TransportProtocolInputStream sshIn;

    // The output stream for sending data
    /**
     *  Description of the Field
     */
    protected TransportProtocolOutputStream sshOut;
    private int remoteEOL = EOL_CRLF;


    /**
     *  Constructor for the SshTransportProtocol object
     */
    public TransportProtocolCommon() { }


    /**
     *  Gets the guessed EOL setting for the remote host
     *
     *@return    either EOL_CRLF or EOL_LF
     */
    public int getRemoteEOL() {
        return remoteEOL;
    }


    /**
     *  Gets the state attribute of the TransportProtocolCommon object
     *
     *@return    The transport protocols state
     */
    public TransportProtocolState getState() {
        return state;
    }
    protected abstract void onDisconnect();

    /**
     *  Disconnects the connection by sending a disconnect message with the
     *  BY_APPLICAITON reason.
     *
     *@param  description  The description of the reason
     */
    public void disconnect(String description) {
        log.debug("Disconnect: " + description);

        onDisconnect();
        try {
            // Send the disconnect message automatically
            sendDisconnect(SshMsgDisconnect.BY_APPLICATION, description);
        } catch (Exception e) {
            log.warn("Failed to send disconnect", e);
        }
    }


    /**
     *  Implements the TransportProtocol interface method to allow external SSH
     *  implementations to receive message notificaitons.
     *
     *@param  messageId                              The messageId of the
     *      registered message
     *@param  implementor                            The class that implements
     *      the message
     *@param  store                                  The message store to
     *      receive notificaiton
     *@exception  MessageAlreadyRegisteredException  if the message cannot be
     *      registered.
     */
    public void registerMessage(Integer messageId, Class implementor,
            SshMessageStore store)
             throws MessageAlreadyRegisteredException {
        log.debug("Registering message Id " + messageId.toString());

        if (!messageNotifications.containsKey(messageId)) {
            messageNotifications.put(messageId,
                    new RegisteredMessage(implementor, store));
        } else {
            throw new MessageAlreadyRegisteredException(messageId);
        }
    }


    /**
     *  Called to request the registration of transport protocol messages
     *
     *@throws  MessageAlreadyRegisteredException  if the message is already
     *      registered
     */
    public abstract void registerTransportMessages()
             throws MessageAlreadyRegisteredException;


    /**
     *  Main processing method for the TransportProtocolCommon object
     */
    public void run() {
        try {
            state.setValue(TransportProtocolState.NEGOTIATING_PROTOCOL);

            log.info("Registering transport protocol messages with inputstream");

            algorithmsOut = new TransportProtocolAlgorithmSync();
            algorithmsIn = new TransportProtocolAlgorithmSync();

            // Create the input/output streams
            sshIn =
                    new TransportProtocolInputStream(socket, this, algorithmsIn);
            sshOut =
                    new TransportProtocolOutputStream(socket, this, algorithmsOut);

            // Register the transport layer messages that this class will handle
            transportMessages.put(new Integer(SshMsgDisconnect.SSH_MSG_DISCONNECT),
                    SshMsgDisconnect.class);

            transportMessages.put(new Integer(SshMsgIgnore.SSH_MSG_IGNORE),
                    SshMsgIgnore.class);

            transportMessages.put(new Integer(SshMsgUnimplemented.SSH_MSG_UNIMPLEMENTED),
                    SshMsgUnimplemented.class);

            transportMessages.put(new Integer(SshMsgDebug.SSH_MSG_DEBUG),
                    SshMsgDebug.class);

            transportMessages.put(new Integer(SshMsgKexInit.SSH_MSG_KEX_INIT),
                    SshMsgKexInit.class);

            transportMessages.put(new Integer(SshMsgNewKeys.SSH_MSG_NEWKEYS),
                    SshMsgNewKeys.class);

            registerTransportMessages();

            List list = SshKeyExchangeFactory.getSupportedKeyExchanges();
            Iterator it = list.iterator();

            while (it.hasNext()) {
                String keyExchange = (String) it.next();
                SshKeyExchange kex =
                        SshKeyExchangeFactory.newInstance(keyExchange);
                kex.init(this);
                kexs.put(keyExchange, kex);
            }

            // call abstract to initialise the local ident string
            setLocalIdent();

            // negotiate the protocol version
            negotiateVersion();

            startBinaryPacketProtocol();

            log.debug("The Transport Protocol has been stopped");

        } catch (Exception e) {
            log.fatal("The Transport Protocol thread failed", e);
        } finally {
          thread = null;
        }
    }


    /**
     *  Send an SSH message, if the state doen't allow it because of key
     *  exchange then the message is stored and sent as soon as the state
     *  changes
     *
     *@param  msg                             The SshMessage to send
     *@param  sender                          the object whom is sending the
     *      message
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    public void sendMessage(SshMessage msg, Object sender)
             throws IOException {
        // Send a message, if were in key exchange then add it to
        // the list unless of course it is a transport protocol or key
        // exchange message
        log.debug("Sending " + msg.getMessageName());

        int currentState = state.getValue();

        if (sender instanceof SshKeyExchange
                || sender instanceof TransportProtocolCommon
                || (currentState == TransportProtocolState.CONNECTED)) {
            sshOut.sendMessage(msg);

            return;
        }

        if (currentState == TransportProtocolState.PERFORMING_KEYEXCHANGE) {
            log.debug("Adding to message queue whilst in key exchange");

            synchronized (messageStack) {
                // Add this message to the end of the list
                messageStack.add(msg);
            }
        } else {
            throw new TransportProtocolException("The transport protocol is disconnected");
        }
    }


    /**
     *  Starts the transport protocol
     *
     *@param  socket                       the underlying socket for
     *      communication
     *@param  properties                   the properties of the connection
     *@throws  TransportProtocolException  if a protocol error occurs
     */
    public void startTransportProtocol(Socket socket,
            SshConnectionProperties properties)
             throws IOException {
        // Save the connected socket for later use
        this.socket = socket;
        this.properties = properties;

        // Start the transport layer message loop
        log.info("Starting transport protocol");
        thread = new Thread(this, "TransportProtocolCommon");
        if(ConfigurationLoader.isContextClassLoader())
              thread.setContextClassLoader(ConfigurationLoader.getContextClassLoader());
        thread.setDaemon(true);
        thread.start();

        try {
            state.waitForState(TransportProtocolState.CONNECTED);
        } catch (InvalidStateException ise) {
        }
    }


    /**
     *  Implements the TransportProtocol method to allow external SSH
     *  implementations to unregister a message.
     *
     *@param  messageId                          The message id of the message
     *@param  store                              The message store receiving the
     *      notifications.
     *@exception  MessageNotRegisteredException  if the message is not
     *      registered.
     */
    public void unregisterMessage(Integer messageId, SshMessageStore store)
             throws MessageNotRegisteredException {
        log.debug("Unregistering message Id " + messageId.toString());

        if (!messageNotifications.containsKey(messageId)) {
            throw new MessageNotRegisteredException(messageId);
        }

        SshMessageStore actual =
                (SshMessageStore) messageNotifications.get(messageId);

        if (!store.equals(actual)) {
            throw new MessageNotRegisteredException(messageId, store);
        }

        messageNotifications.remove(messageId);
    }


    /**
     *  Abstract method to determine the correct decryption algorithm to use
     *  This is found by iterating through the clients supported algorithm and
     *  selecting the first supported decryption method that the server also
     *  supports. Client and server implementations should define this method
     *  using the determineAlgorithm method to pass either the CS or SC methods
     *  of the SshMsgKexInit object.
     *
     *@return                                  The decryption algorithm to use
     *      i.e. "3des-cbc"
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed
     */
    protected abstract String getDecryptionAlgorithm()
             throws AlgorithmNotAgreedException;


    /**
     *  Abstract method to determine the correct encryption algorithm to use
     *  This is found by iterating through the clients supported algorithm and
     *  selecting the first supported encryption method that the server also
     *  supports. Client and server implementations should define this method
     *  using the determineAlgorithm method to pass either the CS or SC methods
     *  of the SshMsgKexInit object object
     *
     *@return                                  The encryption algorithm to use
     *      i.e. "3des-cbc"
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed
     */
    protected abstract String getEncryptionAlgorithm()
             throws AlgorithmNotAgreedException;


    /**
     *  Abtract method for the client/server implmentations to determine the
     *  compression algorithm for the input stream. Client and server
     *  implementations should define this method using the determineAlgorithm
     *  method to pass either the CS or SC methods of the SshMsgKexInit object.
     *
     *@return                                  The compression algorithm to use
     *      i.e. "zlib"
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed
     */
    protected abstract String getInputStreamCompAlgortihm()
             throws AlgorithmNotAgreedException;


    /**
     *  Abtract method for the client/server implmentations to determine the
     *  message authentication algorithm for the input stream. Client and server
     *  implementations should define this method using the determineAlgorithm
     *  method to pass either the CS or SC methods of the SshMsgKexInit object.
     *
     *@return                                  The mac algorithm to use i.e.
     *      "hmac-sha1"
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed
     */
    protected abstract String getInputStreamMacAlgorithm()
             throws AlgorithmNotAgreedException;


    /**
     *  Abstract method that requires a derived class to set value of the local
     *  identification string. If the class implementing this method is a client
     *  then it should set the clientIdent protected member variable, if the
     *  class is implementing a server it should set the protected member
     *  serverIdent.
     */
    protected abstract void setLocalIdent();


    /**
     *  Abstract method to return the local identification string which is used
     *  in protocol negotiation and in computing the exchange hash.
     *  Implementations should return either the protected member variable
     *  clientIdent or serverIdent
     *
     *@return    The local computers idnetification string, used in protocol
     *      negotiation
     */
    protected abstract String getLocalIdent();


    /**
     *  Abstract method to set the local kex init msg which is used in computing
     *  the exchange hash. Implementations should set the appropriate client or
     *  server member variable
     *
     *@param  msg  The local computers kex init message
     */
    protected abstract void setLocalKexInit(SshMsgKexInit msg);


    /**
     *  Abstract method to get the local kex init msg which is used in computing
     *  the exchange hash. Implementations should return the appropriate client
     *  or server member variable
     *
     *@return    The local computers kex init message
     */
    protected abstract SshMsgKexInit getLocalKexInit();


    /**
     *  Abtract method for the client/server implmentations to determine the
     *  compression algorithm for the output stream. Client and server
     *  implementations should define this method using the determineAlgorithm
     *  method to pass either the CS or SC methods of the SshMsgKexInit object.
     *
     *@return                                  The compression algorithm to use
     *      i.e. "zlib"
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed
     */
    protected abstract String getOutputStreamCompAlgorithm()
             throws AlgorithmNotAgreedException;


    /**
     *  Abtract method for the client/server implmentations to determine the
     *  message authentication algorithm for the output stream. Client and
     *  server implementations should define this method using the
     *  determineAlgorithm method to pass either the CS or SC methods of the
     *  SshMsgKexInit object.
     *
     *@return                                  The mac algorithm to use i.e.
     *      "hmac-sha1"
     *@exception  AlgorithmNotAgreedException  if an algorithm cannot be agreed
     */
    protected abstract String getOutputStreamMacAlgorithm()
             throws AlgorithmNotAgreedException;


    /**
     *  Abstract method that requires a derived class to set value of the remote
     *  identification string. If the class implementing this method is a client
     *  then it should set the serverIdent protected member variable, if the
     *  class is implementing a server it should set the protected member
     *  clientIdent.
     *
     *@param  ident  The identifiaction string received from the remote host
     */
    protected abstract void setRemoteIdent(String ident);


    /**
     *  Abstract method to return the remote identification string which is used
     *  in protocol negotiation and in computing the exchange hash.
     *  Implementations should return either the protected member variable
     *  clientIdent or serverIdent
     *
     *@return    The local computers idnetification string, used in protocol
     *      negotiation
     */
    protected abstract String getRemoteIdent();


    /**
     *  Abstract method to set the remote kex init msg which is used in
     *  computing the exchange hash. Implementations should set the appropriate
     *  client or server member variable
     *
     *@param  msg  The remote computers kex init message
     */
    protected abstract void setRemoteKexInit(SshMsgKexInit msg);


    /**
     *  Abstract method to get the remote kex init msg which is used in
     *  computing the exchange hash. Implementations should return the
     *  appropriate client or server member variable
     *
     *@return    The local computers kex init message
     */
    protected abstract SshMsgKexInit getRemoteKexInit();


    /**
     *  Abstract method called when key exchange has begun
     *
     *@param  kex                          the key exchange in progress
     *@throws  TransportProtocolException  if a protocol error occurs
     *@throws  KeyExchangeException        if key exchange fails
     */
    protected abstract void performKeyExchange(SshKeyExchange kex)
             throws IOException,
            KeyExchangeException;


    /**
     *  Determines the correct key exchange algorithm to use
     *
     *@return                               A string containing the algorithm
     *      name i.e. "diffie-hellman-group1.sha1"
     *@throws  AlgorithmNotAgreedException  if no algorithm is agreed between
     *      the two parties
     */
    protected String getKexAlgorithm()
             throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedKex(),
                serverKexInit.getSupportedKex());
    }


    /**
     *  Sets the transport layer up for performing the key exchange, this is
     *  called when either a SSH_MSG_KEXINIT message is received or sent by
     *  either party
     *
     *@throws  TransportProtocolException  if a protocol error occurs
     *@throws  KeyExchangeException        if key exchange fails
     */
    protected void beginKeyExchange()
             throws IOException,
            KeyExchangeException {
        log.info("Starting key exchange");

        state.setValue(TransportProtocolState.PERFORMING_KEYEXCHANGE);

        String kexAlgorithm = "";

        // We now have both kex inits, this is where client/server
        // implemtations take over so call abstract methods
        try {
            // Determine the key exchange algorithm
            kexAlgorithm = getKexAlgorithm();

            log.debug("Key exchange algorithm: " + kexAlgorithm);

            // Get an instance of the key exchange algortihm
            SshKeyExchange kex = (SshKeyExchange) kexs.get(kexAlgorithm);

            // Do the key exchange
            performKeyExchange(kex);

            // Record the output
            exchangeHash = kex.getExchangeHash();
            hostKey = kex.getHostKey();
            signature = kex.getSignature();
            k = kex.getSecret();

            // Send new keys
            sendNewKeys();

            kex.reset();
        } catch (AlgorithmNotAgreedException e) {
            sendDisconnect(SshMsgDisconnect.KEY_EXCHANGE_FAILED,
                    "No suitable key exchange algorithm was agreed");

            throw new KeyExchangeException("No suitable key exchange algorithm could be agreed.");
        }
    }


    /**
     *  Creates the local key exchange init message
     *
     *@return                              the local kex init message
     *@throws  TransportProtocolException  if a protocol error occurs
     */
    protected SshMsgKexInit createLocalKexInit()
             throws IOException {
        return new SshMsgKexInit(properties);
    }


    /**
     *  This is called when a corrupt Mac has been received on the input stream.
     *  In this instance we will send a disconnect message.
     */
    protected void onCorruptMac() {
        log.fatal("Corrupt Mac on Input");

        // Send a disconnect message
        sendDisconnect(SshMsgDisconnect.MAC_ERROR, "Corrupt Mac on input");
    }


    /**
     *  Called by the framework when a new message is received
     *
     *@param  messageId                       the id of the message
     *@param  bar                             the byte array reader containing
     *      the message
     *@exception  InvalidMessageException     if the message is invalid
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    protected void onMessageData(Integer messageId, ByteArrayReader bar)
             throws IOException {
        try {
            // Is the message a transport layer message?
            if (transportMessages.containsKey(messageId)) {
                Class implementor = (Class) transportMessages.get(messageId);
                SshMessage msg = (SshMessage) implementor.newInstance();
                msg.fromByteArray(bar);

                if (msg.getMessageId() == SshMsgNewKeys.SSH_MSG_NEWKEYS) {
                    onMsgNewKeys((SshMsgNewKeys) msg);
                } else {
                    messageStore.addMessage(msg);
                }
            } else {
                RegisteredMessage registered =
                        (RegisteredMessage) messageNotifications.get(messageId);

                if (registered == null) {
                    log.debug("Unimplemented message received "
                            + String.valueOf(messageId));

                    SshMsgUnimplemented msg =
                            new SshMsgUnimplemented(sshIn.getSequenceNo());
                    sendMessage(msg, this);

                    return;
                }

                // Create an instance of the implementation class
                SshMessage msg =
                        (SshMessage) registered.getImplementor().newInstance();

                // Call its fromByteArray method
                msg.fromByteArray(bar);

                // Get the message store receiving notification
                SshMessageStore store =
                        (SshMessageStore) registered.getMessageStore();

                if (store == null) {
                    throw new TransportProtocolException("Message was registered but message store is null!");
                }

                log.debug("Received " + msg.getMessageName());

                // Add the message to the store
                store.addMessage(msg);
            }
        } catch (IllegalAccessException iae) {
            throw new InvalidMessageException("Illegal access! Could not create message class: "
                    + iae.getMessage());
        } catch (InstantiationException ie) {
            throw new InvalidMessageException("Could not instansate class: "
                    + ie.getMessage());
        }
    }


    /**
     *  Called by the framework when a new message is received.
     *
     *@param  msg                             The message recevied
     *@exception  TransportProtocolException  Description of the Exception
     *@exception  ServiceOperationException   Description of the Exception
     */
    protected abstract void onMessageReceived(SshMessage msg)
             throws IOException,
            ServiceOperationException;


    /**
     *  Sends a disconnect message
     *
     *@param  reason       The reason code.
     *@param  description  The readable reason description.
     */
    protected void sendDisconnect(int reason, String description) {
        SshMsgDisconnect msg = new SshMsgDisconnect(reason, description, "");

        try {
            sendMessage(msg, this);
            stop();
        } catch (Exception e) {
            log.warn("Failed to send disconnect", e);
        }
    }


    /**
     *  Sends the key exchange init message
     *
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    protected void sendKeyExchangeInit()
             throws IOException {
        setLocalKexInit(createLocalKexInit());
        sendMessage(getLocalKexInit(), this);
    }


    /**
     *  Sends the SSH_MSG_NEWKEYS message to indicate that new keys are now in
     *  operation
     *
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    protected void sendNewKeys()
             throws IOException {
        // Send new keys
        SshMsgNewKeys msg = new SshMsgNewKeys();
        sendMessage(msg, this);

        // Lock the outgoing algorithms so nothing else is sent untill
        // weve updated them with the new keys
        algorithmsOut.lock();

        // If we have received the remote sides new keys message then
        // we will put the new keys into operation
        synchronized (completeOnNewKeys) {
            if (completeOnNewKeys.booleanValue()) {
                completeKeyExchange();
            } else {
                completeOnNewKeys = new Boolean(true);
            }
        }
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
    protected abstract void setupNewKeys(byte encryptCSKey[],
            byte encryptCSIV[],
            byte encryptSCKey[],
            byte encryptSCIV[], byte macCSKey[],
            byte macSCKey[])
             throws AlgorithmNotAgreedException,
            AlgorithmOperationException,
            AlgorithmNotSupportedException,
            AlgorithmInitializationException;


    /**
     *  Completes key exchange by creating keys from the exchange hash and puts
     *  them into use.
     *
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    protected void completeKeyExchange()
             throws IOException {
        log.info("Completing key exchange");

        try {
            // Reset the state variables
            completeOnNewKeys = new Boolean(false);

            log.debug("Making keys from key exchange output");

            // Make the keys
            byte encryptionKey[] = makeSshKey('C');
            byte encryptionIV[] = makeSshKey('A');
            byte decryptionKey[] = makeSshKey('D');
            byte decryptionIV[] = makeSshKey('B');
            byte sendMac[] = makeSshKey('E');
            byte receiveMac[] = makeSshKey('F');

            log.debug("Creating algorithm objects");

            setupNewKeys(encryptionKey, encryptionIV, decryptionKey,
                    decryptionIV, sendMac, receiveMac);

            // Reset the key exchange
            clientKexInit = null;
            serverKexInit = null;

            algorithmsIn.release();
            algorithmsOut.release();

            /*
             *  Update our state, we can send all packets
             *
             */
            state.setValue(TransportProtocolState.CONNECTED);

            // Send any outstanding messages
            synchronized (messageStack) {
                Iterator it = messageStack.iterator();

                log.debug("Sending queued messages");

                while (it.hasNext()) {
                    SshMessage msg = (SshMessage) it.next();

                    sendMessage(msg, this);
                }

                messageStack.clear();
            }
        } catch (AlgorithmNotAgreedException anae) {
            sendDisconnect(SshMsgDisconnect.KEY_EXCHANGE_FAILED,
                    "Algorithm not agreed");
            throw new TransportProtocolException("The connection was disconnected because an algorithm could not be agreed");
        } catch (AlgorithmNotSupportedException anse) {
            sendDisconnect(SshMsgDisconnect.KEY_EXCHANGE_FAILED,
                    "Application error");
            throw new TransportProtocolException("The connection was disconnected because an algorithm class could not be loaded");
        } catch (AlgorithmOperationException aoe) {
            sendDisconnect(SshMsgDisconnect.KEY_EXCHANGE_FAILED,
                    "Algorithm operation error");
            throw new TransportProtocolException("The connection was disconnected because"
                    + " of an algorithm operation error");
        } catch (AlgorithmInitializationException aie) {
            sendDisconnect(SshMsgDisconnect.KEY_EXCHANGE_FAILED,
                    "Algorithm initialization error");
            throw new TransportProtocolException("The connection was disconnected because"
                    + " of an algorithm initialization error");
        }
    }


    /**
     *  Helper method to determine the first algorithm that appears in the
     *  client list that is also supported by the server
     *
     *@param  clientAlgorithms                 The list of client algorithms
     *@param  serverAlgorithms                 The list of server algorithms
     *@return                                  The determined algorithms
     *@exception  AlgorithmNotAgreedException  if the algorithm cannot be agreed
     */
    protected String determineAlgorithm(List clientAlgorithms,
            List serverAlgorithms)
             throws AlgorithmNotAgreedException {
        log.debug("Determine Algorithm");
        log.debug("Client Algorithms: " + clientAlgorithms.toString());
        log.debug("Server Algorithms: " + serverAlgorithms.toString());

        String algorithmClient;
        String algorithmServer;

        Iterator itClient = clientAlgorithms.iterator();

        while (itClient.hasNext()) {
            algorithmClient = (String) itClient.next();

            Iterator itServer = serverAlgorithms.iterator();

            while (itServer.hasNext()) {
                algorithmServer = (String) itServer.next();

                if (algorithmClient.equals(algorithmServer)) {
                    log.debug("Returning " + algorithmClient);

                    return algorithmClient;
                }
            }
        }

        throw new AlgorithmNotAgreedException("Could not agree algorithm");
    }


    /**
     *  Starts the transport protocols binary messaging
     *
     *@throws  TransportProtocolException  if a protocol error occurs
     *@throws  ServiceOperationException   if an operation fails
     */
    protected void startBinaryPacketProtocol()
             throws IOException {
        // Send our Kex Init
        sendKeyExchangeInit();

        sshIn.open();
        processMessages();
    }


    /**
     *  Stops the transport layer
     */
    protected final void stop() {
        state.setValue(TransportProtocolState.DISCONNECTED);

        // Close the input/output streams
        sshIn.close();
        messageStore.close();
        try {
            socket.close();
        } catch (IOException ioe) {
        }
    }


    /**
     *  Creates an Ssh key from the exchange hash and a literal character
     *
     *@param  chr                             The character used to create the
     *      key
     *@return                                 40 bytes of key data
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    private byte[] makeSshKey(char chr)
             throws IOException {
        try {
            // Create the first 20 bytes of key data
            ByteArrayWriter keydata = new ByteArrayWriter();
            byte data[] = new byte[20];

            Hash hash = new Hash("SHA");

            // Put the dh k value
            hash.putBigInteger(k);

            // Put in the exchange hash
            hash.putBytes(exchangeHash);

            // Put in the character
            hash.putByte((byte) chr);

            // Put the exchange hash in again
            hash.putBytes(exchangeHash);

            // Create the fist 20 bytes
            data = hash.doFinal();
            keydata.write(data);

            // Now do the next 20
            hash.reset();

            // Put the dh k value in again
            hash.putBigInteger(k);

            // And the exchange hash
            hash.putBytes(exchangeHash);

            // Finally the first 20 bytes of data we created
            hash.putBytes(data);

            data = hash.doFinal();

            // Put it all together
            keydata.write(data);

            // Return it
            return keydata.toByteArray();
        } catch (NoSuchAlgorithmException nsae) {
            sendDisconnect(SshMsgDisconnect.KEY_EXCHANGE_FAILED,
                    "Application error");
            throw new TransportProtocolException("SHA algorithm not supported");
        } catch (IOException ioe) {
            sendDisconnect(SshMsgDisconnect.KEY_EXCHANGE_FAILED,
                    "Application error");
            throw new TransportProtocolException("Error writing key data");
        }
    }


    /**
     *  When the protocol starts, both sides must send an identification string
     *  that identifies the protocol version supported as well as and additional
     *  software version comments field. The identification strings are saved
     *  for later use in computing the exchange hash
     *
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    private void negotiateVersion()
             throws IOException {
        byte buf[];
        int len;
        String remoteVer = "";

            log.info("Negotiating protocol version");
            log.debug("Local identification: " + getLocalIdent());

            // Get the local ident string by calling the abstract method, this
            // way the implementations set the correct variables for computing the
            // exchange hash
            String data = getLocalIdent() + "\r\n";

            // Send our version string
            socket.getOutputStream().write(data.getBytes());

            // Now wait for a reply and evaluate the ident string
            //buf = new byte[255];
            StringBuffer buffer = new StringBuffer();

            char ch;

            // Look for a string starting with "SSH-"
            while (!remoteVer.startsWith("SSH-")) {
                // Get the next string
                while ((ch = (char) socket.getInputStream().read()) != '\n') {
                    buffer.append(ch);
                }

                // Set trimming off any EOL characters
                remoteVer = buffer.toString();

                // Guess the remote sides EOL by looking at the end of the ident string
                if (remoteVer.endsWith("\r")) {
                    remoteEOL = EOL_CRLF;
                } else {
                    remoteEOL = EOL_LF;
                }

                log.debug("EOL is guessed at "
                        + ((remoteEOL == EOL_CRLF) ? "CR+LF" : "LF"));

                // Remove any \r
                remoteVer = remoteVer.trim();
            }

            // Get the index of the seperators
            int l = remoteVer.indexOf("-");
            int r = remoteVer.indexOf("-", l + 1);

            // Call abstract method so the implementations can set the
            // correct member variable
            setRemoteIdent(remoteVer.trim());

            log.debug("Remote identification: " + getRemoteIdent());

            // Get the version
            String remoteVersion = remoteVer.substring(l + 1, r);

            // Evaluate the version, we only support 2.0
            if (!(remoteVersion.equals("2.0") || (remoteVersion.equals("1.99")))) {
                log.fatal("The remote computer does not support protocol version 2.0");
                throw new TransportProtocolException("The protocol version of the remote computer is not supported!");
            }

            log.info("Protocol negotiation complete");
    }


    /**
     *  Handles a debug message
     *
     *@param  msg  the debug message
     */
    private void onMsgDebug(SshMsgDebug msg) {
        log.debug(msg.getMessage());
    }


    /**
     *  Handles a disconnect message
     *
     *@param  msg                             the disconnect message
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    private void onMsgDisconnect(SshMsgDisconnect msg)
             throws IOException {
        log.info("The remote computer disconnected");
        log.debug(msg.getDescription());
        onDisconnect();
        stop();
    }


    /**
     *  Handles the ignore message
     *
     *@param  msg  the ignore message
     */
    private void onMsgIgnore(SshMsgIgnore msg) {
        log.debug("SSH_MSG_IGNORE with "
                + String.valueOf(msg.getData().length()) + " bytes of data");
    }


    /**
     *  Handles the kex init message
     *
     *@param  msg                             the kex init message
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    private void onMsgKexInit(SshMsgKexInit msg)
             throws IOException {
        log.debug("Received remote key exchange init message");
        log.debug(msg.toString());

        synchronized (kexLock) {
            setRemoteKexInit(msg);

            // As either party can initiate a key exchange then we
            // must check to see if we have sent our own
            if (getLocalKexInit() == null) {
                sendKeyExchangeInit();
            }

            try {
                beginKeyExchange();
            } catch (KeyExchangeException ke) {
                log.fatal("Key exchange failed", ke);
                throw new TransportProtocolException("Key exchange failed!");
            }
        }
    }


    /**
     *  Handles the new keys message
     *
     *@param  msg                             The message received
     *@exception  TransportProtocolException  if a protocol error occurs
     */
    private void onMsgNewKeys(SshMsgNewKeys msg)
             throws IOException {
        // Determine whether we have completed our own
        log.debug("Received New Keys");
        algorithmsIn.lock();

        synchronized (completeOnNewKeys) {
            if (completeOnNewKeys.booleanValue()) {
                completeKeyExchange();
            } else {
                completeOnNewKeys = new Boolean(true);
            }
        }
    }


    /**
     *  Handles an unimplemented message
     *
     *@param  msg  the unimplemented message
     */
    private void onMsgUnimplemented(SshMsgUnimplemented msg) {
        log.debug("The message with sequence no " + msg.getSequenceNo()
                + " was reported as unimplemented by the remote end.");
    }


    /**
     *  Implements the message loop
     *
     *@throws  TransportProtocolException  if a protocol error occurs
     *@throws  ServiceOperationException   if a service operation fails
     */
    private void processMessages()
             throws IOException {
        while (state.getValue() != TransportProtocolState.DISCONNECTED) {
            SshMessage msg = messageStore.nextMessage();

            if(msg==null)
              return;

            if (state.getValue() == TransportProtocolState.DISCONNECTED) {
                break;
            }

            log.debug("Transport Protocol is processing "
                    + msg.getMessageName());

            switch (msg.getMessageId()) {
                case SshMsgKexInit.SSH_MSG_KEX_INIT:
                {
                    onMsgKexInit((SshMsgKexInit) msg);

                    break;
                }

                case SshMsgDisconnect.SSH_MSG_DISCONNECT:
                {
                    onMsgDisconnect((SshMsgDisconnect) msg);

                    break;
                }

                case SshMsgIgnore.SSH_MSG_IGNORE:
                {
                    onMsgIgnore((SshMsgIgnore) msg);

                    break;
                }

                case SshMsgUnimplemented.SSH_MSG_UNIMPLEMENTED:
                {
                    onMsgUnimplemented((SshMsgUnimplemented) msg);

                    break;
                }

                case SshMsgDebug.SSH_MSG_DEBUG:
                {
                    onMsgDebug((SshMsgDebug) msg);

                    break;
                }

                default:
                    onMessageReceived(msg);
            }
        }


    }


    /**
     *  Stores the details of a registered message
     *
     *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
     *@created    20 December 2002
     *@version    $Id: TransportProtocolCommon.java,v 1.24 2002/12/12 20:03:33
     *      martianx Exp $
     */
    class RegisteredMessage {
        private Class implementor;
        private SshMessageStore messageStore;


        /**
         *  Constructor for the RegisteredMessage object
         *
         *@param  implementor   the message implementation class
         *@param  messageStore  the message store for notification
         */
        public RegisteredMessage(Class implementor, SshMessageStore messageStore) {
            this.implementor = implementor;
            this.messageStore = messageStore;
        }


        /**
         *  Gets the implementor attribute of the RegisteredMessage object
         *
         *@return    The implementor value
         */
        public Class getImplementor() {
            return implementor;
        }


        /**
         *  Gets the messageStore attribute of the RegisteredMessage object
         *
         *@return    The messageStore value
         */
        public SshMessageStore getMessageStore() {
            return messageStore;
        }
    }
}
