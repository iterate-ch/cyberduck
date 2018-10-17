package ch.cyberduck.core.spectra;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.s3.S3ListService;

import java.util.HashMap;

public class SpectraListService extends S3ListService {

    public SpectraListService(final SpectraSession session) {
        super(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return super.list(directory, new IndexedListProgressListener() {

            @Override
            public void message(final String message) {
                listener.message(message);
            }

            @Override
            public void chunk(final Path folder, final AttributedList<Path> list) throws ConnectionCanceledException {
                super.chunk(folder, list);
                listener.chunk(folder, list);
            }

            @Override
            public void visit(final AttributedList<Path> list, final int index, final Path p) {
                if(p.isFile()) {
                    if(p.attributes().isDuplicate()) {
                        if(p.attributes().getRevision() == 1) {
                            final HashMap<String, String> custom = new HashMap<>(p.attributes().getCustom());
                            custom.put(SpectraVersioningFeature.KEY_REVERTABLE, Boolean.TRUE.toString());
                            p.attributes().setCustom(custom);
                        }
                    }
                }
            }
        });
    }
}
