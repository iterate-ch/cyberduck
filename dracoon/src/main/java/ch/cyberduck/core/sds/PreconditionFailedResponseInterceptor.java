package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreconditionFailedResponseInterceptor extends OAuth2ErrorResponseInterceptor {

    private static final Logger log = LogManager.getLogger(PreconditionFailedResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final Host bookmark;
    private final OAuth2RequestInterceptor service;
    private final LoginCallback prompt;
    private final ServiceUnavailableRetryStrategy next;

    public PreconditionFailedResponseInterceptor(final Host bookmark,
                                                 final OAuth2RequestInterceptor service,
                                                 final LoginCallback prompt,
                                                 final ServiceUnavailableRetryStrategy next) {
        super(bookmark, service, prompt);
        this.bookmark = bookmark;
        this.service = service;
        this.prompt = prompt;
        this.next = next;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_PRECONDITION_FAILED:
                if(executionCount <= MAX_RETRIES) {
                    try {
                        log.warn(String.format("Invalidate OAuth tokens due to failed precondition %s", response));
                        // Reset OAuth Tokens
                        bookmark.getCredentials().setOauth(OAuthTokens.EMPTY);
                        service.save(service.authorize(bookmark, prompt, new DisabledCancelCallback(), OAuth2AuthorizationService.FlowType.AuthorizationCode));
                        // Try again
                        return true;
                    }
                    catch(BackgroundException e) {
                        log.warn(String.format("Failure %s refreshing OAuth tokens", e));
                    }
                }
                else {
                    log.warn(String.format("Skip retry for response %s after %d executions", response, executionCount));
                }
                return false;
        }
        return next.retryRequest(response, executionCount, context);
    }
}
