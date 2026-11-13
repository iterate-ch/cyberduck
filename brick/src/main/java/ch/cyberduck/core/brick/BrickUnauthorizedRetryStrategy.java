package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.core.threading.BackgroundActionStateCancelCallback;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class BrickUnauthorizedRetryStrategy implements HttpRequestInterceptor, HttpResponseInterceptor {
    private static final String REST_REQUEST = "brick.rest.request";
    private final ReentrantLock lock = new ReentrantLock();
    private final HostPasswordStore store;
    private final BrickSession session;
    private final LoginCallback prompt;
    private final CancelCallback cancel;
    private volatile long recoveryCount;
    private BackgroundException recoveryFailure;
    private volatile String rejectedSavedKey;

    public BrickUnauthorizedRetryStrategy(final BrickSession session, final HostPasswordStore store, final LoginCallback prompt, final CancelCallback cancel) {
        this.store = store;
        this.session = session;
        this.prompt = prompt;
        this.cancel = cancel;
    }

    public long getRecoveryCount() {
        return recoveryCount;
    }

    public boolean retryRequest(final ApiException failure, final int executionCount, final String rejectedKey, final long requestRecoveryCount) throws BackgroundException {
        if(failure.getCode() != HttpStatus.SC_UNAUTHORIZED) {
            return false;
        }
        if(executionCount > 1) {
            this.invalidate(rejectedKey);
            return false;
        }
        final CancelCallback cancellation = new BackgroundActionRegistryCancelCallback(cancel);
        try {
            do {
                cancellation.verify();
            }
            while(!lock.tryLock(100, TimeUnit.MILLISECONDS));
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionCanceledException(e);
        }
        try {
            cancellation.verify();
            if(requestRecoveryCount != recoveryCount) {
                if(recoveryFailure != null) {
                    throw recoveryFailure;
                }
                return true;
            }
            try {
                recoveryFailure = null;
                final Credentials credentials = session.getHost().getCredentials();
                if(credentials.isPasswordAuthentication() && !StringUtils.equals(rejectedKey, credentials.getPassword())) {
                    return true;
                }
                if(BrickOAuth2AuthorizationService.isKeychainEnabled()) {
                    final String saved = store.findLoginPassword(session.getHost());
                    if(StringUtils.isNotBlank(saved) && !StringUtils.equals(rejectedKey, saved) && !StringUtils.equals(rejectedSavedKey, saved)) {
                        credentials.setPassword(saved).setSaved(false);
                        credentials.setProperty(BrickOAuth2AuthorizationService.ACCOUNT, credentials.getUsername());
                        return true;
                    }
                }
                session.authorize(prompt, cancellation);
                if(credentials.isSaved() && BrickOAuth2AuthorizationService.isKeychainEnabled()) {
                    store.save(session.getHost());
                }
                return true;
            }
            catch(BackgroundException e) {
                recoveryFailure = e;
                if(e instanceof LoginFailureException || e instanceof ConnectionCanceledException) {
                    this.invalidate(rejectedKey);
                }
                throw e;
            }
            finally {
                recoveryCount++;
            }
        }
        finally {
            lock.unlock();
        }
    }

    private void invalidate(final String rejectedKey) {
        final Credentials credentials = session.getHost().getCredentials();
        if(StringUtils.equals(rejectedKey, credentials.getPassword())) {
            if(StringUtils.isNotEmpty(rejectedKey)) {
                rejectedSavedKey = rejectedKey;
            }
            credentials.setPassword(StringUtils.EMPTY).setSaved(false);
        }
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        request.removeHeaders("X-FilesAPI-Key");
        final boolean api = this.isApiRequest(request, context);
        final List<URI> redirects = HttpClientContext.adapt(context).getRedirectLocations();
        if(redirects == null || redirects.isEmpty()) {
            context.setAttribute(REST_REQUEST, api);
        }
        if(api && session.getHost().getCredentials().isPasswordAuthentication()) {
            request.addHeader("X-FilesAPI-Key", session.getHost().getCredentials().getPassword());
        }
    }

    private boolean isApiRequest(final HttpRequest request, final HttpContext context) {
        final HttpHost target = HttpCoreContext.adapt(context).getTargetHost();
        final String path = URI.create(request.getRequestLine().getUri()).getPath();
        return target != null && StringUtils.startsWith(path, "/api/rest/v1/")
                && StringUtils.equalsIgnoreCase(target.getHostName(), session.getHost().getHostname())
                && StringUtils.equalsIgnoreCase(target.getSchemeName(), session.getHost().getProtocol().getScheme().name())
                && (target.getPort() == -1 ? 443 : target.getPort()) == session.getHost().getPort();
    }

    @Override
    public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        final HttpRequest request = HttpCoreContext.adapt(context).getRequest();
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED
                && Boolean.TRUE.equals(context.getAttribute(REST_REQUEST))
                && (request == null || !this.isApiRequest(request, context))) {
            // A redirected storage response must not start REST credential recovery.
            throw new HttpResponseException(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getReasonPhrase());
        }
    }

    private static final class BackgroundActionRegistryCancelCallback implements CancelCallback {
        private final CancelCallback delegate;

        public BackgroundActionRegistryCancelCallback(final CancelCallback delegate) {
            this.delegate = delegate;
        }

        @Override
        public void verify() throws ConnectionCanceledException {
            delegate.verify();
            for(BackgroundAction action : BackgroundActionRegistry.global()) {
                if(null == action) {
                    continue;
                }
                // Fail if any current background action is canceled
                new BackgroundActionStateCancelCallback(action).verify();
            }
        }

        @Override
        public BackgroundAction.Context getContext() {
            return delegate.getContext();
        }
    }
}
