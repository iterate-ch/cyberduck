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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

@Optional
public interface Copy {
    /**
     * @param source   Source file or folder
     * @param target   Target file or folder
     * @param status   Write status
     * @param callback Prompt
     * @param listener
     * @return Target file
     */
    Path copy(Path source, Path target, TransferStatus status, ConnectionCallback callback, final StreamListener listener) throws BackgroundException;

    /**
     * @param source Source file or folder
     * @param target Target file or folder
     * @return True if the implementation can copy directories recursively
     */
    default boolean isRecursive(Path source, Path target) {
        return false;
    }

    /**
     * @param source Source file or folder
     * @param target Target file or folder
     * @return False if not supported for given files
     */
    default boolean isSupported(Path source, Path target) {
        return true;
    }

    /**
     * @param session Target session for stateful protocols
     * @return This
     */
    default Copy withTarget(Session<?> session) {
        return this;
    }
}
