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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.OutputStream;

public class MantaWriteFeature implements Write<Void> {

    private final MantaSession session;

    public MantaWriteFeature(final MantaSession session) {
        this.session = session;
    }

    /**
     * Return an output stream that writes to Manta. {@code putAsOutputStream} requires a thread per call and as a
     * result is discouraged in the java-manta client documentation.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) {
        final OutputStream putStream = session.getClient().putAsOutputStream(file.getAbsolute());
        return new VoidStatusOutputStream(putStream);
    }
}
