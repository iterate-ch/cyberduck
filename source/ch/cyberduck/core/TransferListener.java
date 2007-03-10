package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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
public interface TransferListener {

    /**
     * The transfers are about to start transfering
     */
    public abstract void transferWillStart();

    /**
     * The transfer is paused and waits for other transfers to finish first
     */
    public abstract void transferPaused();

    /**
     * The transfer has a slot in the queue allocated
     */
    public abstract void transferResumed();

    /**
     * All transfers did end
     */
    public abstract void transferDidEnd();

    /**
     *
     * @param path
     */
    public abstract void willTransferPath(Path path);

    /**
     *
     * @param path
     */
    public abstract void didTransferPath(Path path);
}
