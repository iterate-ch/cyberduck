package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.MacUniqueIdService;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.ctera.model.Attachment;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVRedirectStrategy;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
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
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.DefaultCookieSpec;
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.util.concurrent.Uninterruptibles;

public class CTERASession extends DAVSession {
    private static final Logger log = Logger.getLogger(CTERASession.class);

    private CTERACookieInterceptor cookieInterceptor;

    public CTERASession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public DAVClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setRedirectStrategy(new DAVRedirectStrategy(new PreferencesRedirectCallback()));
        cookieInterceptor = new CTERACookieInterceptor();
        configuration.addInterceptorLast(cookieInterceptor);
        return new DAVClient(new HostUrlProvider().withUsername(false).get(host), configuration);
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        if(this.getPublicInfo().hasWebSSO) {
            this.startWebSSOFlow(cancel);
        }
        else {
            if(StringUtils.isBlank(token)) {
                // Prompt for credentials
                final Credentials input = prompt.prompt(host,
                    MessageFormat.format(LocaleFactory.localizedString(
                        "Login {0} with username and password", "Credentials"), BookmarkNameProvider.toString(host)),
                    LocaleFactory.localizedString("No login credentials could be found in the Keychain", "Credentials"),
                    new LoginOptions(host.getProtocol()).token(false)
                );
                credentials.setUsername(input.getUsername());
                credentials.setPassword(input.getPassword());
                credentials.setSaved(input.isSaved());
                // Flow to get additional token
                //todo
            }
        }
    }

    private void startWebSSOFlow(final CancelCallback cancel) throws BackgroundException {
        final String url = String.format("%s/ServicesPortal/activate?scheme=%s",
            new HostUrlProvider().withUsername(false).withPath(false).get(host), CTERAProtocol.CTERA_REDIRECT_URI
        );
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open browser with URL %s", url));
        }
        if(!browser.open(url)) {
            log.warn(String.format("Failed to launch web browser for %s", url));
        }
        final AtomicReference<String> authenticationCode = new AtomicReference<>();
        final CountDownLatch signal = new CountDownLatch(1);
        final OAuth2TokenListenerRegistry registry = OAuth2TokenListenerRegistry.get();
        registry.register(StringUtils.EMPTY, new OAuth2TokenListener() {
            @Override
            public void callback(final String code) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Callback with code %s", code));
                }
                if(!StringUtils.isBlank(code)) {
                    authenticationCode.set(code);
                }
                signal.countDown();
            }
        });
        while(!Uninterruptibles.awaitUninterruptibly(signal, 500, TimeUnit.MILLISECONDS)) {
            cancel.verify();
        }

        // Attach device
        final HttpPost attach = new HttpPost("/ServicesPortal/public/users?format=jsonext");
        try {
            attach.setEntity(
                new StringEntity(
                    this.getAttachmentAsString(authenticationCode.get(),
                        URIEncoder.encode(InetAddress.getLocalHost().getHostName()), new MacUniqueIdService().getUUID()),
                    ContentType.create("application/xml", StandardCharsets.UTF_8.name()
                    )
                ));
            final AttachDeviceResponse response = client.execute(attach, new AbstractResponseHandler<AttachDeviceResponse>() {
                @Override
                public AttachDeviceResponse handleEntity(final HttpEntity entity) throws IOException {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(entity.getContent(), AttachDeviceResponse.class);
                }
            });
            token.
                setSharedSecret(response.sharedSecret).
                setDeviceId(response.deviceUID);
        }

        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }

        // WebDAV authentication
        this.webdavAuthenticate(token);
        host.getCredentials().setToken(token.toString());
        host.getCredentials().setSaved(true);
        return token;
    }

    private void webdavAuthenticate(final CTERAToken token) throws BackgroundException {
        final HttpPost login = new HttpPost("/ServicesPortal/api/login?format=jsonext");
        try {
            login.setEntity(
                new StringEntity(String.format("j_username=device%%5c%s&j_password=%s", token.getDeviceId(), token.getSharedSecret()),
                    ContentType.APPLICATION_FORM_URLENCODED
                )
            );
            client.execute(login, new AbstractResponseHandler<Void>() {
                @Override
                public Void handleResponse(final HttpResponse response) throws IOException {
                    final Header header = response.getFirstHeader("Set-Cookie");
                    try {
                        final Cookie cookie = new DefaultCookieSpec().parse(header, new CookieOrigin(host.getHostname(), host.getPort(), StringUtils.EMPTY, true)).get(0);
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Received cookie %s", cookie));
                        }
                        token.setCookie(cookie.getValue());
                        return super.handleResponse(response);
                    }
                    catch(MalformedCookieException e) {
                        throw new IOException("Unable to parse cookie", e);
                    }
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

    private Attachment getAttachment(final String code, final String server, final String hostname, final String mac) {
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
        ssoActivationCode.setVal(code);
        paramsAttributes.add(ssoActivationCode);
        final Attachment.Attribute password = new Attachment.Attribute();
        password.setId("password");
        paramsAttributes.add(password);
        final Attachment.Attribute host = new Attachment.Attribute();
        host.setId("hostname");
        host.setVal(URIEncoder.encode(hostname));
        paramsAttributes.add(host);

        return attachment;
    }

    private String getAttachmentAsString(final String code, final String server, final String hostname, final String mac) throws JsonProcessingException {
        final Attachment attachment = this.getAttachment(code, server, hostname, mac);
        final XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writeValueAsString(attachment);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class PublicInfo {
        public boolean hasWebSSO;
    }

    /*
    {
        "$class":"AttachDeviceRespond",
        "deviceName": "Test-device",
        "deviceUID": 00000000000,
        "lastLogin": "2021-02-03T12:03:00",
        "sharedSecret": "********************************"
     }
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class AttachDeviceResponse {
        public String deviceUID;
        public String sharedSecret;
    }

    private static class CTERAToken {
        private String deviceId;
        private String sharedSecret;
        private String cookie;

        public static CTERAToken parse(final String token) throws BackgroundException {
            final String[] t = token.split(":");
            if(t.length < 3) {
                log.error("Unable to parse token");
                throw new LoginCanceledException();
            }
            return new CTERAToken().
                setDeviceId(t[0]).
                setSharedSecret(t[1]).
                setCookie(t[2]);
        }

        public String getDeviceId() {
            return deviceId;
        }

        public CTERAToken setDeviceId(final String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public String getSharedSecret() {
            return sharedSecret;
        }

        public CTERAToken setSharedSecret(final String sharedSecret) {
            this.sharedSecret = sharedSecret;
            return this;
        }

        public String getCookie() {
            return cookie;
        }

        public CTERAToken setCookie(final String cookie) {
            this.cookie = cookie;
            return this;
        }

        @Override
        public String toString() {
            return String.format("%s:%s:%s", deviceId, sharedSecret, cookie);
        }
    }

    private class CTERACookieInterceptor implements HttpRequestInterceptor {

        private String cookie;

        @Override
        public void process(final HttpRequest request, final HttpContext httpContext) {
            if(request instanceof HttpRequestWrapper) {
                final HttpRequestWrapper wrapper = (HttpRequestWrapper) request;
                if(null != wrapper.getTarget()) {
                    if(StringUtils.equals(wrapper.getTarget().getHostName(), host.getHostname())) {
                        if(StringUtils.isNotBlank(cookie)) {
                            request.addHeader("Cookie", String.format("JSESSIONID=%s", cookie));
                        }
                    }
                }
            }
        }

        public CTERACookieInterceptor setCookie(final String cookie) {
            this.cookie = cookie;
            return this;
        }
    }
}
