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
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;


import java.io.OutputStream;

public class MantaWriteFeature implements Write<Void> {

    private final Preferences preferences
            = PreferencesFactory.get();

    private final MantaSession session;
    private final MantaAttributesFinderFeature attrFinder;

    public MantaWriteFeature(final MantaSession session) {
        this.session = session;
        this.attrFinder = new MantaAttributesFinderFeature(session);
    }

    /**
     * Return an output stream that writes to Manta. {@code putAsOutputStream} requires a thread per call and as
     * a result is discouraged in the java-manta client documentation.
     *
     * {@inheritDoc}
     */
    @Override
    public HttpResponseOutputStream<Void> write(final Path file,
                                          final TransferStatus status,
                                          final ConnectionCallback callback) throws BackgroundException {
        final OutputStream putStream = session.getClient().putAsOutputStream(session.pathMapper.requestPath(file));

        return new HttpResponseOutputStream<Void>(putStream) {
            @Override
            public Void getStatus() throws BackgroundException {
                return null;
            }
        };
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        PathAttributes attributes;
        try {
            attributes = attrFinder.withCache(cache).find(file);
        } catch (Exception e) {
            return Write.notfound;
        }

        // TODO: figure out what override does here and if we're even doing anything that makes sense really
        return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    // TODO: what does this checksum do? can we combine it with our MD5 checksum?
    @Override
    public ChecksumCompute checksum() {
        // TODO: verify this is actually used
        return new MD5ChecksumCompute();
    }
}
