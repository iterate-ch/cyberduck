package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Search;

import org.jets3t.service.ServiceException;

import java.io.IOException;

public class S3SearchFeature implements Search {

    private final S3Session session;

    public S3SearchFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> search(final Path workdir, final Filter<Path> regex, final ListProgressListener listener) throws BackgroundException {
        final S3ObjectListService list = new S3ObjectListService(session);
        try {
            return list.listObjects(workdir, String.format("%s%s", list.createPrefix(workdir), regex.toPattern().pattern()), listener);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, workdir);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, workdir);
        }
    }

    @Override
    public boolean isRecursive() {
        return false;
    }

    @Override
    public Search withCache(final Cache<Path> cache) {
        return this;
    }
}
