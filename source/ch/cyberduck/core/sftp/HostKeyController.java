package ch.cyberduck.core.sftp;


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

import ch.cyberduck.core.ConnectionCanceledException;
import ch.ethz.ssh2.ServerHostKeyVerifier;

/**
 * @version $Id:$
 */
public abstract class HostKeyController implements ServerHostKeyVerifier {

    /**
     * @return True if accepted.
     */
    protected abstract boolean isUnknownKeyAccepted(final String hostname, final int port, final String serverHostKeyAlgorithm,
                                 final byte[] serverHostKey) throws ConnectionCanceledException;

    protected abstract boolean isChangedKeyAccepted(final String hostname, final int port, final String serverHostKeyAlgorithm,
                                 final byte[] serverHostKey) throws ConnectionCanceledException;
}
