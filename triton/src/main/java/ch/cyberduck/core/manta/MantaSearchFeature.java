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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Search;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.joyent.manta.client.MantaObject;

public class MantaSearchFeature implements Search {

    private static final Logger LOG = Logger.getLogger(MantaSearchFeature.class);

    private final MantaSession session;
    private final MantaObjectAttributeAdapter adapter;
    private Cache<Path> cache;

    public MantaSearchFeature(final MantaSession session) {
        this.session = session;
        this.adapter = new MantaObjectAttributeAdapter(session);
        this.cache = PathCache.empty();
    }

    /**
     * @param workdir  Current working directory in browser
     * @param regex    Search string
     * @param listener Notification listener
     * @return List of files found or empty list
     */
    @Override
    public AttributedList<Path> search(final Path workdir, final Filter<Path> regex, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();

        final Predicate<MantaObject> regexPredicate = o -> regex.accept(adapter.toPath(o));
        final Predicate<MantaObject> fastSearchPredicate = o -> session.isWorldReadable(o) || session.isUserWritable(o);;

        // this secondary search allows us to find objects in system folders which may take a very long time to inspect
        // final Predicate<MantaObject> slowSearchPredicate = fastSearchPredicate.negate();

        if(workdir.getParent().isRoot()) {
            final List<Path> homeFolderObjects = searchAndConvertObjects(workdir, fastSearchPredicate.and(regexPredicate));
            addFoundObjects(list, workdir, listener, discardEmptyDirectories(homeFolderObjects, regex));

            // disable search of system directories until we can provide incremental results
            // final List<Path> systemFolderObjects = searchAndConvertObjects(workdir, slowSearchPredicate.and(regexPredicate));
            // addFoundObjects(list, workdir, listener, systemFolderObjects);
        }

        return list;
    }

    private List<Path> discardEmptyDirectories(final List<Path> foundPaths, final Filter<Path> regex) {
        final ArrayList<Path> pathsRetained = new ArrayList<>(Math.floorDiv(foundPaths.size(), 2));
        final Pattern pattern = regex.toPattern();

        for (final Path candidatePath : foundPaths) {
            final String candidateName = candidatePath.getName();
            if (pattern.matcher(candidateName).matches()) {
                pathsRetained.add(candidatePath);
            }
        }

        return pathsRetained;
    }

    private List<Path> searchAndConvertObjects(final Path workdir, final Predicate<MantaObject> searchPredicate) {
        return session.getClient().find(workdir.getAbsolute(), searchPredicate)
            .map(adapter::toPath)
            .collect(Collectors.toList());
    }

    private void addFoundObjects(final AttributedList<Path> list,
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

    @Override
    public Search withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
