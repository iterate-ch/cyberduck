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

import ch.cyberduck.core.Archive;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Compress;

import java.util.List;

public class SFTPCompressFeature implements Compress {

    private SFTPCommandFeature command;

    public SFTPCompressFeature(final SFTPSession session) {
        this.command = new SFTPCommandFeature(session);
    }

    /**
     * Create compressed archive.
     *
     * @param archive    Archive format description
     * @param workdir    Working directory
     * @param files      List of files to archive
     * @param transcript Transcript
     */
    public void archive(final Archive archive, final Path workdir, final List<Path> files,
                        final ProgressListener listener, final TranscriptListener transcript) throws BackgroundException {
        command.send(archive.getCompressCommand(workdir, files), listener, transcript);
    }

    /**
     * Unpack compressed archive
     *
     * @param archive    Archive format description
     * @param file       File to decompress
     * @param transcript Transcript
     */
    public void unarchive(final Archive archive, final Path file,
                          final ProgressListener listener, final TranscriptListener transcript) throws BackgroundException {
        command.send(archive.getDecompressCommand(file), listener, transcript);
    }
}
