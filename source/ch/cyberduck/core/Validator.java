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

import java.util.Collection;

/**
 * @version $Id$
 */
public interface Validator {

    /**
     * Overwrite any prior existing file
     */
    public static final String OVERWRITE = "overwrite";
    /**
     * Append to any exsisting file when writing
     */
    public static final String RESUME = "resume";
    /**
     * Create a new file with a similar name
     *
     * @see ch.cyberduck.core.DownloadTransfer#adjustFilename(Path)
     * @see ch.cyberduck.core.UploadTransfer#adjustFilename(Path)
     */
    public static final String SIMILAR = "similar";
    /**
     * Prompt the user about existing files
     */
    public static final String ASK = "ask";

    /**
     * Adds the path for possible inclusion in the result set
     *
     * @param p
     */
    public abstract void prompt(Path p);

    /**
     * Blocks the caller until the user confirmed the selection
     * @return The result set selected by the user
     */
    public abstract Collection result();

    /**
     * @return True if the validation has been canceled by the user
     */
    public abstract boolean isCanceled();
}
