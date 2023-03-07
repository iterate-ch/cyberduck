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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Required
public interface Delete {
    default void delete(List<Path> files, PasswordCallback prompt, Callback callback) throws BackgroundException {
        final Map<Path, TransferStatus> set = new LinkedHashMap<>();
        for(Path file : files) {
            set.put(file, new TransferStatus());
        }
        this.delete(set, prompt, callback);
    }

    void delete(Map<Path, TransferStatus> files, PasswordCallback prompt, Callback callback) throws BackgroundException;

    default boolean isSupported(final Path file) {
        return file.attributes().getPermission().isWritable();
    }

    default boolean isRecursive() {
        return false;
    }

    interface Callback {
        void delete(Path file);
    }

    class DisabledCallback implements Callback {
        @Override
        public void delete(Path file) {
            //
        }
    }
}
