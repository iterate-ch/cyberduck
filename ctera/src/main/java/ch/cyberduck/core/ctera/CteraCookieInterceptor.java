package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class CteraCookieInterceptor implements HttpResponseInterceptor {

    private static final Logger log = LogManager.getLogger(CteraCookieInterceptor.class);

    @Override
    public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        if(response.containsHeader("Set-Cookie")) {
            final HttpClientContext clientContext = HttpClientContext.adapt(context);
            if(StringUtils.equals(CteraAuthenticationHandler.AUTH_PATH, clientContext.getRequest().getRequestLine().getUri()) &&
                    response.getStatusLine().getStatusCode() == HttpStatus.SC_OK &&
                    HttpPost.METHOD_NAME.equals(clientContext.getRequest().getRequestLine().getMethod())) {
                log.debug(String.format("Accept cookie %s from login response", response.getFirstHeader("Set-Cookie")));
            }
            else {
                response.removeHeaders("Set-Cookie");
            }
        }
    }
}
