package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.StorageBucket;
import org.jets3t.service.model.StorageOwner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version $Id$
 */
public class S3BucketListService {
    private static final Logger log = Logger.getLogger(S3BucketListService.class);

    public List<StorageBucket> list(final S3Session session) throws IOException {
        try {
            final List<StorageBucket> buckets = new ArrayList<StorageBucket>();
            if(session.getHost().getCredentials().isAnonymousLogin()) {
                if(log.isInfoEnabled()) {
                    log.info("Anonymous cannot list buckets");
                }
                // Listing buckets not supported for thirdparty buckets
                String bucketname = this.getContainer(session.getHost());
                if(StringUtils.isEmpty(bucketname)) {
                    if(StringUtils.isNotBlank(session.getHost().getDefaultPath())) {
                        Path d = PathFactory.createPath(session, session.getHost().getDefaultPath(), Path.DIRECTORY_TYPE);
                        while(!d.getParent().isRoot()) {
                            d = d.getParent();
                        }
                        bucketname = d.getName();
                    }
                    log.info(String.format("Using default path to determine bucket name %s", bucketname));
                }
                if(StringUtils.isEmpty(bucketname)) {
                    log.warn(String.format("No bucket name given in hostname %s", session.getHost().getHostname()));
                    // Rewrite endpoint to default S3 endpoint
                    session.configure(session.getHost().getProtocol().getDefaultHostname());
                    bucketname = session.getHost().getHostname(true);
                }
                if(!session.getClient().isBucketAccessible(bucketname)) {
                    session.error("Cannot read container configuration",
                            new ServiceException(String.format("Bucket %s not accessible", bucketname)));
                }
                final S3Bucket bucket = new S3Bucket(bucketname);
                try {
                    StorageOwner owner = session.getClient().getBucketAcl(bucketname).getOwner();
                    bucket.setOwner(owner);
                }
                catch(ServiceException e) {
                    // ACL not readable by anonymous user.
                    log.warn(e.getMessage());
                }
                buckets.add(bucket);
            }
            else {
                // If bucket is specified in hostname, try to connect to this particular bucket only.
                final String bucketname = this.getContainer(session.getHost());
                if(StringUtils.isNotEmpty(bucketname)) {
                    if(!session.getClient().isBucketAccessible(bucketname)) {
                        session.error("Cannot read container configuration",
                                new ServiceException(String.format("Bucket %s not accessible", bucketname)));
                    }
                    final S3Bucket bucket = new S3Bucket(bucketname);
                    try {
                        StorageOwner owner = session.getClient().getBucketAcl(bucketname).getOwner();
                        bucket.setOwner(owner);
                    }
                    catch(ServiceException e) {
                        // ACL not readable by anonymous or IAM user.
                        log.warn(e.getMessage());
                    }
                    buckets.add(bucket);
                }
                else {
                    // List all buckets owned
                    Collections.addAll(buckets, session.getClient().listAllBuckets());
                }
            }
            return buckets;
        }
        catch(ServiceException failure) {
            final IOException e = new IOException(failure.getMessage());
            e.initCause(failure);
            throw e;
        }
    }

    /**
     * @return Null if no container component in hostname prepended
     */
    protected String getContainer(final Host host) {
        final String hostname = host.getHostname(true);
        if(hostname.equals(host.getProtocol().getDefaultHostname())) {
            return null;
        }
        // Bucket name is available in URL's host name.
        if(hostname.endsWith(host.getProtocol().getDefaultHostname())) {
            // Bucket name is available as S3 subdomain
            return hostname.substring(0, hostname.length() - host.getProtocol().getDefaultHostname().length() - 1);
        }
        return null;
    }
}
