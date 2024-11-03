package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.ProxyCredentialsStore;
import ch.cyberduck.core.ProxyCredentialsStoreFactory;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthOption;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.impl.auth.win.WindowsCredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.client.WinHttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

public class CallbackProxyAuthenticationStrategy extends ProxyAuthenticationStrategy {
    private static final Logger log = LogManager.getLogger(CallbackProxyAuthenticationStrategy.class);

    private static final String PROXY_CREDENTIALS_INPUT_ID = "cyberduck.credentials.input";

    private static final List<String> DEFAULT_SCHEME_PRIORITY =
        Collections.unmodifiableList(Arrays.asList(
            AuthSchemes.SPNEGO,
            AuthSchemes.KERBEROS,
            AuthSchemes.NTLM,
            AuthSchemes.CREDSSP,
            AuthSchemes.DIGEST,
            AuthSchemes.BASIC));

    private static final List<String> IWA_SCHEME_PRIORITY =
        Collections.unmodifiableList(Arrays.asList(
            AuthSchemes.SPNEGO,
            AuthSchemes.NTLM));

    private final Preferences preferences
        = PreferencesFactory.get();

    private final Host bookmark;
    private final LoginCallback prompt;
    private final ProxyCredentialsStore keychain;

    public CallbackProxyAuthenticationStrategy(final Host bookmark, final LoginCallback prompt) {
        this(ProxyCredentialsStoreFactory.get(), bookmark, prompt);
    }

    public CallbackProxyAuthenticationStrategy(final ProxyCredentialsStore keychain, final Host bookmark, final LoginCallback prompt) {
        this.keychain = keychain;
        this.bookmark = bookmark;
        this.prompt = prompt;
    }

    @Override
    public Queue<AuthOption> select(final Map<String, Header> challenges, final HttpHost authhost, final HttpResponse response, final HttpContext context) throws MalformedChallengeException {
        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final Queue<AuthOption> options = new LinkedList<>();
        final RequestConfig config = clientContext.getRequestConfig();
        Collection<String> authPrefs = config.getProxyPreferredAuthSchemes();
        if(authPrefs == null) {
            authPrefs = DEFAULT_SCHEME_PRIORITY;
        }
        // if available try to authenticate with Integrated Windows Authentication
        if(preferences.getBoolean("connection.proxy.windows.authentication.enable")) {
            if(WinHttpClients.isWinAuthAvailable()) {
                for(String s : IWA_SCHEME_PRIORITY) {
                    final Header challenge = challenges.get(s.toLowerCase(Locale.ROOT));
                    if(challenge != null) {
                        final AuthSchemeProvider provider;
                        switch(s) {
                            case AuthSchemes.SPNEGO:
                                provider = new BackportWindowsNegotiateSchemeFactory(null);
                                break;
                            default:
                                provider = new BackportWindowsNTLMSchemeFactory(null);
                                break;
                        }
                        if(log.isDebugEnabled()) {
                            log.debug("Use provider {} for challenge {}", provider, challenge);
                        }
                        final AuthScheme authScheme = provider.create(context);
                        authScheme.processChallenge(challenge);
                        final AuthScope authScope = new AuthScope(
                            authhost.getHostName(),
                            authhost.getPort(),
                            authScheme.getRealm(),
                            authScheme.getSchemeName());
                        if(log.isDebugEnabled()) {
                            log.debug("Add authentication options for scheme {}", authPrefs);
                        }
                        options.add(new AuthOption(authScheme, new WindowsCredentialsProvider(
                            null == clientContext.getCredentialsProvider() ? new BasicCredentialsProvider() : clientContext.getCredentialsProvider()).getCredentials(authScope)));
                    }
                }
                if(!options.isEmpty()) {
                    return options;
                }
            }
        }
        Credentials credentials = keychain.getCredentials(authhost.toURI());
        if(StringUtils.isEmpty(credentials.getPassword())) {
            try {
                credentials = prompt.prompt(bookmark,
                    StringUtils.EMPTY,
                    String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), authhost.getHostName()),
                    MessageFormat.format(LocaleFactory.localizedString(
                        "Login {0} with username and password", "Credentials"), authhost.getHostName()),
                    new LoginOptions()
                        .icon(bookmark.getProtocol().disk())
                        .usernamePlaceholder(LocaleFactory.localizedString("Username", "Credentials"))
                        .passwordPlaceholder(LocaleFactory.localizedString("Password", "Credentials"))
                        .user(true).password(true)
                );
                if(credentials.isSaved()) {
                    context.setAttribute(PROXY_CREDENTIALS_INPUT_ID, credentials);
                }
            }
            catch(LoginCanceledException ignored) {
                // Ignore dismiss of prompt
                throw new MalformedChallengeException(ignored.getMessage(), ignored);
            }
        }
        final Lookup<AuthSchemeProvider> registry = clientContext.getAuthSchemeRegistry();
        if(registry == null) {
            log.warn("Missing authentication scheme registry in client context");
            return options;
        }
        if(log.isDebugEnabled()) {
            log.debug("Authentication schemes in the order of preference: {}", authPrefs);
        }
        for(final String id : authPrefs) {
            final Header challenge = challenges.get(id.toLowerCase(Locale.ROOT));
            if(challenge != null) {
                final AuthSchemeProvider authSchemeProvider = registry.lookup(id);
                if(authSchemeProvider == null) {
                    continue;
                }
                final AuthScheme authScheme = authSchemeProvider.create(context);
                authScheme.processChallenge(challenge);
                options.add(new AuthOption(authScheme, new NTCredentials(credentials.getUsername(), credentials.getPassword(),
                    preferences.getProperty("webdav.ntlm.workstation"), preferences.getProperty("webdav.ntlm.domain"))));
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("Challenge for {} authentication scheme not available", id);
                    // Try again
                }
            }
        }
        return options;
    }

    @Override
    public void authSucceeded(final HttpHost authhost, final AuthScheme authScheme, final HttpContext context) {
        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final Credentials credentials = clientContext.getAttribute(PROXY_CREDENTIALS_INPUT_ID, Credentials.class);
        if(null != credentials) {
            clientContext.removeAttribute(PROXY_CREDENTIALS_INPUT_ID);
            if(log.isInfoEnabled()) {
                log.info("Save passphrase for proxy {}", authhost);
            }
            keychain.addCredentials(authhost.toURI(), credentials.getUsername(), credentials.getPassword());
        }
        super.authSucceeded(authhost, authScheme, context);
    }

    @Override
    public void authFailed(final HttpHost authhost, final AuthScheme authScheme, final HttpContext context) {
        keychain.deleteCredentials(authhost.getHostName());
        super.authFailed(authhost, authScheme, context);
    }
}
