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
import ch.cyberduck.core.local.features.Touch;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class DefaultLocalTouchFeature implements Touch {
    private static final Logger log = Logger.getLogger(DefaultLocalTouchFeature.class);

    @Override
    public void touch(final Local l) throws AccessDeniedException {
        final File file = new File(l.getAbsolute());
        final File parent = file.getParentFile();
        if(!parent.exists()) {
            if(!parent.mkdirs()) {
                throw new AccessDeniedException(MessageFormat.format(
                        LocaleFactory.localizedString("Cannot create folder {0}", "Error"), l.getAbsolute()));
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Created folder %s", parent));
            }
        }
        if(l.exists()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Skip creating file %s", l));
            }
            return;
        }
        try {
            if(!file.createNewFile()) {
                throw new AccessDeniedException(MessageFormat.format(
                        LocaleFactory.localizedString("Cannot create {0}", "Error"), l.getAbsolute()));
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Created file %s", file));
            }
        }
        catch(IOException e) {
            throw new AccessDeniedException(MessageFormat.format(
                    LocaleFactory.localizedString("Cannot create {0}", "Error"), l.getAbsolute()), e);
        }
    }
}
