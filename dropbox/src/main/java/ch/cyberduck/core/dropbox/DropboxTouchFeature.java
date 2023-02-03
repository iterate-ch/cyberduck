package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultTouchFeature;

import org.apache.commons.lang3.StringUtils;

import com.dropbox.core.v2.files.Metadata;

public class DropboxTouchFeature extends DefaultTouchFeature<Metadata> {

    public DropboxTouchFeature(final DropboxSession session) {
        super(new DropboxWriteFeature(session));
    }

    /**
     * Name begins with ~$ (a tilde and dollar sign) or .~ (a period and tilde) Name begins with a tilde and ends in
     * .tmp, such as ~myfile.tmp
     *
     * @param workdir  Working directory
     * @param filename Filename
     * @return False if restricted filename
     */
    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        if(StringUtils.startsWith(filename, "~$")) {
            return false;
        }
        if(StringUtils.startsWith(filename, "~") && StringUtils.endsWith(filename, ".tmp")) {
            return false;
        }
        return true;
    }
}
