package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.preferences.ApplicationResourcesFinder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URI;

public class ClasspathResourcesFinder implements ApplicationResourcesFinder {

    private static final Logger log = LogManager.getLogger(ClasspathResourcesFinder.class);

    @Override
    public Local find() {
        final String current = new File(URI.create(ClasspathResourcesFinder.class.getProtectionDomain().getCodeSource().getLocation().toString())).getPath();
        final Local parent = LocalFactory.get(current).getParent();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Use folder %s for application resources directory", parent));
        }
        return parent;
    }
}
