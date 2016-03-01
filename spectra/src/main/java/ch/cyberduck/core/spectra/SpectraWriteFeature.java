/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DefaultAttributesFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.model.StorageObject;

import java.util.HashMap;
import java.util.Map;

public class SpectraWriteFeature extends S3WriteFeature {
    private static final Logger log = Logger.getLogger(SpectraWriteFeature.class);

    private final Find finder;

    private final Attributes attributes;

    public SpectraWriteFeature(final SpectraSession session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFeature(session));
    }

    public SpectraWriteFeature(final SpectraSession session, final Find finder, final Attributes attributes) {
        super(session);
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public ResponseOutputStream<StorageObject> write(final Path file, final TransferStatus status) throws BackgroundException {
        // This is an Amazon S3 compatible operation with additional request parameters. The job and offset parameters should always be used
        // when doing a PUT object as part of a bulk PUT job
        final Map<String, String> parameters = new HashMap<>(status.getParameters());
        // Job parameter already present from bulk service
        if(status.isAppend()) {
            parameters.put("offset", Long.toString(status.getOffset()));
            status.parameters(parameters);
        }
        else {
            parameters.put("offset", Long.toString(status.getOffset()));
            status.parameters(parameters);
        }
        return super.write(file, status);
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
        return Write.notfound;
    }
}
