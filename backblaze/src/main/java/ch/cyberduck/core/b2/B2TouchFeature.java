package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;

import java.io.IOException;
import java.util.Collections;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2TouchFeature implements Touch {

    private final B2Session session;

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final Write write;

    public B2TouchFeature(final B2Session session) {
        this(session, session.getFeature(Write.class));
    }

    public B2TouchFeature(final B2Session session, final Write write) {
        this.session = session;
        this.write = write;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        try {
            final TransferStatus status = new TransferStatus();
            status.setChecksum(session.getFeature(ChecksumCompute.class, ChecksumComputeFactory.get(HashAlgorithm.sha1))
                    .compute(new NullInputStream(0L), status)
            );
            status.setMime(new MappingMimeTypeService().getMime(file.getName()));
            status.setMetadata(Collections.singletonMap(
                    X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, String.valueOf(System.currentTimeMillis())
                    )
            );
            status.setLength(session.getFeature(Vault.class).toCiphertextSize(0L));
            write.write(file, status).close();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }
}
