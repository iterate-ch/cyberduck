package ch.cyberduck.core.features;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;

/**
 * Create new folder on server
 *
 * @param <Reply> Metadata
 */
@Required
public interface Directory<Reply> {

    /**
     * Create new folder on server
     *
     * @param folder Directory
     * @param status Transfer status
     */
    Path mkdir(Path folder, TransferStatus status) throws BackgroundException;

    /**
     * @param workdir Working directory in browser
     * @param filename    Folder name or null if unknown
     * @return True if creating directory is supported in the working directory
     */
    default boolean isSupported(final Path workdir, final String filename) {
        try {
            this.preflight(workdir, filename);
            return true;
        }
        catch(BackgroundException e) {
            return false;
        }
    }

    /**
     * Retrieve write implementation for implementations using placeholder files for folders
     */
    Directory<Reply> withWriter(Write<Reply> writer);

    /**
     * @throws AccessDeniedException Permission failure for target directory
     */
    default void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(!workdir.attributes().getPermission().isWritable()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), filename));
        }
    }
}
