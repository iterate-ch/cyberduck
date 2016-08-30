package ch.cyberduck.core.local;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.library.Native;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public final class LaunchServicesFileDescriptor extends AbstractFileDescriptor {

    static {
        Native.load("core");
    }

    @Override
    public String getKind(final String filename) {
        if(StringUtils.isBlank(FilenameUtils.getExtension(filename))) {
            final String kind = this.kind(filename);
            if(StringUtils.isBlank(kind)) {
                return LocaleFactory.localizedString("Unknown");
            }
            return kind;
        }
        final String kind = this.kind(FilenameUtils.getExtension(filename));
        if(StringUtils.isBlank(kind)) {
            return LocaleFactory.localizedString("Unknown");
        }
        return kind;
    }

    private native String kind(String extension);
}
