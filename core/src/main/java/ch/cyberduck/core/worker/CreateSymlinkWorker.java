package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Symlink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class CreateSymlinkWorker extends Worker<Path> {
    private static final Logger log = LogManager.getLogger(CreateSymlinkWorker.class);

    private final Path link;
    private final String target;

    public CreateSymlinkWorker(final Path link, final String target) {
        this.link = link;
        this.target = target;
    }

    @Override
    public Path run(final Session<?> session) throws BackgroundException {
        // Symlink pointing to existing target file
        final Symlink feature = session.getFeature(Symlink.class);
        if(log.isDebugEnabled()) {
            log.debug("Run with feature {}", feature);
        }
        feature.symlink(link, target);
        return link;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Uploading {0}", "Status"), link.getName());
    }
}
