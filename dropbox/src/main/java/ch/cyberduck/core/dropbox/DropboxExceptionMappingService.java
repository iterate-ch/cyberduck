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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.exception.UnsupportedException;

import org.apache.commons.lang3.StringUtils;

import java.io.StringReader;
import java.time.Duration;

import com.dropbox.core.AccessErrorException;
import com.dropbox.core.DbxException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.ProtocolException;
import com.dropbox.core.RetryException;
import com.dropbox.core.ServerException;
import com.dropbox.core.v2.auth.AccessError;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsError;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class DropboxExceptionMappingService extends AbstractExceptionMappingService<DbxException> {

    @Override
    public BackgroundException map(final DbxException failure) {
        final StringBuilder buffer = new StringBuilder();
        this.parse(buffer, failure.getMessage());
        if(failure instanceof InvalidAccessTokenException) {
            return new LoginFailureException(buffer.toString(), failure);
        }
        if(failure instanceof RetryException) {
            // Rate limit
            final Duration delay = Duration.ofMillis(((RetryException) failure).getBackoffMillis());
            return new RetriableAccessDeniedException(buffer.toString(), delay);
        }
        if(failure instanceof ServerException) {
            return new ConnectionRefusedException(buffer.toString(), failure);
        }
        if(failure instanceof ProtocolException) {
            return new InteroperabilityException(buffer.toString(), failure);
        }
        if(failure instanceof NetworkIOException) {
            return new DefaultIOExceptionMappingService().map(((NetworkIOException) failure).getCause());
        }
        // API failure
        if(failure instanceof GetMetadataErrorException) {
            final GetMetadataError error = ((GetMetadataErrorException) failure).errorValue;
            if(error.isPath()) {
                final LookupError lookup = error.getPathValue();
                this.parse(buffer, lookup.toString());
                switch(lookup.tag()) {
                    case LOCKED:
                        return new LockedException(buffer.toString(), failure);
                    case OTHER:
                        return new InteroperabilityException(buffer.toString(), failure);
                    case NOT_FOUND:
                    case NOT_FILE:
                    case NOT_FOLDER:
                        return new NotfoundException(buffer.toString(), failure);
                    case MALFORMED_PATH:
                    case RESTRICTED_CONTENT:
                        return new AccessDeniedException(buffer.toString(), failure);
                }
            }
        }
        if(failure instanceof DeleteErrorException) {
            final DeleteError error = ((DeleteErrorException) failure).errorValue;
            final LookupError lookup = error.getPathLookupValue();
            this.parse(buffer, lookup.toString());
            switch(lookup.tag()) {
                case LOCKED:
                    return new LockedException(buffer.toString(), failure);
                case OTHER:
                    return new InteroperabilityException(buffer.toString(), failure);
                case NOT_FOUND:
                case NOT_FILE:
                case NOT_FOLDER:
                    return new NotfoundException(buffer.toString(), failure);
                case MALFORMED_PATH:
                case RESTRICTED_CONTENT:
                    return new AccessDeniedException(buffer.toString(), failure);
            }
        }
        if(failure instanceof ListFolderErrorException) {
            final ListFolderError error = ((ListFolderErrorException) failure).errorValue;
            if(error.isPath()) {
                final LookupError lookup = error.getPathValue();
                this.parse(buffer, lookup.toString());
                switch(lookup.tag()) {
                    case LOCKED:
                        return new LockedException(buffer.toString(), failure);
                    case OTHER:
                        return new InteroperabilityException(buffer.toString(), failure);
                    case NOT_FOUND:
                    case NOT_FILE:
                    case NOT_FOLDER:
                        return new NotfoundException(buffer.toString(), failure);
                    case MALFORMED_PATH:
                    case RESTRICTED_CONTENT:
                        return new AccessDeniedException(buffer.toString(), failure);
                }
            }
        }
        if(failure instanceof CreateFolderErrorException) {
            final CreateFolderError error = ((CreateFolderErrorException) failure).errorValue;
            if(error.isPath()) {
                final WriteError lookup = error.getPathValue();
                this.parse(buffer, lookup.toString());
                switch(lookup.tag()) {
                    case OTHER:
                        return new InteroperabilityException(buffer.toString(), failure);
                    case MALFORMED_PATH:
                    case DISALLOWED_NAME:
                    case NO_WRITE_PERMISSION:
                        return new AccessDeniedException(buffer.toString(), failure);
                    case CONFLICT:
                        return new ConflictException(buffer.toString(), failure);
                    case INSUFFICIENT_SPACE:
                        return new QuotaException(buffer.toString(), failure);
                }
            }
        }
        if(failure instanceof SearchErrorException) {
            final SearchError error = ((SearchErrorException) failure).errorValue;
            if(error.isPath()) {
                final LookupError lookup = error.getPathValue();
                this.parse(buffer, lookup.toString());
                switch(lookup.tag()) {
                    case LOCKED:
                        return new LockedException(buffer.toString(), failure);
                    case OTHER:
                        return new InteroperabilityException(buffer.toString(), failure);
                    case NOT_FOUND:
                    case NOT_FILE:
                    case NOT_FOLDER:
                        return new NotfoundException(buffer.toString(), failure);
                    case MALFORMED_PATH:
                    case RESTRICTED_CONTENT:
                        return new AccessDeniedException(buffer.toString(), failure);
                }
            }
        }
        if(failure instanceof DownloadErrorException) {
            final DownloadError error = ((DownloadErrorException) failure).errorValue;
            if(error.isPath()) {
                final LookupError lookup = error.getPathValue();
                this.parse(buffer, lookup.toString());
                switch(lookup.tag()) {
                    case LOCKED:
                        return new LockedException(buffer.toString(), failure);
                    case OTHER:
                        return new InteroperabilityException(buffer.toString(), failure);
                    case NOT_FOUND:
                    case NOT_FILE:
                    case NOT_FOLDER:
                        return new NotfoundException(buffer.toString(), failure);
                    case MALFORMED_PATH:
                    case RESTRICTED_CONTENT:
                        return new AccessDeniedException(buffer.toString(), failure);
                }
            }
        }
        if(failure instanceof UploadErrorException) {
            final UploadError error = ((UploadErrorException) failure).errorValue;
            if(error.isPath()) {
                final UploadWriteFailed lookup = error.getPathValue();
                this.parse(buffer, lookup.toString());
                switch(lookup.getReason().tag()) {
                    case CONFLICT:
                        return new ConflictException(buffer.toString(), failure);
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
        }
        if(failure instanceof UploadSessionFinishErrorException) {
            final UploadSessionFinishError error = ((UploadSessionFinishErrorException) failure).errorValue;
            if(error.isPath()) {
                final WriteError lookup = error.getPathValue();
                this.parse(buffer, lookup.toString());
                switch(lookup.tag()) {
                    case OTHER:
                        return new InteroperabilityException(buffer.toString(), failure);
                    case CONFLICT:
                        return new ConflictException(buffer.toString(), failure);
                    case MALFORMED_PATH:
                    case DISALLOWED_NAME:
                    case NO_WRITE_PERMISSION:
                        return new AccessDeniedException(buffer.toString(), failure);
                    case INSUFFICIENT_SPACE:
                        return new QuotaException(buffer.toString(), failure);
                }
            }
        }
        if(failure instanceof GetTemporaryLinkErrorException) {
            final GetTemporaryLinkError error = ((GetTemporaryLinkErrorException) failure).errorValue;
            if(error.isPath()) {
                final LookupError lookup = error.getPathValue();
                this.parse(buffer, lookup.toString());
                switch(lookup.tag()) {
                    case LOCKED:
                        return new LockedException(buffer.toString(), failure);
                    case NOT_FOUND:
                    case NOT_FILE:
                    case NOT_FOLDER:
                        return new NotfoundException(buffer.toString(), failure);
                    case MALFORMED_PATH:
                    case RESTRICTED_CONTENT:
                        return new AccessDeniedException(buffer.toString(), failure);
                    case OTHER:
                        return new InteroperabilityException(buffer.toString(), failure);
                }
            }
        }
        if(failure instanceof ListFolderContinueErrorException) {
            final ListFolderContinueError error = ((ListFolderContinueErrorException) failure).errorValue;
            if(error.isPath()) {
                final LookupError lookup = error.getPathValue();
                this.parse(buffer, lookup.toString());
                switch(lookup.tag()) {
                    case LOCKED:
                        return new LockedException(buffer.toString(), failure);
                    case NOT_FOUND:
                    case NOT_FILE:
                    case NOT_FOLDER:
                        return new NotfoundException(buffer.toString(), failure);
                    case MALFORMED_PATH:
                    case RESTRICTED_CONTENT:
                        return new AccessDeniedException(buffer.toString(), failure);
                    case OTHER:
                        return new InteroperabilityException(buffer.toString(), failure);
                }
            }
        }
        if(failure instanceof AccessErrorException) {
            final AccessError error = ((AccessErrorException) failure).getAccessError();
            this.parse(buffer, error.toString());
            // File locking is not supported for user
            return new UnsupportedException(buffer.toString(), failure);
        }
        if(failure instanceof CreateSharedLinkWithSettingsErrorException) {
            final CreateSharedLinkWithSettingsError error = ((CreateSharedLinkWithSettingsErrorException) failure).errorValue;
            switch(error.tag()) {
                case SHARED_LINK_ALREADY_EXISTS:
                    return new ConflictException(buffer.toString(), failure);
                case ACCESS_DENIED:
                    return new AccessDeniedException(buffer.toString(), failure);
            }
        }
        if(failure instanceof RelocationErrorException) {
            final RelocationError error = ((RelocationErrorException) failure).errorValue;
            switch(error.tag()) {
                case TOO_MANY_FILES:
                case INSUFFICIENT_QUOTA:
                    return new QuotaException(buffer.toString(), failure);
            }
            if(error.isTo()) {
                switch(error.getToValue().tag()) {
                    case MALFORMED_PATH:
                    case DISALLOWED_NAME:
                    case NO_WRITE_PERMISSION:
                        return new AccessDeniedException(buffer.toString(), failure);
                    case TOO_MANY_WRITE_OPERATIONS:
                        return new RetriableAccessDeniedException(buffer.toString(), failure);
                    case CONFLICT:
                        return new ConflictException(buffer.toString(), failure);
                    case INSUFFICIENT_SPACE:
                        return new QuotaException(buffer.toString(), failure);
                }
            }
            if(error.isFromLookup()) {
                switch(error.getFromLookupValue().tag()) {
                    case LOCKED:
                        return new LockedException(buffer.toString(), failure);
                    case NOT_FOUND:
                    case NOT_FILE:
                    case NOT_FOLDER:
                        return new NotfoundException(buffer.toString(), failure);
                    case MALFORMED_PATH:
                    case RESTRICTED_CONTENT:
                        return new AccessDeniedException(buffer.toString(), failure);
                }
            }
        }
        return new InteroperabilityException(buffer.toString(), failure);
    }

    private void parse(final StringBuilder buffer, final String message) {
        if(StringUtils.isBlank(message)) {
            return;
        }
        try {
            final JsonElement element = JsonParser.parseReader(new StringReader(message));
            if(element.isJsonObject()) {
                final JsonObject json = element.getAsJsonObject();
                final JsonObject error = json.getAsJsonObject("error");
                if(null == error) {
                    this.append(buffer, message);
                }
                else {
                    final JsonPrimitive tag = error.getAsJsonPrimitive(".tag");
                    if(null == tag) {
                        this.append(buffer, message);
                    }
                    else {
                        this.append(buffer, StringUtils.replace(tag.getAsString(), "_", " "));
                    }
                }
            }
            if(element.isJsonPrimitive()) {
                this.append(buffer, element.getAsString());
            }
        }
        catch(JsonParseException e) {
            // Ignore
        }
    }
}
