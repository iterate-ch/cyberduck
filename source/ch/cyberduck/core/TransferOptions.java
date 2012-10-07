package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

/**
 * @version $Id$
 */
public final class TransferOptions {

    /**
     * Resume requested using user interface
     */
    public boolean resumeRequested = false;

    /**
     * Reload requested using user interface
     */
    public boolean reloadRequested = false;

    /**
     * Close session after transfer
     */
    public boolean closeSession = true;

    /**
     * Add quarantine flag to downloaded file
     */
    public boolean quarantine =
            Preferences.instance().getBoolean("queue.download.quarantine");
}
