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

import ch.cyberduck.core.*;
import ch.cyberduck.core.ctera.auth.CteraTokens;
import ch.cyberduck.core.ctera.model.APICredentials;
import ch.cyberduck.core.ctera.model.PortalSession;
import ch.cyberduck.core.ctera.model.PublicInfo;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DefaultUrlProvider;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.shared.DisabledQuotaFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CteraSession extends DAVSession {

    private static final String PUBLIC_INFO_PATH = "/ServicesPortal/public/publicInfo?format=jsonext";
    public static final String CURRENT_SESSION_PATH = "/ServicesPortal/api/currentSession?format=jsonext";
    public static final String API_PATH = "/ServicesPortal/api?format=jsonext";

    protected CteraAuthenticationHandler authentication;
    protected CteraDirectIOInterceptor directio;
    protected PublicInfo info;

    private final HostPasswordStore keychain;
    private final VersionIdProvider versionid = new DefaultVersionIdProvider(this);

    private APICredentials apiCredentials;
    private final ReentrantLock lock = new ReentrantLock();

    public CteraSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        this(host, trust, key, PasswordStoreFactory.get());
    }

    protected CteraSession(final Host host, final X509TrustManager trust, final X509KeyManager key,
                           final HostPasswordStore keychain) {
        super(host, trust, key);
        this.keychain = keychain;
    }

    @Override
    protected DAVClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.disableRedirectHandling();
        if(preferences.getBoolean("ctera.download.directio.enable")) {
            configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                    new ExecutionCountServiceUnavailableRetryStrategy(authentication = new CteraAuthenticationHandler(this, prompt),
                            directio = new CteraDirectIOInterceptor(this))));
            configuration.addInterceptorFirst(directio);
        }
        else {
            configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                    new ExecutionCountServiceUnavailableRetryStrategy(authentication = new CteraAuthenticationHandler(this, prompt))));
        }
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
            credentials.setUsername(this.getPortalSession().username);
        }
    }

    private PortalSession getPortalSession() throws BackgroundException {
        final HttpGet request = new HttpGet(CURRENT_SESSION_PATH);
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

    public APICredentials getOrCreateAPIKeys() throws BackgroundException {
        lock.lock();
        try {
            if(null != apiCredentials) {
                return apiCredentials;
            }
            final String accessKey = keychain.getPassword(toServiceName(host), toAccountNameForAccessKey(host));
            final String secretKey = keychain.getPassword(toServiceName(host), toAccountNameForSecretKey(host));
            final APICredentials credentials;
            if(StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey)) {
                credentials = this.createAPICredentials();
            }
            else {
                credentials = new APICredentials();
                credentials.accessKey = accessKey;
                credentials.secretKey = secretKey;
                apiCredentials = credentials;
            }
            return credentials;
        }
        finally {
            lock.unlock();
        }
    }

    public APICredentials createAPICredentials() throws BackgroundException {
        lock.lock();
        try {
            final HttpPost post = new HttpPost(API_PATH);
            try {
                final String userId = this.getPortalSession().getUserIdFromUserRef();
                post.setEntity(
                        new StringEntity(String.format("<obj><att id=\"type\"><val>user-defined</val></att><att id=\"name\"><val>createApiKey</val></att><att id=\"param\"><val>%s</val></att></obj>",
                                userId), ContentType.TEXT_XML
                        )
                );
                final APICredentials credentials = this.getClient().execute(post, new AbstractResponseHandler<APICredentials>() {
                    @Override
                    public APICredentials handleEntity(final HttpEntity entity) throws IOException {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(entity.getContent(), APICredentials.class);
                    }
                });
                keychain.addPassword(toServiceName(host), toAccountNameForAccessKey(host), credentials.accessKey);
                keychain.addPassword(toServiceName(host), toAccountNameForSecretKey(host), credentials.secretKey);
                return apiCredentials = credentials;
            }
            catch(HttpResponseException e) {
                throw new DefaultHttpResponseExceptionMappingService().map(e);
            }
            catch(IOException e) {
                throw new HttpExceptionMappingService().map(e);
            }
        }
        finally {
            lock.unlock();
        }
    }

    private static String toAccountNameForAccessKey(final Host bookmark) {
        return String.format("API Access Key (%s)", bookmark.getCredentials().getUsername());
    }

    private static String toAccountNameForSecretKey(final Host bookmark) {
        return String.format("API Secret Key (%s)", bookmark.getCredentials().getUsername());
    }

    private static String toServiceName(final Host bookmark) {
        return new DefaultUrlProvider(bookmark).toUrl(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)),
                EnumSet.of(DescriptiveUrl.Type.provider)).find(DescriptiveUrl.Type.provider).getUrl();
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            authentication.logout();
        }
        finally {
            super.logout();
            versionid.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == VersionIdProvider.class) {
            if(preferences.getBoolean("ctera.download.directio.enable")) {
                return (T) versionid;
            }
            return null;
        }
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
            if(preferences.getBoolean("ctera.download.directio.enable")) {
                return (T) new CteraDelegatingReadFeature(this);
            }
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
        if(type == Quota.class) {
            return (T) new DisabledQuotaFeature();
        }
        if(type == Bulk.class) {
            if(preferences.getBoolean("ctera.download.directio.enable")) {
                return (T) new CteraBulkFeature(this, versionid);
            }
            return (T) new DisabledBulkFeature();
        }
        return super._getFeature(type);
    }
}
