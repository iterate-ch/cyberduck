package ch.cyberduck.core.azure;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;

import java.text.MessageFormat;

public class AzureTouchFeature extends DefaultTouchFeature<Void> {

    private final AzureSession session;

    public AzureTouchFeature(final AzureSession session) {
        super(new AzureWriteFeature(session));
        this.session = session;
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
        }
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        status.setChecksum(write.checksum(file, status).compute(new NullInputStream(0L), status));
        return super.touch(file, status).withAttributes(new AzureAttributesFinderFeature(session).find(file));
    }
}
