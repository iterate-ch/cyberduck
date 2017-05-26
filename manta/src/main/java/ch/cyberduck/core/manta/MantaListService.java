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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.client.MantaObject;
import com.joyent.manta.exception.MantaException;
import com.joyent.manta.exception.MantaIOException;


public class MantaListService implements ListService {

    private final MantaSession session;
    private final MantaObjectAttributeAdapter adapter;

    public MantaListService(final MantaSession session) {
        this.session = session;
        adapter = new MantaObjectAttributeAdapter(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {

        final AttributedList<Path> children = new AttributedList<>();
        final MantaClient c = session.getClient();
        final String remotePath = session.pathMapper.requestPath(directory);

        final Iterator<MantaObject> objectsIter;
        try {
            objectsIter = c.listObjects(remotePath).iterator();
        }
        catch(MantaIOException me) {
            throw session.exceptionMapper.map("Listing directory {0} failed", me);
        }
        catch(IOException ioe) {
            throw new DefaultIOExceptionMappingService().map("Listing directory {0} failed", ioe);
        }

        while(objectsIter.hasNext()) {
            MantaObject o = objectsIter.next();
            final PathAttributes attr = adapter.from(o);
            final Path path = new Path(
                    directory,
                    attr.getDisplayname(),
                    o.isDirectory()
                            ? EnumSet.of(Path.Type.directory)
                            : EnumSet.of(Path.Type.file),
                    attr);

            children.add(path);
            listener.chunk(directory, children);
        }

        return children;
    }
}
