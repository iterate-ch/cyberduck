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
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.ctera.auth.CteraTokens;
import ch.cyberduck.core.ctera.model.PortalSession;
import ch.cyberduck.core.ctera.model.PublicInfo;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.CustomActions;
import ch.cyberduck.core.features.DefaultFileIdProvider;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CteraSession extends DAVSession {
    private static final Logger log = LogManager.getLogger(CteraSession.class);

    private static final String PUBLIC_INFO_PATH = "/ServicesPortal/public/publicInfo?format=jsonext";
    private static final String CURRENT_SESSION_PATH = "/ServicesPortal/api/currentSession?format=jsonext";

    protected CteraAuthenticationHandler authentication;
    protected PublicInfo info;

    public CteraSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected DAVClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.disableRedirectHandling();
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new ExecutionCountServiceUnavailableRetryStrategy(authentication = new CteraAuthenticationHandler(this, prompt, cancel))));
        configuration.addInterceptorFirst(new CteraCookieInterceptor());
        final DAVClient client = new DAVClient(new HostUrlProvider().withUsername(false).get(host), configuration);
        final HttpGet request = new HttpGet(PUBLIC_INFO_PATH);
        try {
            info = client.execute(request, new AbstractResponseHandler<PublicInfo>() {
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
        return client;
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        if(!info.hasWebSSO) {
            if(!credentials.validate(host.getProtocol(), new LoginOptions().user(true).password(true))) {
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
        }
        final CteraTokens tokens = authentication.withInfo(info).withCredentials(credentials.getUsername(), credentials.getPassword(),
                CteraTokens.parse(credentials.getToken())).validate();
        credentials.setToken(String.format("%s:%s", tokens.getDeviceId(), tokens.getSharedSecret()));
        if(StringUtils.isBlank(credentials.getUsername())) {
            final HttpGet request = new HttpGet(CURRENT_SESSION_PATH);
            try {
                credentials.setUsername(client.execute(request, new AbstractResponseHandler<PortalSession>() {
                    @Override
                    public PortalSession handleEntity(final HttpEntity entity) throws IOException {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(entity.getContent(), PortalSession.class);
                    }
                }).username);
            }
            catch(IOException e) {
                throw new HttpExceptionMappingService().map(e);
            }
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            authentication.logout();
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
        if(type == Metadata.class) {
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
        if(type == FileIdProvider.class) {
            return (T) new DefaultFileIdProvider();
        }
        return super._getFeature(type);
    }
}
