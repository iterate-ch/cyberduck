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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;

import java.util.EnumSet;

@Optional
public interface Versioning {

    /**
     * Read configuration
     *
     * @param container Bucket
     * @return Current configuration
     * @throws BackgroundException API or network failure
     */
    VersioningConfiguration getConfiguration(Path container) throws BackgroundException;

    /**
     * Write configuration
     *
     * @param container     Bucket
     * @param prompt        Prompt for MFA token
     * @param configuration New configuration
     * @throws BackgroundException API or network failure
     */
    void setConfiguration(Path container, PasswordCallback prompt, VersioningConfiguration configuration) throws BackgroundException;

    /**
     * Save new version
     *
     * @param file File or folder
     * @return True if version is saved
     */
    default boolean save(Path file) throws BackgroundException {
        return false;
    }

    /**
     * Restore this version
     *
     * @param file File
     * @throws BackgroundException API or network failure
     */
    void revert(Path file) throws BackgroundException;

    /**
     * Determine if version can be restored
     *
     * @param file File
     * @return True if this file version can be reverted
     */
    default boolean isRevertable(final Path file) {
        return this.features(file).contains(Flags.revert);
    }

    /**
     * Find all versions for path. Should not include latest version but only previous.
     *
     * @param file     File on server
     * @param listener Progress notification callback
     * @return List of versions or singleton list if no other versions found on server. List must be sorted with the newest version first
     * @throws BackgroundException Failure reading versions from server
     */
    AttributedList<Path> list(Path file, ListProgressListener listener) throws BackgroundException;

    default void cleanup(Path file, ConnectionCallback callback) throws BackgroundException {
        //
    }

    /**
     * @return Supported features
     */
    default EnumSet<Flags> features(Path file) {
        if(file.attributes().isDuplicate()) {
            return EnumSet.of(Flags.revert);
        }
        return EnumSet.noneOf(Flags.class);
    }

    /**
     * Feature flags
     */
    enum Flags {
        /**
         * Support reverting to previous version of file
         */
        revert,
        /**
         * Archive file version
         */
        save,
        /**
         * Toggle configuration
         */
        configuration,
        /**
         *
         */
        list
    }
}
