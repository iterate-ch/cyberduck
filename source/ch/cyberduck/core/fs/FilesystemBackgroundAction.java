package ch.cyberduck.core.fs;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.AbstractBackgroundAction;

/**
* @version $Id:$
*/
public abstract class FilesystemBackgroundAction<T> extends AbstractBackgroundAction<T> {
    private Object lock;

    public FilesystemBackgroundAction(Object lock) {
        this.lock = lock;
    }

    @Override
    public Object lock() {
        return lock;
    }

    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getActivity() {
        return Locale.localizedString("Filesystem");
    }
}
