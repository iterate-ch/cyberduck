package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2026 AiNET and Blomp. All rights reserved.
 * https://ai.net/
 * https://www.blomp.com/
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Region;

/**
 * Quota of a single container from its bytes used and the {@code X-Container-Meta-Quota-Bytes} metadata set by the
 * container quotas middleware. Reads the response headers directly as header names are case-insensitive and proxies
 * may send them in lower case.
 */
public class SwiftContainerQuotaFeature implements Quota {
    private static final Logger log = LogManager.getLogger(SwiftContainerQuotaFeature.class);

    private final SwiftSession session;
    private final SwiftRegionService regionService;
    private final Path container;

    public SwiftContainerQuotaFeature(final SwiftSession session, final SwiftRegionService regionService, final Path container) {
        this.session = session;
        this.regionService = regionService;
        this.container = container;
    }

    @Override
    public Space get() throws BackgroundException {
        try {
            final Region region = regionService.lookup(container);
            final Client client = session.getClient();
            // Renews an expired token, which the direct request below does not do
            client.getContainerInfo(region, container.getName());
            final HttpHead request = new HttpHead(region.getStorageUrl(container.getName()));
            request.setHeader("X-Auth-Token", client.getAuthentication().getAuthToken());
            try(CloseableHttpResponse response = client.getClient().execute(request)) {
                final int status = response.getStatusLine().getStatusCode();
                if(status / 100 != 2) {
                    throw new DefaultHttpResponseExceptionMappingService().map("Failure to read attributes of {0}",
                            new HttpResponseException(status, response.getStatusLine().getReasonPhrase()), container);
                }
                return toSpace(response);
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Failure to read attributes of {0}", e, container);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, container);
        }
    }

    /**
     * @param response Response to HEAD request for container
     * @return Bytes used and available from quota. Available is unlimited if no quota is set.
     */
    static Space toSpace(final HttpResponse response) {
        final long used = toLong(response.getFirstHeader("X-Container-Bytes-Used"));
        final long quota = toLong(response.getFirstHeader("X-Container-Meta-Quota-Bytes"));
        if(used < 0) {
            log.warn("Missing bytes used in response for container");
        }
        if(quota < 0) {
            log.debug("No quota set for container");
            return new Space(Math.max(0L, used), Long.MAX_VALUE);
        }
        return new Space(Math.max(0L, used), Math.max(0L, quota - Math.max(0L, used)));
    }

    private static long toLong(final Header header) {
        if(null == header) {
            return -1L;
        }
        return NumberUtils.toLong(StringUtils.trim(header.getValue()), -1L);
    }
}
