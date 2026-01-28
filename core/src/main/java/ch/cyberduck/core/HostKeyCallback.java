package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.security.PublicKey;

public interface HostKeyCallback {

    /**
     * Verifies the given public key for the specified host.
     *
     * @param hostname the host for which the key verification is to be performed
     * @param key      the public key to be verified
     * @return true if the key is verified successfully, false otherwise
     * @throws ConnectionCanceledException Verification canceled by user
     * @throws BackgroundException         Unknown verification failure
     */
    boolean verify(Host hostname, PublicKey key) throws BackgroundException;
}
