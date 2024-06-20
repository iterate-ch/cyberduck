package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.MacUniqueIdService;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.ctera.auth.CteraTokens;
import ch.cyberduck.core.ctera.model.AttachDeviceResponse;
import ch.cyberduck.core.ctera.model.Attachment;
import ch.cyberduck.core.ctera.model.PublicInfo;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.oauth.OAuth2TokenListenerRegistry;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.urlhandler.SchemeHandler;
import ch.cyberduck.core.urlhandler.SchemeHandlerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.util.concurrent.Uninterruptibles;

public class CteraAuthenticationHandler implements ServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(CteraAuthenticationHandler.class);

    public static final String AUTH_PATH = "/ServicesPortal/api/login?format=jsonext";
    public static final String LOGOUT_PATH = "/ServicesPortal/api/logout?format=jsonext";
    public static final String ATTACH_DEVICE_ACTIVATION_CODE_PATH = "/ServicesPortal/public/users?format=jsonext";
    public static final String ATTACH_DEVICE_USERNAME_PATH = "/ServicesPortal/public/users/%s?format=jsonext";

    private final CteraSession session;
    private final LoginCallback prompt;
    private final CancelCallback cancel;

    private final HostPasswordStore store = PasswordStoreFactory.get();

    /**
     * Currently valid tokens
     */
    private CteraTokens tokens = CteraTokens.EMPTY;
    private PublicInfo info = new PublicInfo();
    private String username = StringUtils.EMPTY;
    private String password = StringUtils.EMPTY;

    public CteraAuthenticationHandler(final CteraSession session, final LoginCallback prompt, final CancelCallback cancel) {
        this.session = session;
        this.prompt = prompt;
        this.cancel = cancel;
    }

    public CteraAuthenticationHandler withCredentials(final String username, final String password, final CteraTokens tokens) {
        this.username = username;
        this.password = password;
        this.tokens = tokens;
        return this;
    }

    public CteraAuthenticationHandler withInfo(final PublicInfo info) {
        this.info = info;
        return this;
    }

    public CteraTokens validate() throws BackgroundException {
        if(tokens.validate()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Authorize with saved tokens %s", tokens));
            }
        }
        else {
            tokens = this.attach();
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Authorize with tokens %s", tokens));
        }
        try {
            this.authorize();
        }
        catch(AccessDeniedException e) {
            // Try to re-authenticate with new tokens
            log.warn(String.format("Failure %s authorizing with tokens %s", e, tokens));
            tokens = this.attach();
            this.authorize();
        }
        return tokens;
    }

    private CteraTokens attach() throws BackgroundException {
        if(info.hasWebSSO) {
            if(log.isDebugEnabled()) {
                log.debug("Start new flow attaching device with activation code");
            }
            return this.attachDeviceWithActivationCode(this.startWebSSOFlow(cancel));
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug("Start new flow attaching device with username and password");
            }
            return this.attachDeviceWithUsernamePassword(username, password);
        }
    }

    public void authorize() throws BackgroundException {
        final HttpPost login = new HttpPost(AUTH_PATH);
        try {
            login.setEntity(
                    new StringEntity(String.format("j_username=device%%5c%s&j_password=%s",
                            tokens.getDeviceId(), tokens.getSharedSecret()), ContentType.APPLICATION_FORM_URLENCODED
                    )
            );
            session.getClient().execute(login, new AbstractResponseHandler<Void>() {
                @Override
                public Void handleResponse(final HttpResponse response) throws IOException {
                    if(!response.containsHeader("Set-Cookie")) {
                        log.warn(String.format("No cookie in response %s", response));
                    }
                    else {
                        final Header header = response.getFirstHeader("Set-Cookie");
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Received cookie %s", header));
                        }
                    }
                    return super.handleResponse(response);
                }

                @Override
                public Void handleEntity(final HttpEntity entity) {
                    return null;
                }
            });
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    protected void logout() throws BackgroundException {
        final HttpPost request = new HttpPost(LOGOUT_PATH);
        try {
            session.getClient().execute(request, new BasicResponseHandler());
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    /**
     * @return Activation code
     */
    private String startWebSSOFlow(final CancelCallback cancel) throws BackgroundException {
        final AtomicReference<String> activationCode = new AtomicReference<>();
        final CountDownLatch signal = new CountDownLatch(1);
        final OAuth2TokenListenerRegistry registry = OAuth2TokenListenerRegistry.get();
        registry.register(StringUtils.EMPTY, code -> {
            if(log.isInfoEnabled()) {
                log.info(String.format("Callback with code %s", code));
            }
            if(!StringUtils.isBlank(code)) {
                activationCode.set(code);
            }
            signal.countDown();
        });
        final SchemeHandler schemeHandler = SchemeHandlerFactory.get();
        schemeHandler.setDefaultHandler(new Application(PreferencesFactory.get().getProperty("application.identifier")),
                Collections.singletonList(PreferencesFactory.get().getProperty("oauth.handler.scheme")));
        final String url = String.format("%s/ServicesPortal/activate?scheme=%s",
                new HostUrlProvider().withUsername(false).withPath(false).get(session.getHost()), CteraProtocol.CTERA_REDIRECT_URI
        );
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open browser with URL %s", url));
        }
        if(!BrowserLauncherFactory.get().open(url)) {
            throw new LoginCanceledException(new LocalAccessDeniedException(String.format("Failed to launch web browser for %s", url)));
        }
        while(!Uninterruptibles.awaitUninterruptibly(signal, Duration.ofMillis(500))) {
            cancel.verify();
        }
        return activationCode.get();
    }

    /**
     * @param activationCode Activation code from Web SSO
     * @return Device and shared secret
     */
    private CteraTokens attachDeviceWithActivationCode(final String activationCode) throws BackgroundException {
        final HttpPost request = new HttpPost(ATTACH_DEVICE_ACTIVATION_CODE_PATH);
        try {
            request.setEntity(
                    new StringEntity(
                            getAttachmentAsString(activationCode, new HostPreferences(session.getHost()).getProperty("ctera.attach.devicetype"), null,
                                    URIEncoder.encode(InetAddress.getLocalHost().getHostName()), new MacUniqueIdService().getUUID()),
                            ContentType.create("application/xml", StandardCharsets.UTF_8.name()
                            )
                    ));
        }
        catch(JsonProcessingException | UnknownHostException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        final AttachDeviceResponse response = this.attachDevice(request);
        return new CteraTokens(response.deviceUID, response.sharedSecret);
    }

    /**
     * @return Device and shared secret
     */
    private CteraTokens attachDeviceWithUsernamePassword(final String username, final String password) throws BackgroundException {
        final HttpPost request = new HttpPost(String.format(ATTACH_DEVICE_USERNAME_PATH, username));
        try {
            request.setEntity(
                    new StringEntity(
                            getAttachmentAsString(null, new HostPreferences(session.getHost()).getProperty("ctera.attach.devicetype"), password,
                                    URIEncoder.encode(InetAddress.getLocalHost().getHostName()), new MacUniqueIdService().getUUID()),
                            ContentType.create("application/xml", StandardCharsets.UTF_8.name()
                            )
                    ));
        }
        catch(JsonProcessingException | UnknownHostException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        final AttachDeviceResponse response = this.attachDevice(request);
        return new CteraTokens(response.deviceUID, response.sharedSecret);
    }

    private AttachDeviceResponse attachDevice(final HttpPost request) throws BackgroundException {
        final AttachDeviceResponse response;
        final AtomicReference<Attachment> error = new AtomicReference<>();
        try {
            response = session.getClient().execute(request, new AbstractResponseHandler<AttachDeviceResponse>() {
                @Override
                public AttachDeviceResponse handleResponse(final HttpResponse response) throws IOException {
                    switch(response.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                            final XmlMapper mapper = new XmlMapper();
                            try {
                                error.set(mapper.readValue(response.getEntity().getContent(), Attachment.class));
                                for(Attachment.Attribute attr : error.get().getAttributes()) {
                                    if("msg".equals(attr.getId())) {
                                        if("Invalid username or password".equals(attr.getVal())) {
                                            log.error(attr.getVal());
                                        }
                                        else {
                                            log.error(String.format("Failure attaching the device %s", attr.getVal()));
                                            error.set(null);
                                        }
                                    }
                                }
                            }
                            catch(IOException e) {
                                log.error("Error parsing response", e);
                            }
                    }
                    return super.handleResponse(response);
                }

                @Override
                public AttachDeviceResponse handleEntity(final HttpEntity entity) throws IOException {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(entity.getContent(), AttachDeviceResponse.class);
                }
            });
        }
        catch(HttpResponseException e) {
            if(error.get() != null) {
                throw new LoginFailureException("Invalid username or password");
            }
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return response;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_MOVED_TEMPORARILY:
                try {
                    log.info(String.format("Attempt to refresh cookie for failure %s", response));
                    this.validate();
                    // Try again
                    return true;
                }
                catch(BackgroundException e) {
                    log.error(String.format("Failure refreshing cookie. %s", e));
                    return false;
                }
        }
        return false;
    }

    @Override
    public long getRetryInterval() {
        return 0L;
    }

    private static String getAttachmentAsString(final String activationCode, final String attachDeviceType, final String password,
                                                final String hostname, final String mac) throws JsonProcessingException {
        return new XmlMapper().writeValueAsString(toAttachment(activationCode, attachDeviceType, password, hostname, mac));
    }


    private static Attachment toAttachment(final String activationCode, final String attachDeviceType,
                                           final String password, final String hostname, final String mac) {
        final Attachment attachment = new Attachment();
        final ArrayList<Attachment.Attribute> attributes = new ArrayList<>();
        attachment.setAttributes(attributes);
        final Attachment.Attribute type = new Attachment.Attribute();
        type.setId("type");
        type.setVal("user-defined");
        attributes.add(type);
        final Attachment.Attribute name = new Attachment.Attribute();
        name.setId("name");
        name.setVal("attachMobileDevice");
        attributes.add(name);
        final Attachment.Attribute param = new Attachment.Attribute();
        param.setId("param");
        final Attachment.AttachedMobileDeviceParams params = new Attachment.AttachedMobileDeviceParams();
        param.setParams(params);
        attributes.add(param);
        final ArrayList<Attachment.Attribute> paramsAttributes = new ArrayList<>();
        params.setAtt(paramsAttributes);
        final Attachment.Attribute deviceType = new Attachment.Attribute();
        deviceType.setId("deviceType");
        deviceType.setVal(attachDeviceType);
        paramsAttributes.add(deviceType);
        final Attachment.Attribute deviceMac = new Attachment.Attribute();
        deviceMac.setId("deviceMac");
        deviceMac.setVal(mac);
        paramsAttributes.add(deviceMac);
        final Attachment.Attribute ssoActivationCode = new Attachment.Attribute();
        ssoActivationCode.setId("ssoActivationCode");
        ssoActivationCode.setVal(activationCode);
        paramsAttributes.add(ssoActivationCode);
        final Attachment.Attribute pwd = new Attachment.Attribute();
        pwd.setId("password");
        pwd.setVal(password);
        paramsAttributes.add(pwd);
        final Attachment.Attribute host = new Attachment.Attribute();
        host.setId("hostname");
        host.setVal(URIEncoder.encode(hostname));
        paramsAttributes.add(host);
        return attachment;
    }
}
