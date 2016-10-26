package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Host;

import org.jets3t.service.utils.RestUtils;
import org.jets3t.service.utils.ServiceUtils;

public class S3TorrentUrlProvider {

    private final Host endpoint;

    public S3TorrentUrlProvider(final Host endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Generates a URL string that will return a Torrent file for an object in S3,
     * which file can be downloaded and run in a BitTorrent client.
     *
     * @param bucket the name of the bucket containing the object.
     * @param key    the name of the object.
     * @return a URL to a Torrent file representing the object.
     */
    public String create(final String bucket, final String key) {
        String s3Endpoint = endpoint.getHostname();
        String serviceEndpointVirtualPath = "";

        String bucketNameInPath =
                ServiceUtils.isBucketNameValidDNSName(bucket)
                        ? ""
                        : RestUtils.encodeUrlString(bucket) + "/";
        String urlPath =
                RestUtils.encodeUrlPath(serviceEndpointVirtualPath, "/")
                        + "/" + bucketNameInPath
                        + RestUtils.encodeUrlPath(key, "/");
        return "http://" + ServiceUtils.generateS3HostnameForBucket(
                bucket, false, s3Endpoint)
                + urlPath
                + "?torrent";
    }
}
