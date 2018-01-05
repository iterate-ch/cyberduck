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
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

public class CallbackProxyAuthenticationStrategy extends ProxyAuthenticationStrategy {
    private static final Logger log = Logger.getLogger(CallbackProxyAuthenticationStrategy.class);

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

    private static final List<String> DEFAULT_SCHEME_PRIORITY =
        Collections.unmodifiableList(Arrays.asList(
            AuthSchemes.SPNEGO,
            AuthSchemes.KERBEROS,
            AuthSchemes.NTLM,
            AuthSchemes.CREDSSP,
            AuthSchemes.DIGEST,
            AuthSchemes.BASIC));

    @Override
    public Queue<AuthOption> select(final Map<String, Header> challenges, final HttpHost authhost, final HttpResponse response, final HttpContext context) throws MalformedChallengeException {
        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final Queue<AuthOption> options = new LinkedList<AuthOption>();
        final Lookup<AuthSchemeProvider> registry = clientContext.getAuthSchemeRegistry();
        if(registry == null) {
            return options;
        }
        final RequestConfig config = clientContext.getRequestConfig();
        Collection<String> authPrefs = config.getProxyPreferredAuthSchemes();
        if(authPrefs == null) {
            authPrefs = DEFAULT_SCHEME_PRIORITY;
        }
        if(log.isDebugEnabled()) {
            log.debug("Authentication schemes in the order of preference: " + authPrefs);
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
                final AuthScope authScope = new AuthScope(
                    authhost.getHostName(),
                    authhost.getPort(),
                    authScheme.getRealm(),
                    authScheme.getSchemeName());

                final Credentials saved = keychain.getCredentials(authhost.getHostName());
                if(StringUtils.isEmpty(saved.getPassword())) {
                    try {
                        final Credentials input = prompt.prompt(bookmark,
                            bookmark.getCredentials().getUsername(),
                            String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), authhost.getHostName()),
                            authScheme.getRealm(),
                            new LoginOptions().user(true).password(true)
                        );
                        if(input.isSaved()) {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Save passphrase for proxy %s", authhost));
                            }
                            keychain.addCredentials(authhost.getHostName(), input.getUsername(), input.getPassword());
                        }
                        options.add(new AuthOption(authScheme, new NTCredentials(input.getUsername(), input.getPassword(),
                            preferences.getProperty("webdav.ntlm.workstation"), preferences.getProperty("webdav.ntlm.domain"))));
                    }
                    catch(LoginCanceledException ignored) {
                        // Ignore dismiss of prompt
                    }
                }
                else {
                    options.add(new AuthOption(authScheme, new NTCredentials(saved.getUsername(), saved.getPassword(),
                        preferences.getProperty("webdav.ntlm.workstation"), preferences.getProperty("webdav.ntlm.domain"))));
                }
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("Challenge for " + id + " authentication scheme not available");
                    // Try again
                }
            }
        }
        return options;
    }
}
