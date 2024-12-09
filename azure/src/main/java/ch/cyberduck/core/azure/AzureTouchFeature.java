package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

import com.microsoft.azure.storage.OperationContext;

public class AzureTouchFeature extends DefaultTouchFeature<Void> {

    public AzureTouchFeature(final AzureSession session, final OperationContext context) {
        super(new AzureWriteFeature(session, context));
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
        }
        if(!validate(filename)) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename));
        }
    }

    public static boolean validate(final String filename) {
        // Empty argument if not known in validation
        if(StringUtils.isNotBlank(filename)) {
            // Container names must be lowercase, between 3-63 characters long and must start with a letter or
            // number. Container names may contain only letters, numbers, and the dash (-) character.
            if(StringUtils.length(filename) > 63) {
                return false;
            }
            if(StringUtils.length(filename) < 3) {
                return false;
            }
            if(!StringUtils.isAlphanumeric(RegExUtils.removeAll(filename, "-"))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        status.setChecksum(write.checksum(file, status).compute(new NullInputStream(0L), status));
        return super.touch(file, status);
    }
}
