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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.EnumSet;

/**
 * Move or rename file or folder on server
 */
@Required
public interface Move {
    /**
     * @param source Source file or folder
     * @param target Target file or folder
     * @param status True if the target file exists
     * @param delete Progress callback
     * @param prompt Prompt
     * @return Target file
     */
    Path move(Path source, Path target, TransferStatus status, Delete.Callback delete, ConnectionCallback prompt) throws BackgroundException;

    /**
     * @param source Source file or folder
     * @param target Target file or folder
     * @return True if the implementation can move directories recursively
     */
    default boolean isRecursive(final Path source, final Path target) {
        return this.features(source, target).contains(Flags.recursive);
    }

    /**
     * @param source    Source file or folder
     * @param directory Target directory
     * @param filename  Target filename
     * @return False if not supported for given files
     */
    default boolean isSupported(final Path source, final Path directory, final String filename) {
        try {
            this.preflight(source, directory, filename);
            return true;
        }
        catch(BackgroundException e) {
            return false;
        }
    }

    /**
     * @param session Target session for stateful protocols when move can only be made with copy/delete
     * @return This
     */
    default Move withTarget(Session<?> session) {
        return this;
    }

    /**
     * @param source    Existing file or folder
     * @param directory Target directory
     * @param filename  Target filename
     * @throws AccessDeniedException    Permission failure for target parent directory
     * @throws UnsupportedException     Move operation not supported for source
     * @throws InvalidFilenameException Target filename not supported
     */
    default void preflight(final Path source, final Path directory, final String filename) throws BackgroundException {
        if(!directory.getParent().attributes().getPermission().isWritable()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"),
                    source.getName())).withFile(source);
        }
    }


    /**
     * @return Supported features
     */
    default EnumSet<Flags> features(Path source, Path target) {
        return EnumSet.noneOf(Flags.class);
    }

    /**
     * Feature flags
     */
    enum Flags {
        /**
         * Support moving directories recursively
         */
        recursive
    }
}
