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
package com.sshtools.j2ssh.authentication;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.ServerConfiguration;
import com.sshtools.j2ssh.transport.MessageAlreadyRegisteredException;
import com.sshtools.j2ssh.transport.Service;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.util.InvalidStateException;

/**
 *  <p>
 *
 *  This class implements the SSH Authentication protocol as a Transport
 *  Protocol service. </p> <p>
 *
 *  The authentication process is as follows:<br>
 *  <br>
 *  The authentication service is started<br>
 *  The server MAY reply with a banner message (which the client can retreive
 *  using <code>getBannerMessage</code><br>
 *  The user MAY request the list of available authorizations by calling <code>getAvailableAuths</code>
 *  <br>
 *  The user MUST request authentication by calling <code>authenticate</code>
 *  for each method they wish to try with an uninitailized instance of a
 *  Transport Protocol service.<br>
 *  The server responds with a success message and the desired service is
 *  started<br>
 *  </p>
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: AuthenticationProtocol.java,v 1.19 2002/12/12 20:00:27
 *      martianx Exp $
 */
public class AuthenticationProtocol
         extends Service {
    private static Logger log = Logger.getLogger(AuthenticationProtocol.class);
    private AuthenticationProtocolState protocolState =
            new AuthenticationProtocolState();
    private List availableAuths = null;
    private List completedAuthentications = new ArrayList();
    private Map acceptServices = new HashMap();
    private Map authMethods = null;
    private SshAuthentication currentAuth = null;
    private String banner;
    private String serviceToStart;
    private byte exchangeHash[];
    private boolean isNoneRequest = false;


    /**
     *  Constructor for the AuthenticationProtocol
     */
    public AuthenticationProtocol() {
        super("ssh-userauth");
    }


    /**
     *  Gets the available authentication methods returned by the server.<br>
     *  <br>
     *  NOTE: The authentication protocol states that the server may return
     *  authentication methods that are not valid for the user.
     *
     *@param  username                     The username to request
     *      authentication methods for
     *@param  serviceName                  The service name to start
     *@return                              The List of Strings detailing the
     *      authentication methods
     *@throws  TransportProtocolException  if an error occurs in the Transport
     *      Protocol
     */
    public List getAvailableAuths(String username, String serviceName)
             throws IOException {
        log.info("Requesting authentication methods");

        if (availableAuths == null) {
            SshMsgUserAuthRequest msg =
                    new SshMsgUserAuthRequest(username, serviceName, "none", null);

            isNoneRequest = true;

            transport.sendMessage(msg, this);

            // Wait for the state to change before returning i.e. we have a list
            // of available authentication methods
            try {
                protocolState.waitForState(AuthenticationProtocolState.READY);
            } catch (InvalidStateException ise) {
            }
        }

        isNoneRequest = false;

        return availableAuths;
    }


    /**
     *  Gets the authentication banner message received from the server. This
     *  may be null.
     *
     *@return    A String containing the banner message
     */
    public String getBannerMessage() {
        return banner;
    }


    /**
     *  Gets the current state of the authentication protocol.
     *
     *@return    The Authentication Protocols State instance
     */
    public AuthenticationProtocolState getProtocolState() {
        return protocolState;
    }


    /**
     *  Configures the Authentication Protocol to allow authentication attempts
     *  to start a Transport Protocol service.
     *
     *@param  service  The service instance to start
     */
    public void acceptService(Service service) {
        acceptServices.put(service.getServiceName(), service);
    }


    /**
     *  Perform's user authentication
     *
     *@param  auth                         The authentication method instance to
     *      try.
     *@param  serviceToStart               The service instance to start.
     *@return                              The result of the authentication;
     *      this is an AuthenticationProtocolState value.
     *@throws  TransportProtocolException  if an error occurs in the Transport
     *      Protocol
     *@throws  ServiceOperationException   if a critical error occurs in the
     *      service operation
     */
    public int authenticate(SshAuthentication auth, Service serviceToStart)
             throws IOException {
        auth.init(this, transport);
        auth.authenticate(serviceToStart.getServiceName());

        int ret = protocolState.waitForStateUpdate();

        if (ret == AuthenticationProtocolState.COMPLETE) {
            serviceToStart.init(REQUESTING_SERVICE, transport, exchangeHash,
                    nativeSettings);
            serviceToStart.start();
        }

        return ret;
    }


    /**
     *  Informs the authentication protocol that the supplied authentication has
     *  completed
     *
     *@param  authentication               The authentication method instance
     *@throws  TransportProtocolException  if an error occurs in the Transport
     *      Protocol
     *@throws  ServiceOperationException   if a critical error occurs in the
     *      service operation
     */
    public void authenticationComplete(SshAuthentication authentication)
             throws IOException {
        // Add the method to the completed list
        if (!completedAuthentications.contains(authentication.getMethodName())) {
            completedAuthentications.add(authentication.getMethodName());
        }

        // Determine if we have all the required methods
        List required =
                ConfigurationLoader.getServerConfiguration()
                .getRequiredAuthentications();

        Iterator it = required.iterator();

        while (it.hasNext()) {
            if (!completedAuthentications.contains(it.next())) {
                // We have not completed all the required authentications
                sendUserAuthFailure(true);

                return;
            }
        }

        nativeSettings.put("Username", authentication.getUsername());
        // We have all the required authentications
        sendUserAuthSuccess();
    }


    /**
     *  Informs the authentication protocol that the supplied authentication has
     *  failed
     *
     *@param  authentication               The authentication method instance
     *@throws  TransportProtocolException  if an error occurs in the Transport
     *      Protocol
     *@throws  ServiceOperationException   if a critical error occurs in the
     *      service operation
     */
    public void authenticationFailed(SshAuthentication authentication)
             throws IOException {
        sendUserAuthFailure(false);
    }


    /**
     *  Called by the framework if the remote computer disconnects.
     *
     *@param  reason  The reason for disconnection
     */
    public void onDisconnect(String reason) {
        stop();
    }


    /**
     *  Returns the message filter for the required asynchronous messages
     *
     *@return    An array of message id's
     */
    protected int[] getAsyncMessageFilter() {
        int messageFilter[] = new int[4];

        messageFilter[0] = SshMsgUserAuthFailure.SSH_MSG_USERAUTH_FAILURE;
        messageFilter[1] = SshMsgUserAuthSuccess.SSH_MSG_USERAUTH_SUCCESS;
        messageFilter[2] = SshMsgUserAuthBanner.SSH_MSG_USERAUTH_BANNER;
        messageFilter[3] = SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST;

        return messageFilter;
    }


    /**
     *  Called by the Service framework when a registered message has been
     *  received.
     *
     *@param  msg                               The message received
     *@throws  TransportProtocolException       if an error occurs in the
     *      Transport Protocol
     *@throws  ServiceOperationException        if a critical error occurs in
     *      the service operation
     *@throws  AuthenticationProtocolException  if an authentication error
     *      occurs
     */
    protected void onMessageReceived(SshMessage msg)
             throws IOException {
        switch (msg.getMessageId()) {
            case SshMsgUserAuthFailure.SSH_MSG_USERAUTH_FAILURE:
            {
                onMsgUserAuthFailure((SshMsgUserAuthFailure) msg);

                break;
            }

            case SshMsgUserAuthSuccess.SSH_MSG_USERAUTH_SUCCESS:
            {
                onMsgUserAuthSuccess((SshMsgUserAuthSuccess) msg);

                break;
            }

            case SshMsgUserAuthBanner.SSH_MSG_USERAUTH_BANNER:
            {
                onMsgUserAuthBanner((SshMsgUserAuthBanner) msg);

                break;
            }

            case SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST:
            {
                onMsgUserAuthRequest((SshMsgUserAuthRequest) msg);

                break;
            }

            default:
                throw new AuthenticationProtocolException("Unregistered message received!");
        }
    }


    /**
     *  Abstract method implementation called when the service is accepted by
     *  the server. We register client side message notifications here.
     *
     *@throws  ServiceOperationException  Thrown if a message registration error
     *      occurs
     */
    protected void onServiceAccept()
             throws ServiceOperationException { }


    /**
     *  Called by the framework when the service is initialized. We do not
     *  currently perform any processing here.
     *
     *@param  startMode                         Indicates whether this service
     *      is operating as a server (Service.ACCEPTING_SERVICE) or client
     *      (Service.REQUESTING_SERVICE)
     *@throws  ServiceOperationException        if a critical error occurs in
     *      the service operation
     *@throws  AuthenticationProtocolException  if an authentication error
     *      occurs
     */
    protected void onServiceInit(int startMode)
             throws ServiceOperationException {
        try {
            log.info("Registering messages");

            if (startMode == Service.ACCEPTING_SERVICE) {
                // Register the required messages
                transport.registerMessage(new Integer(SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST),
                        SshMsgUserAuthRequest.class,
                        messageStore);

                return;
            }

            if (startMode == Service.REQUESTING_SERVICE) {
                transport.registerMessage(new Integer(SshMsgUserAuthFailure.SSH_MSG_USERAUTH_FAILURE),
                        SshMsgUserAuthFailure.class,
                        messageStore);

                transport.registerMessage(new Integer(SshMsgUserAuthSuccess.SSH_MSG_USERAUTH_SUCCESS),
                        SshMsgUserAuthSuccess.class,
                        messageStore);

                transport.registerMessage(new Integer(SshMsgUserAuthBanner.SSH_MSG_USERAUTH_BANNER),
                        SshMsgUserAuthBanner.class,
                        messageStore);

                return;
            }

            throw new AuthenticationProtocolException("Illegal start service mode!");
        } catch (MessageAlreadyRegisteredException e) {
            throw new AuthenticationProtocolException("Required message already registered by another service or protocol");
        }
    }


    /**
     *  Abstract method implementation called when the service is request by a
     *  remote client.
     *
     *@throws  TransportProtocolException       if an error occurs in the
     *      Transport Protocol
     *@throws  ServiceOperationException        if a critical error occurs in
     *      the service operation
     *@throws  AuthenticationProtocolException  if an authentication error
     *      occurs
     */
    protected void onServiceRequest()
             throws IOException {
        availableAuths = SshAuthenticationFactory.getSupportedMethods();

        // Accept the service request
        sendServiceAccept();

        // Send a user auth banner if configured
        ServerConfiguration server =
                ConfigurationLoader.getServerConfiguration();

        if (server == null) {
            throw new AuthenticationProtocolException("Server configuration unavailable");
        }

        String bannerFile = server.getAuthenticationBanner();

        if (bannerFile != null) {
            if (bannerFile.length() > 0) {
                InputStream in = ConfigurationLoader.loadFile(bannerFile);

                if (in != null) {
                        byte data[] = new byte[in.available()];
                        in.read(data);
                        in.close();

                        SshMsgUserAuthBanner bannerMsg =
                                new SshMsgUserAuthBanner(new String(data));
                        transport.sendMessage(bannerMsg, this);

                } else {
                    log.info("The banner file '" + bannerFile
                            + "' was not found");
                }
            }
        }
    }


    /**
     *  Handles the SSH_MSG_USERAUTH_BANNER message. The user should be shown
     *  the banner message.
     *
     *@param  msg  The message received.
     */
    private void onMsgUserAuthBanner(SshMsgUserAuthBanner msg) {
        banner = msg.getBanner();
    }


    /**
     *  Handles the SSH_MSG_USERAUTH_FAILURE message.
     *
     *@param  msg  The message received.
     */
    private void onMsgUserAuthFailure(SshMsgUserAuthFailure msg) {
        if (msg.getPartialSuccess() && !isNoneRequest) {
            log.info("Previous authentication method succeeded but additional method(s) are required");
        } else {
            log.info("Previous authentication method failed; try an alternative method");
        }

        // Record the list of authenticaitons available
        availableAuths = msg.getAvailableAuthentications();

        // If this is a none request set our state to ready
        if (isNoneRequest) {
            protocolState.setValue(AuthenticationProtocolState.READY);

            return;
        }

        // Update the state object with the correct state
        if (msg.getPartialSuccess()) {
            protocolState.setValue(AuthenticationProtocolState.PARTIAL);
        } else {
            protocolState.setValue(AuthenticationProtocolState.FAILED);
        }

    }


    /**
     *  Handles the SSH_USERAUTH_REQUEST message
     *
     *@param  msg
     *@throws  TransportProtocolException  if an error occurs in the Transport
     *      Protocol
     *@throws  ServiceOperationException   if a critical error occures in the
     *      service operation
     */
    private void onMsgUserAuthRequest(SshMsgUserAuthRequest msg)
             throws IOException,
            ServiceOperationException {
        if (msg.getMethodName().equals("none")) {

            sendUserAuthFailure(false);
        } else {
            // If the service is supported then perfrom the authentication
            if (acceptServices.containsKey(msg.getServiceName())) {
                String method = msg.getMethodName();

                if (availableAuths.contains(method)) {
                    SshAuthentication auth =
                            SshAuthenticationFactory.newInstance(method);
                    serviceToStart = msg.getServiceName();
                    auth.init(this, transport);
                    auth.setUsername(msg.getUsername());
                    auth.authenticate(msg, nativeSettings);
                } else {
                    sendUserAuthFailure(false);
                }
            } else {
                sendUserAuthFailure(false);
            }
        }
    }


    /**
     *  Handles the SSH_MSG_USERAUTH_SUCCESS message.
     *
     *@param  msg  The message received
     */
    private void onMsgUserAuthSuccess(SshMsgUserAuthSuccess msg) {
        log.debug("Authentication succeeded");

        // Update the state
        protocolState.setValue(AuthenticationProtocolState.COMPLETE);

        // We can now stop the authentication service
        stop();
    }


    /**
     *  Sends the SSH_USERAUTH_FAILURE message
     *
     *@param  success                      True if a partial success
     *@throws  TransportProtocolException  if an error occurs in the Transport
     *      Protocol
     *@throws  ServiceOperationException   if a critical error occurs in the
     *      service operation
     */
    private void sendUserAuthFailure(boolean success)
             throws IOException,
            ServiceOperationException {
        Iterator it = availableAuths.iterator();
        String auths = null;

        while (it.hasNext()) {
            auths = ((auths == null) ? "" : auths + ",") + (String) it.next();
        }

        SshMsgUserAuthFailure reply = new SshMsgUserAuthFailure(auths, success);
        transport.sendMessage(reply, this);
    }


    /**
     *  Sends the SSH_USERAUTH_SUCCESS message
     *
     *@throws  TransportProtocolException  if an error occurs in the Transport
     *      Protocol
     *@throws  ServiceOperationException   if a critical error occurs in the
     *      service operation
     */
    private void sendUserAuthSuccess()
             throws IOException {
        SshMsgUserAuthSuccess msg = new SshMsgUserAuthSuccess();
        Service service = (Service) acceptServices.get(serviceToStart);
        service.init(Service.ACCEPTING_SERVICE, transport, exchangeHash,
                nativeSettings);
        service.start();
        transport.sendMessage(msg, this);

        protocolState.setValue(AuthenticationProtocolState.COMPLETE);

        stop();
    }
}
