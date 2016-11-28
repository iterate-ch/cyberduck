package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.BookmarkCollection;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.HistoryCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.EnumSet;

public class MountWorker extends Worker<Path> {
    private static final Logger log = Logger.getLogger(MountWorker.class);

    private final Host bookmark;
    private final Cache<Path> cache;
    private final ListProgressListener listener;
    private final LoginCallback login;

    public MountWorker(final Host bookmark, final Cache<Path> cache, final ListProgressListener listener, LoginCallback login) {
        this.bookmark = bookmark;
        this.cache = cache;
        this.listener = listener;
        this.login = login;
    }

    /**
     * Mount the default path of the configured host or the home directory as returned by the server
     * when not given.
     */
    @Override
    public Path run(final Session<?> session) throws BackgroundException {
        Path home;
        AttributedList<Path> list;
        try {
            home = session.getFeature(Home.class).find();
            // Remove cached home to force error if repeated attempt to mount fails
            cache.invalidate(home);
            // Retrieve directory listing of default path
            list = new SessionListWorker(cache, home, login, listener).run(session);
        }
        catch(NotfoundException e) {
            log.warn(String.format("Mount failed with %s", e.getMessage()));
            // The default path does not exist or is not readable due to possible permission issues. Fallback
            // to default working directory
            home = new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory));
            // Remove cached home to force error if repeated attempt to mount fails
            cache.invalidate(home);
            // Retrieve directory listing of working directory
            list = new SessionListWorker(cache, home, login, listener).run(session);
        }
        cache.put(home, list);
        return home;
    }

    @Override
    public void cleanup(final Path workdir) {
        if(null != workdir) {
            final HistoryCollection history = HistoryCollection.defaultCollection();
            if(history.isLoaded()) {
                history.add(bookmark);
            }
            // Notify changed bookmark
            final BookmarkCollection bookmarks = BookmarkCollection.defaultCollection();
            if(bookmarks.isLoaded()) {
                if(bookmarks.contains(bookmark)) {
                    bookmarks.collectionItemChanged(bookmark);
                }
            }
        }
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Mounting {0}", "Status"),
                bookmark.getHostname());
    }

    @Override
    public Path initialize() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final MountWorker that = (MountWorker) o;
        if(cache != null ? !cache.equals(that.cache) : that.cache != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return cache != null ? cache.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MountWorker{");
        sb.append("cache=").append(cache);
        sb.append('}');
        return sb.toString();
    }
}
