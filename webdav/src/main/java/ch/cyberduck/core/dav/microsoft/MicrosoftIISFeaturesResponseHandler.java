package ch.cyberduck.core.dav.microsoft;

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

import ch.cyberduck.core.dav.DAVSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

import com.github.sardine.impl.handler.ValidatingResponseHandler;

public final class MicrosoftIISFeaturesResponseHandler extends ValidatingResponseHandler<Void> {
    private static final Logger log = LogManager.getLogger(MicrosoftIISFeaturesResponseHandler.class);

    private final DAVSession.HttpCapabilities capabilities;

    public MicrosoftIISFeaturesResponseHandler(final DAVSession.HttpCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public Void handleResponse(final HttpResponse response) throws IOException {
        if(Arrays.stream(response.getAllHeaders()).anyMatch(header ->
                HttpHeaders.SERVER.equals(header.getName()) && StringUtils.contains(header.getValue(), "Microsoft-IIS"))) {
            log.info("Microsoft-IIS backend detected in response {}", response);
            capabilities.withIIS(true);
        }
        this.validateResponse(response);
        return null;
    }
}
