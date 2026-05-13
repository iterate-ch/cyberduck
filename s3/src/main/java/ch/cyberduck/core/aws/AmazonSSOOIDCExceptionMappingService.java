package ch.cyberduck.core.aws;

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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.ssooidc.model.AWSSSOOIDCException;
import com.amazonaws.services.ssooidc.model.AccessDeniedException;
import com.amazonaws.services.ssooidc.model.AuthorizationPendingException;
import com.amazonaws.services.ssooidc.model.ExpiredTokenException;
import com.amazonaws.services.ssooidc.model.InternalServerException;
import com.amazonaws.services.ssooidc.model.InvalidClientException;
import com.amazonaws.services.ssooidc.model.InvalidGrantException;
import com.amazonaws.services.ssooidc.model.InvalidRedirectUriException;
import com.amazonaws.services.ssooidc.model.InvalidRequestException;
import com.amazonaws.services.ssooidc.model.InvalidRequestRegionException;
import com.amazonaws.services.ssooidc.model.InvalidScopeException;
import com.amazonaws.services.ssooidc.model.UnauthorizedClientException;
import com.amazonaws.services.ssooidc.model.UnsupportedGrantTypeException;

public class AmazonSSOOIDCExceptionMappingService extends AbstractExceptionMappingService<AWSSSOOIDCException> {
    private static final Logger log = LogManager.getLogger(AmazonSSOOIDCExceptionMappingService.class);

    @Override
    public BackgroundException map(final AWSSSOOIDCException failure) {
        log.warn("Map failure {}", failure.toString());
        final StringBuilder buffer = new StringBuilder();
        if(failure instanceof AccessDeniedException) {
            this.append(buffer, ((AccessDeniedException) failure).getError_description());
        }
        if(failure instanceof AuthorizationPendingException) {
            this.append(buffer, ((AuthorizationPendingException) failure).getError_description());
        }
        if(failure instanceof ExpiredTokenException) {
            this.append(buffer, ((ExpiredTokenException) failure).getError_description());
        }
        if(failure instanceof InvalidRedirectUriException) {
            this.append(buffer, ((InvalidRedirectUriException) failure).getError_description());
        }
        if(failure instanceof InternalServerException) {
            this.append(buffer, ((InternalServerException) failure).getError_description());
        }
        if(failure instanceof InvalidClientException) {
            this.append(buffer, ((InvalidClientException) failure).getError_description());
        }
        if(failure instanceof InvalidGrantException) {
            this.append(buffer, ((InvalidGrantException) failure).getError_description());
        }
        if(failure instanceof InvalidRequestException) {
            this.append(buffer, ((InvalidRequestException) failure).getError_description());
        }
        if(failure instanceof InvalidRequestRegionException) {
            this.append(buffer, ((InvalidRequestRegionException) failure).getError_description());
        }
        if(failure instanceof InvalidScopeException) {
            this.append(buffer, ((InvalidScopeException) failure).getError_description());
        }
        if(failure instanceof UnauthorizedClientException) {
            this.append(buffer, ((UnauthorizedClientException) failure).getError_description());
        }
        if(failure instanceof UnsupportedGrantTypeException) {
            this.append(buffer, ((UnsupportedGrantTypeException) failure).getError_description());
        }
        return new DefaultHttpResponseExceptionMappingService().map(
                new HttpResponseException(failure.getStatusCode(), buffer.toString()));
    }
}
