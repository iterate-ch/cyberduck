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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.EnumSet;

public class LocalListService implements ListService {

    private final LocalSession session;
    private final LocalAttributesFinderFeature feature;

    public LocalListService(final LocalSession session) {
        this.session = session;
        this.feature = new LocalAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<ch.cyberduck.core.Path> paths = new AttributedList<>();
        try (DirectoryStream<java.nio.file.Path> directoryStream = Files.newDirectoryStream(session.getClient().getPath(directory.getAbsolute()))) {
            for(java.nio.file.Path path : directoryStream) {
                final PathAttributes attributes = feature.convert(path);
                final EnumSet<Path.Type> type = EnumSet.noneOf(Path.Type.class);
                if(Files.isDirectory(path)) {
                    type.add(Path.Type.directory);
                }
                if(Files.isRegularFile(path)) {
                    type.add(Path.Type.file);
                }
                if(Files.isSymbolicLink(path)) {
                    type.add(Path.Type.symboliclink);
                }
                paths.add(new Path(directory, path.getFileName().toString(), type, attributes));
                listener.chunk(directory, paths);
            }
        }
        catch(IOException ex) {
            throw new LocalExceptionMappingService().map("Listing directory {0} failed", ex, directory);
        }
        return paths;
    }
}
