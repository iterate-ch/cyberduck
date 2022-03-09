package ch.cyberduck.core.manta;

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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

import com.joyent.manta.client.MantaObject;
import com.joyent.manta.exception.MantaClientHttpResponseException;
import com.joyent.manta.exception.MantaObjectException;

public class MantaListService implements ListService {

    private final MantaSession session;

    public MantaListService(final MantaSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            return new AttributedList<Path>(Collections.singletonList(
                new MantaAccountHomeInfo(session.getHost().getCredentials().getUsername(), session.getHost().getDefaultPath()).getNormalizedHomePath()));
        }
        final AttributedList<Path> children = new AttributedList<>();
        final Iterator<MantaObject> objectsIter;
        try {
            objectsIter = session.getClient().listObjects(directory.getAbsolute()).iterator();
        }
        catch(MantaObjectException e) {
            throw new MantaExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(MantaClientHttpResponseException e) {
            throw new MantaHttpExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Listing directory {0} failed", e);
        }
        final MantaObjectAttributeAdapter adapter = new MantaObjectAttributeAdapter(session);
        while(objectsIter.hasNext()) {
            MantaObject o = objectsIter.next();
            final Path file = new Path(directory, PathNormalizer.name(o.getPath()),
                    EnumSet.of(o.isDirectory() ? Path.Type.directory : Path.Type.file), adapter.toAttributes(o)
            );
            children.add(file);
            listener.chunk(directory, children);
        }
        return children;
    }
}
