package ch.cyberduck.core.brick;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVRedirectStrategy;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVUploadFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Pairing;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.PreferencesRedirectCallback;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import com.google.common.util.concurrent.Uninterruptibles;

public class BrickSession extends DAVSession {
    private static final Logger log = Logger.getLogger(BrickSession.class);

    public BrickSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public DAVClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.setRedirectStrategy(new DAVRedirectStrategy(new PreferencesRedirectCallback()));
        return new DAVClient(new HostUrlProvider().withUsername(false).get(host), configuration);
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        if(!credentials.isPasswordAuthentication()) {
            // No prompt on explicit connect
            this.pair(host, new DisabledConnectionCallback(), cancel);
        }
        try {
            super.login(proxy, prompt, cancel);
        }
        catch(LoginFailureException e) {
            log.warn(String.format("Attempt to obtain new pairing keys for response %s", e));
            this.pair(host, prompt, cancel);
            super.login(proxy, prompt, cancel);
        }
    }

    public Credentials pair(final Host bookmark, final ConnectionCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final String token = new BrickCredentialsConfigurator().configure(host).getToken();
        final BrickPairingSchedulerFeature scheduler = new BrickPairingSchedulerFeature(this, token, bookmark, cancel);
        // Operate in background until canceled
        final ConnectionCallback lock = new DisabledConnectionCallback() {
            final CountDownLatch lock = new CountDownLatch(1);

            @Override
            public void close(final String input) {
                prompt.close(input);
                // Continue with login
                lock.countDown();
            }

            @Override
            public void warn(final Host bookmark, final String title, final String message,
                             final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                prompt.warn(bookmark, title, message, defaultButton, cancelButton, preference);
                try {
                    if(!BrowserLauncherFactory.get().open(
                        String.format("https://app.files.com/login_from_desktop?pairing_key=%s&platform=%s&computer=%s", token,
                            URIEncoder.encode(new PreferencesUseragentProvider().get()), URIEncoder.encode(InetAddress.getLocalHost().getHostName()))
                    )) {
                        throw new LoginCanceledException();
                    }
                }
                catch(UnknownHostException e) {
                    throw new ConnectionCanceledException(e);
                }
                // Wait for status response from pairing scheduler
                Uninterruptibles.awaitUninterruptibly(lock);
            }
        };
        scheduler.repeat(lock);
        // Await reply
        lock.warn(bookmark, String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), bookmark.getHostname()),
            LocaleFactory.localizedString("The desktop application session has expired or been revoked.", "Brick"),
            LocaleFactory.localizedString("Open in Web Browser"), LocaleFactory.localizedString("Cancel"), null);
        // Not canceled
        scheduler.shutdown();
        // When connect attempt is interrupted will throw connection cancel failure
        cancel.verify();
        return bookmark.getCredentials();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Write.class) {
            return (T) new BrickWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DAVUploadFeature(new BrickWriteFeature(this));
        }
        if(type == Timestamp.class) {
            return (T) new BrickTimestampFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new BrickUrlProvider(host);
        }
        if(type == Pairing.class) {
            return (T) new BrickPairingFeature(this);
        }
        return super._getFeature(type);
    }
}
