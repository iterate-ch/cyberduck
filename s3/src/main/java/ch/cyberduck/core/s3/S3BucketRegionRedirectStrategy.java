package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.features.Location;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;

public class S3BucketRegionRedirectStrategy extends DefaultRedirectStrategy {
    private static final Logger log = LogManager.getLogger(S3BucketRegionRedirectStrategy.class);

    private final RequestEntityRestStorageService service;
    private final Host host;

    public S3BucketRegionRedirectStrategy(final RequestEntityRestStorageService service, final Host host) {
        this.service = service;
        this.host = host;
    }

    @Override
    public HttpUriRequest getRedirect(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException {
        if(response.containsHeader("x-amz-bucket-region")) {
            final Header header = response.getFirstHeader("x-amz-bucket-region");
            log.warn("Received redirect response {} with {}", response, header);
            if(service.getDisableDnsBuckets()) {
                final String message = String.format("Virtual host style requests are disabled but received redirect response %s with x-amz-bucket-region %s",
                        response, response.getFirstHeader("x-amz-bucket-region"));
                log.warn(message);
                throw new RedirectException(message);
            }
            final String region = header.getValue();
            final String uri = StringUtils.replaceEach(request.getRequestLine().getUri(),
                    host.getProtocol().getRegions().stream().map(Location.Name::getIdentifier).toArray(String[]::new),
                    host.getProtocol().getRegions().stream().map(location -> region).toArray(String[]::new));
            final HttpUriRequest redirect = RequestBuilder.copy(request).setUri(uri).build();
            log.warn("Retry request with URI {}", redirect.getURI());
            final String bucketName = ServiceUtils.findBucketNameInHostOrPath(redirect.getURI(),
                    RequestEntityRestStorageService.createRegionSpecificEndpoint(host, region));
            log.debug("Determined bucket {} from request {}", bucketName, request);
            // Update cache with new region
            if(bucketName != null) {
                log.debug("Cache region {} for bucket {}", region, bucketName);
                service.getRegionEndpointCache().putRegionForBucketName(bucketName, region);
            }
            return redirect;
        }
        return super.getRedirect(request, response, context);
    }
}
