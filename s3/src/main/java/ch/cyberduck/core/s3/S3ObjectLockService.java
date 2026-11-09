package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Lookup if a bucket has Object Lock enabled. Uploads to a bucket with Object Lock require a Content-MD5 header.
 */
public class S3ObjectLockService {
    private static final Logger log = LogManager.getLogger(S3ObjectLockService.class);

    private final S3Session session;
    private final PathContainerService containerService;
    private final XmlMapper mapper = new XmlMapper();

    private final LRUCache<Path, Boolean> cache
            = LRUCache.build(10);

    public S3ObjectLockService(final S3Session session) {
        this.session = session;
        this.containerService = new S3PathContainerService(session.getHost());
    }

    /**
     * @param file File or bucket
     * @return False only when confirmed that Object Lock is not enabled for the bucket. True when enabled or when the
     * configuration cannot be determined, to remain compatible with the requirements of the bucket.
     */
    public boolean isObjectLockEnabled(final Path file) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        if(bucket.isRoot()) {
            return false;
        }
        final Boolean cached = cache.get(bucket);
        if(null != cached) {
            return cached;
        }
        final boolean enabled = this.lookup(bucket);
        cache.put(bucket, enabled);
        return enabled;
    }

    private boolean lookup(final Path bucket) throws BackgroundException {
        try {
            final HttpResponse response = session.getClient().performRestGet(bucket.getName(), null,
                    Collections.singletonMap("object-lock", StringUtils.EMPTY), null, new int[]{HttpStatus.SC_OK});
            try (final InputStream in = response.getEntity().getContent()) {
                return StringUtils.equals("Enabled", StringUtils.trim(mapper.readValue(in, ObjectLockConfiguration.class).enabled));
            }
            catch(JsonProcessingException e) {
                log.warn("Failure parsing object lock configuration for {} {}", bucket.getName(), e.getMessage());
                // Cannot be determined
                return true;
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
            finally {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
        catch(ServiceException e) {
            try {
                throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, bucket);
            }
            catch(NotfoundException n) {
                // ObjectLockConfigurationNotFoundError
                log.debug("No object Lock configuration for bucket {}", bucket);
                return false;
            }
            catch(AccessDeniedException | InteroperabilityException l) {
                log.warn("Missing permission to read bucket object lock configuration for {} {}", bucket.getName(), e.getMessage());
                // Cannot be determined
                return true;
            }
        }
    }

    /**
     * Response of GET Bucket ObjectLockConfiguration
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class ObjectLockConfiguration {
        @JsonProperty("ObjectLockEnabled")
        public String enabled;
    }
}
