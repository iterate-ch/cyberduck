package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Touch;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;

public class DefaultLocalTouchFeature implements Touch {
    private static final Logger log = Logger.getLogger(DefaultLocalTouchFeature.class);

    @Override
    public void touch(final Local file) throws AccessDeniedException {
        final Local parent = file.getParent();
        if(!parent.exists()) {
            parent.mkdir();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Created folder %s", parent));
            }
        }
        if(file.exists()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Skip creating file %s", file));
            }
            return;
        }
        try {
            Files.createFile(Paths.get(file.getAbsolute()));
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(MessageFormat.format(
                    LocaleFactory.localizedString("Cannot create {0}", "Error"), file.getAbsolute()), e);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Created file %s", file));
        }
    }
}
