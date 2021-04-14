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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.MacUniqueIdService;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.ctera.auth.CTERATokens;
import ch.cyberduck.core.ctera.model.AttachDeviceResponse;
import ch.cyberduck.core.ctera.model.Attachment;
import ch.cyberduck.core.ctera.model.PublicInfo;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVRedirectStrategy;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.oauth.OAuth2TokenListenerRegistry;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.util.concurrent.Uninterruptibles;

public class CTERASession extends DAVSession implements ServiceUnavailableRetryStrategy {
    private static final Logger log = Logger.getLogger(CTERASession.class);

    private static final String SAML_LOCATION = "https://myapps.microsoft.com/signin/CTERA/e8e5145e-4fac-412e-b87b-fbfc26123827";
    private static final int MAX_RETRIES = 1;

    private CTERATokens tokens;

    public CTERASession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public DAVClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setRedirectStrategy(new DAVRedirectStrategy(new PreferencesRedirectCallback()));
        configuration.setServiceUnavailableRetryStrategy(this);
        return new DAVClient(new HostUrlProvider().withUsername(false).get(host), configuration);
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        final String t = credentials.getToken();
        if(StringUtils.isBlank(t)) {
            tokens = new CTERATokens();
            if(!this.getPublicInfo().hasWebSSO) {
                this.startWebSSOFlow(cancel, tokens);
            }
            else {
                this.startDesktopFlow(prompt, credentials, tokens);
            }
        }
        else {
            tokens = CTERATokens.parse(t);
        }
        this.webdavAuthenticate(tokens);
    }

    private void startWebSSOFlow(final CancelCallback cancel, final CTERATokens tokens) throws BackgroundException {
        final String url = String.format("%s/ServicesPortal/activate?scheme=%s",
            new HostUrlProvider().withUsername(false).withPath(false).get(host), CTERAProtocol.CTERA_REDIRECT_URI
        );
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open browser with URL %s", url));
        }
        if(!BrowserLauncherFactory.get().open(url)) {
            log.warn(String.format("Failed to launch web browser for %s", url));
        }
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
        while(!Uninterruptibles.awaitUninterruptibly(signal, 500, TimeUnit.MILLISECONDS)) {
            cancel.verify();
        }
        this.attachDeviceWithActivationCode(activationCode.get(), tokens);
        host.getCredentials().setToken(tokens.toString());
        host.getCredentials().setSaved(true);
    }

    private void startDesktopFlow(final LoginCallback prompt, final Credentials credentials, final CTERATokens tokens) throws BackgroundException {
        final Credentials input = prompt.prompt(host, credentials.getUsername(),
            MessageFormat.format(LocaleFactory.localizedString(
                "Login {0} with username and password", "Credentials"), BookmarkNameProvider.toString(host)),
            LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"),
            new LoginOptions(host.getProtocol()).token(false).user(true).password(true)
        );
        credentials.setUsername(input.getUsername());
        credentials.setPassword(input.getPassword());
        credentials.setSaved(input.isSaved());
        attachDeviceWithUsernamePassword(input.getUsername(), input.getPassword(), tokens);
    }

    private void attachDeviceWithActivationCode(final String activationCode, final CTERATokens tokens) throws BackgroundException {
        final HttpPost attach = new HttpPost("/ServicesPortal/public/users?format=jsonext");
        try {
            attach.setEntity(
                new StringEntity(
                    this.getAttachmentAsString(activationCode, null,
                        URIEncoder.encode(InetAddress.getLocalHost().getHostName()), new MacUniqueIdService().getUUID()),
                    ContentType.create("application/xml", StandardCharsets.UTF_8.name()
                    )
                ));
            this.attachDevice(attach, tokens);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    private void attachDeviceWithUsernamePassword(final String username, final String password, final CTERATokens tokens) throws BackgroundException {
        final HttpPost attach = new HttpPost(String.format("/ServicesPortal/public/users/%s?format=jsonext", username));
        try {
            attach.setEntity(
                new StringEntity(
                    this.getAttachmentAsString(null, password,
                        URIEncoder.encode(InetAddress.getLocalHost().getHostName()), new MacUniqueIdService().getUUID()),
                    ContentType.create("application/xml", StandardCharsets.UTF_8.name()
                    )
                ));
            this.attachDevice(attach, tokens);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    private void attachDevice(final HttpPost attach, final CTERATokens tokens) throws IOException {
        final AttachDeviceResponse response = client.execute(attach, new AbstractResponseHandler<AttachDeviceResponse>() {
            @Override
            public AttachDeviceResponse handleEntity(final HttpEntity entity) throws IOException {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(entity.getContent(), AttachDeviceResponse.class);
            }
        });
        tokens.
            setSharedSecret(response.sharedSecret).
            setDeviceId(response.deviceUID);
    }

    private void webdavAuthenticate(final CTERATokens tokens) throws BackgroundException {
        final HttpPost login = new HttpPost("/ServicesPortal/api/login?format=jsonext");
        try {
            login.setEntity(
                new StringEntity(String.format("j_username=device%%5c%s&j_password=%s", tokens.getDeviceId(), tokens.getSharedSecret()),
                    ContentType.APPLICATION_FORM_URLENCODED
                )
            );
            client.execute(login, new AbstractResponseHandler<Void>() {
                @Override
                public Void handleResponse(final HttpResponse response) throws IOException {
                    final Header header = response.getFirstHeader("Set-Cookie");
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Received cookie %s", header.getValue()));
                    }
                    return super.handleResponse(response);
                }

                @Override
                public Void handleEntity(final HttpEntity entity) {
                    return null;
                }
            });
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
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

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_MOVED_TEMPORARILY:
                final Header l = response.getFirstHeader(HttpHeaders.LOCATION);
                if(StringUtils.startsWith(l.getValue(), SAML_LOCATION)) {
                    if(executionCount <= MAX_RETRIES) {
                        try {
                            log.info(String.format("Attempt to refresh cookie for failure %s", response));
                            webdavAuthenticate(tokens);
                        }
                        catch(BackgroundException e) {
                            log.error(String.format("Failure refreshing cookie. %s", e));
                            return false;
                        }
                        // Try again
                        return true;
                    }
                    break;
                }
        }
        return false;
    }

    @Override
    public long getRetryInterval() {
        return 0L;
    }

    private static Attachment getAttachment(final String activationCode, final String password, final String hostname, final String mac) {
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
        deviceType.setVal("Mobile");
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

    private static String getAttachmentAsString(final String activationCode, final String password,
                                                final String hostname, final String mac) throws JsonProcessingException {
        final Attachment attachment = getAttachment(activationCode, password, hostname, mac);
        final XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writeValueAsString(attachment);
    }
}
