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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Delete files on server
 */
@Required
public interface Delete {
    Logger log = LogManager.getLogger(Delete.class);

    default void delete(List<Path> files, PasswordCallback prompt, Callback callback) throws BackgroundException {
        final Map<Path, TransferStatus> set = new LinkedHashMap<>();
        for(Path file : files) {
            set.put(file, new TransferStatus());
        }
        this.delete(set, prompt, callback);
    }

    /**
     * Delete files on server
     *
     * @param files    Selected files or folders
     * @param prompt   Callback when password is required to delete file
     * @param callback Progress callback
     */
    void delete(Map<Path, TransferStatus> files, PasswordCallback prompt, Callback callback) throws BackgroundException;

    /**
     * @param file File or folder
     * @return True if the file can be deleted on the server
     */
    default boolean isSupported(final Path file) {
        try {
            this.preflight(file);
            return true;
        }
        catch(BackgroundException e) {
            return false;
        }
    }

    /**
     * @return True if the implementation supports deleting folders recursively
     */
    default boolean isRecursive() {
        return false;
    }

    /**
     * Callback for every file deleted
     */
    interface Callback {
        void delete(Path file);
    }

    class DisabledCallback implements Callback {
        @Override
        public void delete(Path file) {
            //
        }
    }

    default void preflight(final Path file) throws BackgroundException {
        if(!file.attributes().getPermission().isWritable()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"),
                    file.getName())).withFile(file);
        }
    }
}
