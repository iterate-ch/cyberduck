package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.joyent.manta.client.MantaObject;

public class MantaSearchFeature implements Search {

    private final MantaSession session;
    private final MantaObjectAttributeAdapter adapter;

    public MantaSearchFeature(final MantaSession session) {
        this.session = session;
        this.adapter = new MantaObjectAttributeAdapter(session);
    }

    @Override
    public AttributedList<Path> search(final Path workdir,
                                       final Filter<Path> regex,
                                       final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();

        // avoid searching the "special" folders if users search from the account root
        if(workdir.getParent().isRoot()) {
            final Predicate<MantaObject> fastSearchPredicate = o -> session.isWorldReadable(o) || session.isUserWritable(o);
            final List<Path> homeFolderPaths = findObjectsAsPaths(workdir, fastSearchPredicate);
            cleanResults(homeFolderPaths, regex);
            addPaths(list, workdir, listener, homeFolderPaths);

            /*
            // disable search of system directories until we can provide incremental results
            // slowSearchPredicate will prevent us from looking at ~~/public and ~~/stor twice
            final Predicate<MantaObject> slowSearchPredicate = fastSearchPredicate.negate();
            final List<Path> systemFolderObjects = findObjectsAsPaths(workdir, slowSearchPredicate.and(regexPredicate));
            cleanResults(systemFolderObjects, regex);
            addPaths(list, workdir, listener, systemFolderObjects);
            */
        }
        else {
            final List<Path> foundPaths = findObjectsAsPaths(workdir, null);
            cleanResults(foundPaths, regex);
            addPaths(list, workdir, listener, foundPaths);
        }

        return list;
    }

    private void cleanResults(final List<Path> foundPaths, final Filter<Path> regex) {
        final Set<Path> removal = new HashSet<>();
        for(final Path f : foundPaths) {
            if(!regex.accept(f)) {
                removal.add(f);
            }
        }
        foundPaths.removeAll(removal);
    }

    private List<Path> findObjectsAsPaths(final Path workdir, final Predicate<MantaObject> searchPredicate) {
        return session.getClient().find(workdir.getAbsolute(), searchPredicate)
                .map(adapter::toPath)
                .collect(Collectors.toList());
    }

    private void addPaths(final AttributedList<Path> list,
                          final Path workdir,
                          final ListProgressListener listener,
                          final List<Path> foundObjects) throws ConnectionCanceledException {
        if(!foundObjects.isEmpty()) {
            list.addAll(foundObjects);
            listener.chunk(workdir, list);
        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }

}
