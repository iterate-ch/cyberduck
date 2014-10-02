package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.github.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVDeleteFeature implements Delete {

    private DAVSession session;

    public DAVDeleteFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final ProgressListener listener) throws BackgroundException {
        final List<Path> deleted = new ArrayList<Path>();
        for(Path file : files) {
            boolean skip = false;
            for(Path d : deleted) {
                if(file.isChild(d)) {
                    skip = true;
                    break;
                }
            }
            if(skip) {
                continue;
            }
            listener.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                    file.getName()));
            try {
                session.getClient().delete(new DAVPathEncoder().encode(file));
            }
            catch(SardineException e) {
                throw new DAVExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, file);
            }
            deleted.add(file);
        }
    }
}
