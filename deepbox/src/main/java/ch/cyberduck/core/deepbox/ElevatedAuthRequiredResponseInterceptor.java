package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Restart OAuth flow when access to a node requires elevated authentication (2FA) not present in the access token
 */
public class ElevatedAuthRequiredResponseInterceptor extends OAuth2ErrorResponseInterceptor {
    private static final Logger log = LogManager.getLogger(ElevatedAuthRequiredResponseInterceptor.class);

    public static final String ELEVATED_AUTH_REQUIRED = "elevated.auth.required";

    private final OAuth2RequestInterceptor service;
    private final CancelCallback cancel;

    public ElevatedAuthRequiredResponseInterceptor(final Host bookmark, final OAuth2RequestInterceptor service, final CancelCallback cancel) {
        super(bookmark, service, cancel);
        this.service = service;
        this.cancel = cancel;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_FORBIDDEN:
                if(StringUtils.equals(ELEVATED_AUTH_REQUIRED, toMessageId(response))) {
                    try {
                        log.warn("Invalidate OAuth tokens due to elevated authentication required {}", response);
                        service.save(service.authorize(cancel));
                        // Try again
                        return true;
                    }
                    catch(BackgroundException e) {
                        log.warn("Failure {} refreshing OAuth tokens", e.getMessage());
                    }
                }
        }
        return false;
    }

    /**
     * Response entity is buffered to allow the content to be read again when the request is not retried
     *
     * @return Message identifier from error response or null
     */
    private static String toMessageId(final HttpResponse response) {
        final HttpEntity entity = response.getEntity();
        if(null == entity) {
            return null;
        }
        try {
            EntityUtils.updateEntity(response, new BufferedHttpEntity(entity));
            final JsonNode node = new ObjectMapper().readTree(response.getEntity().getContent());
            if(null == node) {
                return null;
            }
            return node.path("messageId").asText(null);
        }
        catch(IOException e) {
            log.warn("Failure {} parsing error response {}", e, response);
            return null;
        }
    }
}
