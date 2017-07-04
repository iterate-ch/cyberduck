package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.AuthApi;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

public class SDSErrorResponseInterceptor extends DisabledServiceUnavailableRetryStrategy {
    private static final Logger log = Logger.getLogger(SDSErrorResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final SDSSession session;

    private String user = StringUtils.EMPTY;
    private String password = StringUtils.EMPTY;

    public SDSErrorResponseInterceptor(final SDSSession session) {
        this.session = session;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_UNAUTHORIZED:
                if(executionCount <= MAX_RETRIES) {
                    // The provided token is valid for two hours, every usage resets this period to two full hours again. Logging off invalidates the token.
                    try {
                        final LoginResponse login = new AuthApi(session.getClient()).login(new LoginRequest()
                                .authType(session.getHost().getProtocol().getAuthorization())
                                .language("en")
                                .login(user)
                                .password(password)
                        );
                        session.setToken(login.getToken());
                        return true;
                    }
                    catch(ApiException e) {
                        log.warn(String.format("Attempt to renew expired auth token failed. %s", e.getMessage()));
                        return false;
                    }
                }
        }
        return false;
    }

    public void setTokens(final String user, final String password) {
        this.user = user;
        this.password = password;
    }
}
