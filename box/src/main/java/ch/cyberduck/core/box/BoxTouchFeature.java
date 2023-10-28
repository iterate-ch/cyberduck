package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.shared.DefaultTouchFeature;

import org.apache.commons.lang3.StringUtils;

public class BoxTouchFeature extends DefaultTouchFeature<File> {

    public BoxTouchFeature(final BoxSession session, final BoxFileidProvider fileid) {
        super(new BoxWriteFeature(session, fileid));
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(!validate(filename)) {
            throw new InvalidFilenameException();
        }
    }

    public static boolean validate(final String filename) {
        // Max Length 255
        if(StringUtils.length(filename) > 255) {
            return false;
        }
        if(StringUtils.contains(filename, "/")) {
            return false;
        }
        if(StringUtils.contains(filename, "\\")) {
            return false;
        }
        if(StringUtils.endsWith(filename, StringUtils.SPACE)) {
            return false;
        }
        // Additionally, the names . and .. are not allowed either.
        if(StringUtils.equals(filename, ".")) {
            return false;
        }
        if(StringUtils.equals(filename, "..")) {
            return false;
        }
        return true;
    }
}
