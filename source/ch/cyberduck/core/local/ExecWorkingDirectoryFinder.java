package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id$
 */
public class ExecWorkingDirectoryFinder implements WorkingDirectoryFinder {
    private static final Logger log = Logger.getLogger(ExecWorkingDirectoryFinder.class);

    private final Runtime runtime = Runtime.getRuntime();

    @Override
    public Local find() {
        final Process print;
        try {
            print = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "pwd"});
            return LocalFactory.get(StringUtils.strip(IOUtils.toString(print.getInputStream())));
        }
        catch(IOException e) {
            log.warn(String.format("Failure running `pwd`. %s", e.getMessage()));
            return new DefaultWorkingDirectoryFinder().find();
        }
    }
}
