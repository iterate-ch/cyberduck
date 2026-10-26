package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;

import java.util.EnumSet;


public class AzureBlobDirectoryFeature implements Directory<Void> {

    private final AzureSession session;

    public AzureBlobDirectoryFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Write<Void> writer, final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
            type.add(Path.Type.placeholder);
            return new AzureTouchFeature(session).touch(writer, folder.withType(type),
                    status.setChecksum(writer.checksum(folder, status).compute(new NullInputStream(0L), status)));
        }
        catch(IllegalArgumentException e) {
            throw new InteroperabilityException();
        }
        catch(RuntimeException e) {
            throw new AzureExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }
}
