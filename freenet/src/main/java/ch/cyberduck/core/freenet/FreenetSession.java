package ch.cyberduck.core.freenet;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVRedirectStrategy;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

public class FreenetSession extends DAVSession {
    private static final Logger log = Logger.getLogger(FreenetSession.class);

    public FreenetSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected DAVClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        // Always inject new pool to builder on connect because the pool is shutdown on disconnect
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setRedirectStrategy(new DAVRedirectStrategy(new PreferencesRedirectCallback()));
        configuration.setUserAgent(new FreenetUserAgentProvider().get());
        return new DAVClient(new HostUrlProvider().withUsername(false).get(host), configuration);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == UrlProvider.class) {
            return (T) new FreenetUrlProvider(host);
        }
        return super.getFeature(type);
    }
}
