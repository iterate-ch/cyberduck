package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVWriteFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

public class BrickWriteFeature extends DAVWriteFeature {

    private final DAVSession session;

    public BrickWriteFeature(final DAVSession session) {
        super(session);
        this.session = session;
    }

    public BrickWriteFeature(final DAVSession session, final boolean expect) {
        super(session, expect);
        this.session = session;
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return Write.override;
    }
}
