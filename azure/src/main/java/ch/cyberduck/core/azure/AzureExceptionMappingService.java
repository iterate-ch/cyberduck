package ch.cyberduck.core.azure;

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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.ssl.SSLExceptionMappingService;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.Map;

import com.azure.core.exception.HttpResponseException;
import com.azure.storage.blob.implementation.models.BlobStorageError;
import com.azure.storage.file.datalake.implementation.models.DataLakeStorageError;

import static com.azure.storage.blob.models.BlobErrorCode.BLOCK_COUNT_EXCEEDS_LIMIT;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.ERROR_CODE_HEADER_NAME;

public class AzureExceptionMappingService extends AbstractExceptionMappingService<RuntimeException> {

    @Override
    public BackgroundException map(final RuntimeException failure) {
        if(failure instanceof HttpResponseException) {
            return this.map((HttpResponseException) failure);
        }
        // Failure without response wrapped by the SDK, such as connection or SSL failure or retrieving access token
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof BackgroundException) {
                return (BackgroundException) cause;
            }
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SSLException) {
                return new SSLExceptionMappingService().map((SSLException) cause);
            }
            if(cause instanceof IOException) {
                return new DefaultIOExceptionMappingService().map((IOException) cause);
            }
        }
        return new DefaultExceptionMappingService().map(failure);
    }

    private BackgroundException map(final HttpResponseException failure) {
        final StringBuilder buffer = new StringBuilder();
        if(failure.getValue() instanceof BlobStorageError) {
            final BlobStorageError error = (BlobStorageError) failure.getValue();
            this.append(buffer, error.getMessage());
        }
        if(failure.getValue() instanceof DataLakeStorageError) {
            final DataLakeStorageError error = (DataLakeStorageError) failure.getValue();
            this.append(buffer, error.getMessage());
        }
        if(failure.getValue() instanceof Map) {
            final Map<?, ?> messages = (Map<?, ?>) failure.getValue();
            if(messages.containsKey("Message")) {
                this.append(buffer, messages.get("Message").toString());
            }
        }
        if(buffer.length() == 0) {
            // No structured error details such as from an intermediary
            this.append(buffer, failure.getMessage());
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SSLException) {
                return new SSLExceptionMappingService().map(buffer.toString(), (SSLException) cause);
            }
        }
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof BackgroundException) {
                // Failure retrieving access token
                return (BackgroundException) cause;
            }
        }
        switch(failure.getResponse().getStatusCode()) {
            case 409:
                if(BLOCK_COUNT_EXCEEDS_LIMIT.getValue().equals(failure.getResponse().getHeaderValue(ERROR_CODE_HEADER_NAME))) {
                    // Permanent failure. Append blob exceeds the maximum of 50,000 committed blocks.
                    return new InteroperabilityException(failure.getMessage(), failure);
                }
                break;
            case 403:
                return new LoginFailureException(buffer.toString(), failure);
        }
        return new DefaultHttpResponseExceptionMappingService().map(failure, buffer, failure.getResponse().getStatusCode());
    }
}
