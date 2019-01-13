package ch.cyberduck.core.manta;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.OutputStream;

public class MantaWriteFeature implements Write<Void> {

    private final MantaSession session;
    private final Find finder;
    private final AttributesFinder attributes;

    public MantaWriteFeature(final MantaSession session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public MantaWriteFeature(final MantaSession session, final Find finder, final AttributesFinder attributes) {
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
    }

    /**
     * Return an output stream that writes to Manta. {@code putAsOutputStream} requires a thread per call and as
     * a result is discouraged in the java-manta client documentation.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final OutputStream putStream = session.getClient().putAsOutputStream(file.getAbsolute());
        return new HttpResponseOutputStream<Void>(putStream) {
            @Override
            public Void getStatus() throws BackgroundException {
                return null;
            }
        };
    }

    /**
     * Manta does not support raw append operations.
     */
    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attr = attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attr.getSize()).withChecksum(attr.getChecksum());
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }
}
