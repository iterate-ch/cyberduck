package ch.cyberduck.core.sftp;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Compress;

import java.util.List;

/**
 * @version $Id:$
 */
public class SFTPCompressFeature implements Compress {

    private SFTPSession session;

    public SFTPCompressFeature(final SFTPSession session) {
        this.session = session;
    }

    /**
     * Create ompressed archive.
     *
     * @param archive Archive format description
     * @param files   List of files to archive
     */
    public void archive(final ch.cyberduck.core.Archive archive, final List<Path> files) throws BackgroundException {
        new SFTPCommandFeature(session).send(archive.getCompressCommand(files));
    }

    /**
     * Unpack compressed archive
     *
     * @param archive Archive format description
     * @param file    File to decompress
     */
    public void unarchive(final ch.cyberduck.core.Archive archive, final Path file) throws BackgroundException {
        new SFTPCommandFeature(session).send(archive.getDecompressCommand(file));
    }
}
