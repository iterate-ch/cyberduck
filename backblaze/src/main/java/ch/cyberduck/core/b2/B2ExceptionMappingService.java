package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

import synapticloop.b2.exception.B2ApiException;

public class B2ExceptionMappingService extends AbstractExceptionMappingService<B2ApiException> {
    private static final Logger log = LogManager.getLogger(B2ExceptionMappingService.class);

    private final B2VersionIdProvider fileid;

    public B2ExceptionMappingService(final B2VersionIdProvider fileid) {
        this.fileid = fileid;
    }

    @Override
    public BackgroundException map(final String message, final B2ApiException failure, final Path file) {
        switch(failure.getStatus()) {
            case HttpStatus.SC_BAD_REQUEST:
                fileid.cache(file, null);
        }
        return super.map(message, failure, file);
    }

    @Override
    public BackgroundException map(final B2ApiException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        switch(e.getStatus()) {
            case HttpStatus.SC_FORBIDDEN:
                if("cap_exceeded".equalsIgnoreCase(e.getCode())
                        || "storage_cap_exceeded".equalsIgnoreCase(e.getCode())
                        || "transaction_cap_exceeded".equalsIgnoreCase(e.getCode())) {// Reached the storage cap that you set
                    return new QuotaException(buffer.toString(), e);
                }
                break;
            case HttpStatus.SC_BAD_REQUEST:
                if("duplicate_bucket_name".equalsIgnoreCase(e.getCode())) {
                    return new ConflictException(buffer.toString(), e);
                }
                if("no_such_file".equalsIgnoreCase(e.getCode())) {
                    return new NotfoundException(buffer.toString(), e);
                }
                if("file_not_present".equalsIgnoreCase(e.getCode())) {
                    return new NotfoundException(buffer.toString(), e);
                }
                if("bad_bucket_id".equalsIgnoreCase(e.getCode())) {
                    return new NotfoundException(buffer.toString(), e);
                }
                if("cap_exceeded".equalsIgnoreCase(e.getCode())) {// Reached the storage cap that you set
                    return new QuotaException(buffer.toString(), e);
                }
                if("too_many_buckets".equalsIgnoreCase(e.getCode())) {// Reached the storage cap that you set
                    return new QuotaException(buffer.toString(), e);
                }
                if("bad_request".equalsIgnoreCase(e.getCode())) {
                    if("sha1 did not match data received".equalsIgnoreCase(e.getMessage())) {
                        return new ChecksumException(buffer.toString(), e);
                    }
                    if("checksum did not match data received".equalsIgnoreCase(e.getMessage())) {
                        return new ChecksumException(buffer.toString(), e);
                    }
                    if(StringUtils.lowerCase(e.getMessage()).startsWith("bad file id")) {
                        return new NotfoundException(buffer.toString(), e);
                    }
                }
                break;
            case HttpStatus.SC_UNAUTHORIZED:
                if("expired_auth_token".equalsIgnoreCase(e.getCode())) {
                    return new ExpiredTokenException(buffer.toString(), e);
                }
                break;
            default:
                if(e.getRetry() != null) {
                    // Too Many Requests (429)
                    return new RetriableAccessDeniedException(buffer.toString(), Duration.ofSeconds(e.getRetry()), e);
                }
                break;
        }
        return new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(e.getStatus(), buffer.toString()));
    }
}
