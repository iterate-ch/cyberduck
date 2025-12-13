package ch.cyberduck.core.transfer.upload.features;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Optional;

public class MimeFeatureFilter implements FeatureFilter {

    private final MimeTypeService service;

    public MimeFeatureFilter() {
        this(new MappingMimeTypeService());
    }

    public MimeFeatureFilter(final MimeTypeService service) {
        this.service = service;
    }

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(file.isFile()) {
            status.setMime(service.getMime(file.getName()));
        }
        return status;
    }
}
