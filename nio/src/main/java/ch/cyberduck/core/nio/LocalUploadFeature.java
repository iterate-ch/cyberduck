package ch.cyberduck.core.nio;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;

public class LocalUploadFeature extends DefaultUploadFeature<Void> {

    public LocalUploadFeature(final LocalSession session) {
        super(new LocalWriteFeature(session));
    }

    public LocalUploadFeature(final Write<Void> writer) {
        super(writer);
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Write.Append(status.isExists()).withStatus(status);
    }
}
