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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class BrickErrorResponseInterceptor extends DisabledServiceUnavailableRetryStrategy implements CancelCallback {
    private static final Logger log = Logger.getLogger(BrickErrorResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final BrickSession session;
    private final AtomicBoolean pairing = new AtomicBoolean();
    private final AtomicBoolean cancel = new AtomicBoolean();

    public BrickErrorResponseInterceptor(final BrickSession session) {
        this.session = session;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_UNAUTHORIZED:
                if(executionCount <= MAX_RETRIES) {
                    log.warn(String.format("Attempt to obtain new pairing keys for response %s", response));
                    try {
                        if(pairing.get()) {
                            cancel.set(true);
                        }
                        pairing.set(true);
                        final Credentials credentials = session.getHost().getCredentials();
                        session.pair(credentials, this);
                        final CredentialsProvider provider = new BasicCredentialsProvider();
                        provider.setCredentials(
                            new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.BASIC),
                            new UsernamePasswordCredentials(credentials.getUsername(), credentials.getPassword()));
                        session.getClient().setCredentials(provider);
                    }
                    catch(BackgroundException e) {
                        log.warn(String.format("Failure obtaining pairing key. %s", e.getDetail()));
                    }
                    finally {
                        pairing.set(false);
                        cancel.set(false);
                    }
                    return true;
                }
        }
        return false;
    }

    @Override
    public void verify() throws ConnectionCanceledException {
        if(cancel.get()) {
            throw new ConnectionCanceledException();
        }
    }
}
