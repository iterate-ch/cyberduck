package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.security.PublicKey;

public abstract class AbstractHostKeyCallback implements HostKeyCallback {

    /**
     * @param hostname Hostname
     * @return True if accepted.
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException Canceled by user
     */
    protected abstract boolean isUnknownKeyAccepted(String hostname, PublicKey key)
            throws ConnectionCanceledException, ChecksumException;

    /**
     * @param hostname Hostname
     * @return True if accepted.
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException Canceled by user
     */
    protected abstract boolean isChangedKeyAccepted(String hostname, PublicKey key)
            throws ConnectionCanceledException, ChecksumException;

    protected abstract void allow(String hostname, PublicKey key, boolean persist);
}
