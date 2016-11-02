package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.TransferAcceleration;

import java.util.List;

public class WriteTransferAccelerationWorker extends Worker<Boolean> {

    /**
     * Selected files.
     */
    private final List<Path> files;

    private final boolean enabled;

    public WriteTransferAccelerationWorker(final List<Path> files, final boolean enabled) {
        this.files = files;
        this.enabled = enabled;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final TransferAcceleration feature = session.getFeature(TransferAcceleration.class);
        for(Path file : files) {
            this.write(feature, file);
        }
        return true;
    }

    private void write(final TransferAcceleration feature, final Path file) throws BackgroundException {
        feature.setStatus(file, enabled);
    }
}
