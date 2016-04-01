package ch.cyberduck.core.dropbox;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;

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
public class DropboxTouchFeature implements Touch {


    public DropboxTouchFeature(final DropboxSession dropboxSession) {
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return false;
    }
}
