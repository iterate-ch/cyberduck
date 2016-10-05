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
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DeleteError;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.GetMetadataError;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.LookupError;

public class DropboxExceptionMappingService extends AbstractExceptionMappingService<DbxException> {

    @Override
    public BackgroundException map(final DbxException failure) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getLocalizedMessage());
        if(failure instanceof GetMetadataErrorException) {
            final GetMetadataError error = ((GetMetadataErrorException) failure).errorValue;
            final LookupError lookup = error.getPathValue();
            switch(lookup.tag()) {
                case MALFORMED_PATH:
                    return new InteroperabilityException(buffer.toString(), failure);
                case NOT_FOUND:
                    return new NotfoundException(buffer.toString(), failure);
                case NOT_FILE:
                    return new NotfoundException(buffer.toString(), failure);
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
                    return new InteroperabilityException(buffer.toString(), failure);
                case NOT_FOUND:
                    return new NotfoundException(buffer.toString(), failure);
                case NOT_FILE:
                    return new NotfoundException(buffer.toString(), failure);
                case NOT_FOLDER:
                    return new NotfoundException(buffer.toString(), failure);
                case RESTRICTED_CONTENT:
                    return new AccessDeniedException(buffer.toString(), failure);
            }
        }
        return new BackgroundException(buffer.toString(), failure);
    }
}
