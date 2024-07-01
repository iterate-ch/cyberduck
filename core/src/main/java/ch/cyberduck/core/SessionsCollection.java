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

import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;

public class SessionsCollection extends AbstractFolderHostCollection {

    private static final SessionsCollection SESSIONS_COLLECTION = new SessionsCollection(
            LocalFactory.get(SupportDirectoryFinderFactory.get().find(), "Sessions")
    );

    public SessionsCollection(final Local f) {
        super(f);
    }

    /**
     * @return Singleton instance
     */
    public static SessionsCollection defaultCollection() {
        return SESSIONS_COLLECTION;
    }

    /**
     * Does not allow manual additions
     *
     * @return False
     */
    @Override
    public boolean allowsAdd() {
        return false;
    }

    /**
     * Does not allow editing entries
     *
     * @return False
     */
    @Override
    public boolean allowsEdit() {
        return false;
    }
}
