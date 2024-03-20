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

import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.MacUniqueIdService;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.ctera.auth.CteraTokens;
import ch.cyberduck.core.ctera.model.AttachDeviceResponse;
import ch.cyberduck.core.ctera.model.Attachment;
import ch.cyberduck.core.ctera.model.PortalSession;
import ch.cyberduck.core.ctera.model.PublicInfo;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVRedirectStrategy;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.CustomActions;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.oauth.OAuth2TokenListenerRegistry;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.urlhandler.SchemeHandler;
import ch.cyberduck.core.urlhandler.SchemeHandlerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.util.concurrent.Uninterruptibles;


public class CteraSession extends DAVSession {
    // TODO CTERA-136 create Ctera***Feature for all preflight cases? Keeping all checks in one location (CteraSession) for now.
    // <ctera:readpermission>                                 [Read.preflight()  - files only? see traversepermission ]
    // <ctera:writepermission>                                [Write.preflight() - files only? see Createfilepermission/CreateDirectoriespermission ]
    // <ctera:executepermission>           - Files only       [                  - ?]
    // <ctera:deletepermission>                               [Delete.preflight(), Move.preflight()                  - do we need default writepermission as well?]
    // <ctera:traversepermission>          - Directories only [                  - what does it mean, listing a folder? List/Find features?]
    // <ctera:Createfilepermission>        - Directories only [Touch.preflight(), Copy.preflight(), Move.preflight() - do we need default writepermission on target folder as well?]
    // <ctera:CreateDirectoriespermission> - Directories only [Directory.preflight(), Copy.preflight(), Move.preflight() - do we need default writepermission on target folder as well?]

    private static final Logger log = LogManager.getLogger(CteraSession.class);

    private final CteraAuthenticationHandler authentication = new CteraAuthenticationHandler(this);

    public CteraSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected DAVClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setRedirectStrategy(new DAVRedirectStrategy(new PreferencesRedirectCallback()));
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new ExecutionCountServiceUnavailableRetryStrategy(authentication)));
        return new DAVClient(new HostUrlProvider().withUsername(false).get(host), configuration);
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        if(StringUtils.isBlank(credentials.getToken())) {
            final CteraTokens tokens = this.getTokens(credentials, prompt, cancel);
            this.authenticateWithTokens(tokens);
            credentials.setToken(tokens.toString());
        }
        else {
            try {
                this.authenticateWithTokens(CteraTokens.parse(credentials.getToken()));
            }
            catch(AccessDeniedException e) {
                // Try to re-authenticate with new tokens
                final CteraTokens tokens = this.getTokens(credentials, prompt, cancel);
                this.authenticateWithTokens(tokens);
                credentials.setToken(tokens.toString());
            }
        }

        if(StringUtils.isBlank(credentials.getUsername())) {
            credentials.setUsername(this.getCurrentSession().username);
        }
    }

    private CteraTokens getTokens(final Credentials credentials, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final AttachDeviceResponse response;
        if(this.getPublicInfo().hasWebSSO) {
            response = this.startWebSSOFlow(cancel);
        }
        else {
            response = this.startDesktopFlow(prompt, credentials);
        }
        return new CteraTokens(response.deviceUID, response.sharedSecret);
    }

    private void authenticateWithTokens(final CteraTokens tokens) throws BackgroundException {
        authentication.setTokens(tokens);
        authentication.authenticate();
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            this.logoutCurrentSession();
        }
        finally {
            super.logout();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Touch.class) {
            return (T) new CteraTouchFeature(this);
        }
        if(type == Directory.class) {
            return (T) new CteraDirectoryFeature(this);
        }
        if(type == Move.class) {
            return (T) new CteraMoveFeature(this);
        }
        if(type == Lock.class) {
            return null;
        }
        if(type == Timestamp.class) {
            return null;
        }
        if(type == CustomActions.class) {
            return (T) new CteraCustomActions(this);
        }
        if(type == UrlProvider.class) {
            return (T) new CteraUrlProvider(host);
        }
        if(type == AttributesFinder.class) {
            return (T) new CteraAttributesFinderFeature(this);
        }
        if(type == ListService.class) {
            return (T) new CteraListService(this);
        }
        if(type == AclPermission.class) {
            return (T) new CteraAclPermissionFeature(this);
        }
        if(type == Read.class) {
            return (T) new CteraReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new CteraWriteFeature(this);
        }
        if(type == Delete.class) {
            return (T) new CteraDeleteFeature(this);
        }
        if(type == Copy.class) {
            return (T) new CteraCopyFeature(this);
        }
        return super._getFeature(type);
    }

    private AttachDeviceResponse startWebSSOFlow(final CancelCallback cancel) throws BackgroundException {
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
                new HostUrlProvider().withUsername(false).withPath(false).get(host), CteraProtocol.CTERA_REDIRECT_URI
        );
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open browser with URL %s", url));
        }
        if(!BrowserLauncherFactory.get().open(url)) {
            log.warn(String.format("Failed to launch web browser for %s", url));
        }
        while(!Uninterruptibles.awaitUninterruptibly(signal, Duration.ofMillis(500))) {
            cancel.verify();
        }
        return this.attachDeviceWithActivationCode(activationCode.get());
    }

    private AttachDeviceResponse startDesktopFlow(final LoginCallback prompt, final Credentials credentials) throws BackgroundException {
        if(StringUtils.isNotBlank(credentials.getUsername()) && StringUtils.isNotBlank(credentials.getPassword())) {
            try {
                return this.attachDeviceWithUsernamePassword(credentials.getUsername(), credentials.getPassword());
            }
            catch(LoginFailureException e) {
                this.prompt(prompt, credentials);
                return this.startDesktopFlow(prompt, credentials);
            }
        }
        else {
            this.prompt(prompt, credentials);
            return this.startDesktopFlow(prompt, credentials);
        }
    }

    private void prompt(final LoginCallback prompt, final Credentials credentials) throws LoginCanceledException {
        final Credentials input = prompt.prompt(host, credentials.getUsername(),
                MessageFormat.format(LocaleFactory.localizedString(
                        "Login {0} with username and password", "Credentials"), BookmarkNameProvider.toString(host)),
                LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"),
                new LoginOptions(host.getProtocol()).token(false).user(true).password(true)
        );
        credentials.setUsername(input.getUsername());
        credentials.setPassword(input.getPassword());
        credentials.setSaved(input.isSaved());
    }


    private AttachDeviceResponse attachDeviceWithActivationCode(final String activationCode) throws BackgroundException {
        final HttpPost attach = new HttpPost("/ServicesPortal/public/users?format=jsonext");
        try {
            attach.setEntity(
                    new StringEntity(
                            getAttachmentAsString(activationCode, new HostPreferences(host).getProperty("ctera.attach.devicetype"), null,
                                    URIEncoder.encode(InetAddress.getLocalHost().getHostName()), new MacUniqueIdService().getUUID()),
                            ContentType.create("application/xml", StandardCharsets.UTF_8.name()
                            )
                    ));
            return this.attachDevice(attach);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    private AttachDeviceResponse attachDeviceWithUsernamePassword(final String username, final String password) throws BackgroundException {
        final HttpPost attach = new HttpPost(String.format("/ServicesPortal/public/users/%s?format=jsonext", username));
        try {
            attach.setEntity(
                    new StringEntity(
                            getAttachmentAsString(null, new HostPreferences(host).getProperty("ctera.attach.devicetype"), password,
                                    URIEncoder.encode(InetAddress.getLocalHost().getHostName()), new MacUniqueIdService().getUUID()),
                            ContentType.create("application/xml", StandardCharsets.UTF_8.name()
                            )
                    ));
            return this.attachDevice(attach);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    private AttachDeviceResponse attachDevice(final HttpPost attach) throws IOException, BackgroundException {
        final AttachDeviceResponse response;
        AtomicReference<Attachment> error = new AtomicReference<>();
        try {
            response = client.execute(attach, new AbstractResponseHandler<AttachDeviceResponse>() {
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
        catch(IOException e) {
            if(error.get() != null) {
                throw new LoginFailureException("Invalid username or password");
            }
            throw e;
        }
        return response;
    }

    private PublicInfo getPublicInfo() throws BackgroundException {
        final HttpGet request = new HttpGet("/ServicesPortal/public/publicInfo?format=jsonext");
        try {
            return client.execute(request, new AbstractResponseHandler<PublicInfo>() {
                @Override
                public PublicInfo handleEntity(final HttpEntity entity) throws IOException {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(entity.getContent(), PublicInfo.class);
                }
            });
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    private PortalSession getCurrentSession() throws BackgroundException {
        final HttpGet request = new HttpGet("/ServicesPortal/api/currentSession?format=jsonext");
        try {
            return client.execute(request, new AbstractResponseHandler<PortalSession>() {
                @Override
                public PortalSession handleEntity(final HttpEntity entity) throws IOException {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(entity.getContent(), PortalSession.class);
                }
            });
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    private void logoutCurrentSession() throws BackgroundException {
        final HttpPost request = new HttpPost("/ServicesPortal/api/logout?format=jsonext");
        try {
            client.execute(request, new BasicResponseHandler());
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    private static Attachment getAttachment(final String activationCode, final String attachDeviceType, final String password, final String hostname, final String mac) {
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

    private static String getAttachmentAsString(final String activationCode, final String attachDeviceType, final String password,
                                                final String hostname, final String mac) throws JsonProcessingException {
        final Attachment attachment = getAttachment(activationCode, attachDeviceType, password, hostname, mac);
        final XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writeValueAsString(attachment);
    }
}
