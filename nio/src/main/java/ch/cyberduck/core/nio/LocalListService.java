package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.EnumSet;

public class LocalListService implements ListService {
    private static final Logger log = LogManager.getLogger(LocalListService.class);

    private final LocalSession session;
    private final LocalAttributesFinderFeature feature;

    public LocalListService(final LocalSession session) {
        this.session = session;
        this.feature = new LocalAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<ch.cyberduck.core.Path> paths = new AttributedList<>();
        final java.nio.file.Path p = session.toPath(directory);
        if(!Files.exists(p)) {
            throw new LocalExceptionMappingService().map("Listing directory {0} failed",
                    new NoSuchFileException(directory.getAbsolute()), directory);
        }
        try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(p)) {
            for(java.nio.file.Path n : stream) {
                if(null == n.getFileName()) {
                    continue;
                }
                try {
                    final PathAttributes attributes = feature.toAttributes(n);
                    final EnumSet<Path.Type> type = EnumSet.noneOf(Path.Type.class);
                    if(Files.isDirectory(n)) {
                        type.add(Path.Type.directory);
                    }
                    else {
                        type.add(Path.Type.file);
                    }
                    final Path file = new Path(directory, n.getFileName().toString(), type, attributes);
                    if(this.post(n, file)) {
                        paths.add(file);
                        listener.chunk(directory, paths);
                    }
                }
                catch(IOException e) {
                    log.warn(String.format("Failure reading attributes for %s", n));
                }
            }
        }
        catch(IOException ex) {
            throw new LocalExceptionMappingService().map("Listing directory {0} failed", ex, directory);
        }
        return paths;
    }

    protected boolean post(final java.nio.file.Path path, final Path file) {
        if(Files.isSymbolicLink(path)) {
            try {
                final java.nio.file.Path p = Files.readSymbolicLink(path);
                final Path target = new Path(p.toString(), EnumSet.of(Path.Type.file));
                final Path.Type type;
                if(Files.isDirectory(p)) {
                    type = Path.Type.directory;
                }
                else {
                    type = Path.Type.file;
                }
                file.setType(EnumSet.of(Path.Type.symboliclink, type));
                target.setType(EnumSet.of(type));
                file.setSymlinkTarget(target);
            }
            catch(IOException e) {
                log.warn(String.format("Failure to read symbolic link of %s. %s", file, e.getMessage()));
                return false;
            }
        }
        return true;
    }
}
