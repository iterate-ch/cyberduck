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
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.EnumSet;

/**
 * Server side copying of files
 */
@Optional
public interface Copy {
    /**
     * @param source   Source file or folder
     * @param target   Target file or folder
     * @param status   Write status
     * @param prompt   Prompt
     * @param listener Progress callback
     * @return Target file
     */
    Path copy(Path source, Path target, TransferStatus status, ConnectionCallback prompt, final StreamListener listener) throws BackgroundException;

    /**
     * @param source Source file or folder
     * @param target Target file or folder
     * @return True if the implementation can copy directories recursively
     */
    default boolean isRecursive(final Path source, final Path target) {
        return this.features(source, target).contains(Flags.recursive);
    }

    /**
     * @param source Source file or folder
     * @param folder Target folder
     * @param filename Target filename
     * @return False if not supported for given files
     */
    default boolean isSupported(final Path source, final Path folder, final String filename) {
        try {
            this.preflight(source, folder, filename);
            return true;
        }
        catch(BackgroundException e) {
            return false;
        }
    }

    /**
     * @param session Target session for stateful protocols
     * @return This
     */
    default Copy withTarget(final Session<?> session) {
        return this;
    }

    /**
     * @param source    Existing file or folder
     * @param directory Target directory
     * @param filename  Target filename
     * @throws AccessDeniedException    Permission failure to create target directory
     * @throws UnsupportedException     Copy operation not supported for source
     * @throws InvalidFilenameException Target filename not supported
     */
    default void preflight(final Path source, final Path directory, final String filename) throws BackgroundException {
        if(!directory.getParent().attributes().getPermission().isWritable()) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"),
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
         * Support copying directories recursively
         */
        recursive
    }
}
