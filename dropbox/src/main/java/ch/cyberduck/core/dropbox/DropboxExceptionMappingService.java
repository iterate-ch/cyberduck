package ch.cyberduck.core.dropbox;

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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;

import org.apache.commons.lang3.StringUtils;

import java.io.StringReader;
import java.time.Duration;

import com.dropbox.core.DbxException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.RetryException;
import com.dropbox.core.ServerException;
import com.dropbox.core.v2.files.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class DropboxExceptionMappingService extends AbstractExceptionMappingService<DbxException> {

    @Override
    public BackgroundException map(final DbxException failure) {
        final StringBuilder buffer = new StringBuilder();
        final JsonParser parser = new JsonParser();
        try {
            final JsonObject json = parser.parse(new StringReader(failure.getMessage())).getAsJsonObject();
            final JsonObject error = json.getAsJsonObject("error");
            if(null == error) {
                this.append(buffer, failure.getMessage());
            }
            else {
                final JsonPrimitive tag = error.getAsJsonPrimitive(".tag");
                if(null == tag) {
                    this.append(buffer, failure.getMessage());
                }
                else {
                    this.append(buffer, StringUtils.replace(tag.getAsString(), "_", " "));
                }
            }
        }
        catch(JsonParseException e) {
            // Ignore
            this.append(buffer, failure.getMessage());
        }
        if(failure instanceof InvalidAccessTokenException) {
            return new LoginFailureException(buffer.toString(), failure);
        }
        if(failure instanceof RetryException) {
            final Duration delay = Duration.ofMillis(((RetryException) failure).getBackoffMillis());
            return new RetriableAccessDeniedException(buffer.toString(), delay);
        }
        if(failure instanceof ServerException) {
            return new ConnectionRefusedException(buffer.toString(), failure);
        }
        if(failure instanceof GetMetadataErrorException) {
            final GetMetadataError error = ((GetMetadataErrorException) failure).errorValue;
            final LookupError lookup = error.getPathValue();
            switch(lookup.tag()) {
                case MALFORMED_PATH:
                case OTHER:
                    return new InteroperabilityException(buffer.toString(), failure);
                case NOT_FOUND:
                case NOT_FILE:
                case NOT_FOLDER:
                    return new NotfoundException(buffer.toString(), failure);
                case RESTRICTED_CONTENT:
                    return new AccessDeniedException(buffer.toString(), failure);
            }
        }
        if(failure instanceof DeleteErrorException) {
            final DeleteError error = ((DeleteErrorException) failure).errorValue;
            final LookupError lookup = error.getPathLookupValue();
            switch(lookup.tag()) {
                case MALFORMED_PATH:
                case OTHER:
                    return new InteroperabilityException(buffer.toString(), failure);
                case NOT_FOUND:
                case NOT_FILE:
                case NOT_FOLDER:
                    return new NotfoundException(buffer.toString(), failure);
                case RESTRICTED_CONTENT:
                    return new AccessDeniedException(buffer.toString(), failure);
            }
        }
        if(failure instanceof CreateFolderErrorException) {
            final CreateFolderError error = ((CreateFolderErrorException) failure).errorValue;
            final WriteError lookup = error.getPathValue();
            switch(lookup.tag()) {
                case MALFORMED_PATH:
                case OTHER:
                    return new InteroperabilityException(buffer.toString(), failure);
                case DISALLOWED_NAME:
                case NO_WRITE_PERMISSION:
                case CONFLICT:
                    return new AccessDeniedException(buffer.toString(), failure);
                case INSUFFICIENT_SPACE:
                    return new QuotaException(buffer.toString(), failure);
            }
        }
        if(failure instanceof DownloadErrorException) {
            final DownloadError error = ((DownloadErrorException) failure).errorValue;
            final LookupError lookup = error.getPathValue();
            switch(lookup.tag()) {
                case MALFORMED_PATH:
                case OTHER:
                    return new InteroperabilityException(buffer.toString(), failure);
                case NOT_FOUND:
                case NOT_FILE:
                case NOT_FOLDER:
                    return new NotfoundException(buffer.toString(), failure);
                case RESTRICTED_CONTENT:
                    return new AccessDeniedException(buffer.toString(), failure);
            }
        }
        if(failure instanceof UploadErrorException) {
            final UploadError error = ((UploadErrorException) failure).errorValue;
            final UploadWriteFailed lookup = error.getPathValue();
            switch(lookup.getReason().tag()) {
                case CONFLICT:
                case NO_WRITE_PERMISSION:
                case DISALLOWED_NAME:
                    return new AccessDeniedException(buffer.toString(), failure);
                case INSUFFICIENT_SPACE:
                    return new QuotaException(buffer.toString(), failure);
                case MALFORMED_PATH:
                case OTHER:
                    return new InteroperabilityException(buffer.toString(), failure);
            }
        }
        if(failure instanceof UploadSessionFinishErrorException) {
            final UploadSessionFinishError error = ((UploadSessionFinishErrorException) failure).errorValue;
            final WriteError lookup = error.getPathValue();
            switch(lookup.tag()) {
                case MALFORMED_PATH:
                case OTHER:
                    return new InteroperabilityException(buffer.toString(), failure);
                case DISALLOWED_NAME:
                case NO_WRITE_PERMISSION:
                case CONFLICT:
                    return new AccessDeniedException(buffer.toString(), failure);
                case INSUFFICIENT_SPACE:
                    return new QuotaException(buffer.toString(), failure);
            }
        }
        if(failure instanceof GetTemporaryLinkErrorException) {
            final GetTemporaryLinkError error = ((GetTemporaryLinkErrorException) failure).errorValue;
            final LookupError lookup = error.getPathValue();
            switch(lookup.tag()) {
                case NOT_FOUND:
                case NOT_FILE:
                case NOT_FOLDER:
                    return new NotfoundException(buffer.toString(), failure);
                case RESTRICTED_CONTENT:
                    return new AccessDeniedException(buffer.toString(), failure);
                case MALFORMED_PATH:
                case OTHER:
                    return new InteroperabilityException(buffer.toString(), failure);
            }
        }
        if(failure instanceof ListFolderContinueErrorException) {
            final ListFolderContinueError error = ((ListFolderContinueErrorException) failure).errorValue;
            final LookupError lookup = error.getPathValue();
            switch(lookup.tag()) {
                case NOT_FOUND:
                case NOT_FILE:
                case NOT_FOLDER:
                    return new NotfoundException(buffer.toString(), failure);
                case RESTRICTED_CONTENT:
                    return new AccessDeniedException(buffer.toString(), failure);
                case MALFORMED_PATH:
                case OTHER:
                    return new InteroperabilityException(buffer.toString(), failure);
            }
        }
        return new InteroperabilityException(buffer.toString(), failure);
    }
}
