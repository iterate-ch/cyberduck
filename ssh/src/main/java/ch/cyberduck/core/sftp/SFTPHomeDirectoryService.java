package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import java.io.IOException;
import java.util.EnumSet;

public class SFTPHomeDirectoryService extends DefaultHomeFinderService {

    private final SFTPSession session;

    public SFTPHomeDirectoryService(final SFTPSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        final Path home = super.find();
        if(home == DEFAULT_HOME) {
            try {
                // "." as referring to the current directory
                final String directory = session.sftp().canonicalize(".");
                return new Path(directory,
                        directory.equals(String.valueOf(Path.DELIMITER)) ?
                                EnumSet.of(Path.Type.volume, Path.Type.directory) : EnumSet.of(Path.Type.directory));
            }
            catch(IOException e) {
                throw new SFTPExceptionMappingService().map(e);
            }
        }
        return home;
    }
}
