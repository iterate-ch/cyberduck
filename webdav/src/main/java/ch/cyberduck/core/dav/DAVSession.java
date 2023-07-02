package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.dav.microsoft.MicrosoftIISDAVAttributesFinderFeature;
import ch.cyberduck.core.dav.microsoft.MicrosoftIISDAVFindFeature;
import ch.cyberduck.core.dav.microsoft.MicrosoftIISDAVListService;
import ch.cyberduck.core.dav.microsoft.MicrosoftIISDAVReadFeature;
import ch.cyberduck.core.dav.microsoft.MicrosoftIISDAVTimestampFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.http.RedirectCallback;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.ValidatingResponseHandler;

public class DAVSession extends HttpSession<DAVClient> {
    private static final Logger log = LogManager.getLogger(DAVSession.class);

    private final RedirectCallback redirect;
    private final PreferencesReader preferences
            = new HostPreferences(host);

    private ListService list = new DAVListService(this, new DAVAttributesFinderFeature(this));
    private Read read = new DAVReadFeature(this);
    private Timestamp timestamp = new DAVTimestampFeature(this);
    private AttributesFinder attributes = new DAVAttributesFinderFeature(this);
    private Find find = new DAVFindFeature(this);

    public DAVSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        this(host, trust, key, new PreferencesRedirectCallback());
    }

    public DAVSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final RedirectCallback redirect) {
        super(host, trust, key);
        this.redirect = redirect;
    }

    @Override
    protected DAVClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = this.getConfiguration(proxy, prompt);
        return new DAVClient(new HostUrlProvider().withUsername(false).get(host), configuration);
    }

    protected HttpClientBuilder getConfiguration(final Proxy proxy, final LoginCallback prompt) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setRedirectStrategy(new DAVRedirectStrategy(redirect));
        return configuration;
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.shutdown();
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        if(host.getProtocol().isPasswordConfigurable()) {
            final CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.NTLM),
                    new NTCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword(),
                            preferences.getProperty("webdav.ntlm.workstation"), preferences.getProperty("webdav.ntlm.domain"))
            );
            provider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.SPNEGO),
                    new NTCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword(),
                            preferences.getProperty("webdav.ntlm.workstation"), preferences.getProperty("webdav.ntlm.domain"))
            );
            provider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.BASIC),
                    new UsernamePasswordCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword()));
            provider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.DIGEST),
                    new UsernamePasswordCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword()));
            provider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.KERBEROS),
                    new UsernamePasswordCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword()));
            client.setCredentials(provider);
            if(preferences.getBoolean("webdav.basic.preemptive")) {
                switch(proxy.getType()) {
                    case DIRECT:
                    case SOCKS:
                        // Enable preemptive authentication. See HttpState#setAuthenticationPreemptive
                        client.enablePreemptiveAuthentication(host.getHostname(),
                                host.getPort(),
                                host.getPort(),
                                Charset.forName(preferences.getProperty("http.credentials.charset"))
                        );
                        break;
                    default:
                        client.disablePreemptiveAuthentication();
                }
            }
            else {
                client.disablePreemptiveAuthentication();
            }
        }
        if(host.getCredentials().isPassed()) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            }
            return;
        }
        try {
            final Path home = new DelegatingHomeFeature(new WorkdirHomeFeature(host), new DefaultPathHomeFeature(host)).find();
            final HttpHead head = new HttpHead(new DAVPathEncoder().encode(home));
            try {
                client.execute(head, new MicrosoftIISFeaturesResponseHandler());
            }
            catch(SardineException e) {
                switch(e.getStatusCode()) {
                    case HttpStatus.SC_NOT_FOUND:
                        if(log.isWarnEnabled()) {
                            log.warn(String.format("Ignore failure %s", e));
                        }
                        break;
                    case HttpStatus.SC_NOT_IMPLEMENTED:
                    case HttpStatus.SC_FORBIDDEN:
                    case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
                    case HttpStatus.SC_METHOD_NOT_ALLOWED:
                        if(log.isWarnEnabled()) {
                            log.warn(String.format("Failed HEAD request to %s with %s. Retry with PROPFIND.",
                                    host, e.getResponsePhrase()));
                        }
                        cancel.verify();
                        // Possibly only HEAD requests are not allowed
                        list.list(home, new DisabledListProgressListener() {
                            @Override
                            public void chunk(final Path parent, final AttributedList<Path> list) throws ListCanceledException {
                                try {
                                    cancel.verify();
                                }
                                catch(ConnectionCanceledException e) {
                                    throw new ListCanceledException(list, e);
                                }
                            }
                        });
                        break;
                    case HttpStatus.SC_BAD_REQUEST:
                        if(preferences.getBoolean("webdav.basic.preemptive")) {
                            if(log.isWarnEnabled()) {
                                log.warn(String.format("Disable preemptive authentication for %s due to failure %s",
                                        host, e.getResponsePhrase()));
                            }
                            cancel.verify();
                            client.disablePreemptiveAuthentication();
                            client.execute(head, new MicrosoftIISFeaturesResponseHandler());
                        }
                        else {
                            throw new DAVExceptionMappingService().map(e);
                        }
                        break;
                    default:
                        throw new DAVExceptionMappingService().map(e);
                }
            }
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean alert(final ConnectionCallback callback) throws BackgroundException {
        if(super.alert(callback)) {
            // Propose protocol change if HEAD request redirects to HTTPS
            final Path home = new DelegatingHomeFeature(new DefaultPathHomeFeature(host)).find();
            try {
                final RequestConfig context = client.context().getRequestConfig();
                final HttpHead request = new HttpHead(new DAVPathEncoder().encode(home));
                request.setConfig(RequestConfig.copy(context).setRedirectsEnabled(false).build());
                final Header location = client.execute(request, new ValidatingResponseHandler<Header>() {
                    @Override
                    public Header handleResponse(final HttpResponse response) {
                        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
                            return response.getFirstHeader(HttpHeaders.LOCATION);
                        }
                        return null;
                    }
                });
                // Reset default redirect configuration in context
                client.context().setRequestConfig(RequestConfig.copy(context).setRedirectsEnabled(true).build());
                if(null != location) {
                    final URL url = new URL(location.getValue());
                    if(StringUtils.equals(Scheme.https.name(), url.getProtocol())) {
                        try {
                            callback.warn(host,
                                    MessageFormat.format(LocaleFactory.localizedString("Unsecured {0} connection", "Credentials"), host.getProtocol().getName()),
                                    MessageFormat.format("{0} {1}.", MessageFormat.format(LocaleFactory.localizedString("The server supports encrypted connections. Do you want to switch to {0}?", "Credentials"),
                                            new DAVSSLProtocol().getName()), LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support")),
                                    LocaleFactory.localizedString("Continue", "Credentials"),
                                    LocaleFactory.localizedString("Change", "Credentials"),
                                    String.format("connection.unsecure.%s", host.getHostname()));
                            // Continue chosen. Login using plain FTP.
                        }
                        catch(LoginCanceledException e) {
                            // Protocol switch
                            host.setHostname(url.getHost());
                            host.setProtocol(ProtocolFactory.get().forScheme(Scheme.davs));
                            return false;
                        }
                    }
                }
                // Continue with default alert
            }
            catch(SardineException e) {
                // Ignore failure
                log.warn(String.format("Ignore failed HEAD request to %s with %s.", host, e.getResponsePhrase()));
            }
            catch(IOException e) {
                throw new HttpExceptionMappingService().map(e);
            }
            return preferences.getBoolean("webdav.basic.preemptive");
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) list;
        }
        if(type == Directory.class) {
            return (T) new DAVDirectoryFeature(this);
        }
        if(type == Read.class) {
            return (T) read;
        }
        if(type == Write.class) {
            return (T) new DAVWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DAVUploadFeature(this);
        }
        if(type == Delete.class) {
            return (T) new DAVDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new DAVMoveFeature(this);
        }
        if(type == Headers.class) {
            return (T) new DAVMetadataFeature(this);
        }
        if(type == Metadata.class) {
            return (T) new DAVMetadataFeature(this);
        }
        if(type == Copy.class) {
            return (T) new DAVCopyFeature(this);
        }
        if(type == Find.class) {
            return (T) find;
        }
        if(type == AttributesFinder.class) {
            return (T) attributes;
        }
        if(type == Timestamp.class) {
            return (T) timestamp;
        }
        if(type == Quota.class) {
            return (T) new DAVQuotaFeature(this);
        }
        if(type == Lock.class) {
            if(preferences.getBoolean("webdav.lock.enable")) {
                return (T) new DAVLockFeature(this);
            }
            return null;
        }
        if(type == Touch.class) {
            return (T) new DAVTouchFeature(this);
        }
        return super._getFeature(type);
    }

    private final class MicrosoftIISFeaturesResponseHandler extends ValidatingResponseHandler<Void> {
        @Override
        public Void handleResponse(final HttpResponse response) throws IOException {
            if(Arrays.stream(response.getAllHeaders()).anyMatch(header ->
                    HttpHeaders.SERVER.equals(header.getName()) && StringUtils.contains(header.getValue(), "Microsoft-IIS"))) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Microsoft-IIS backend detected in response %s", response));
                }
                list = new MicrosoftIISDAVListService(DAVSession.this, new MicrosoftIISDAVAttributesFinderFeature(DAVSession.this));
                timestamp = new MicrosoftIISDAVTimestampFeature(DAVSession.this);
                attributes = new MicrosoftIISDAVAttributesFinderFeature(DAVSession.this);
                if(preferences.getBoolean("webdav.microsoftiis.header.translate")) {
                    read = new MicrosoftIISDAVReadFeature(DAVSession.this);
                    find = new MicrosoftIISDAVFindFeature(DAVSession.this);
                }
            }
            this.validateResponse(response);
            return null;
        }
    }
}
