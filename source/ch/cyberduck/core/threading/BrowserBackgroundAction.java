package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.BookmarkCollection;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.HistoryCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.serializer.HostDictionary;

/**
 * @version $Id$
 */
public abstract class BrowserBackgroundAction<T> extends ControllerBackgroundAction<T> {

    private BackgroundActionRegistry registry
            = BackgroundActionRegistry.global();

    public BrowserBackgroundAction(final Controller controller,
                                   final Session<?> session,
                                   final Cache<Path> cache) {
        super(controller, session, cache);
    }

    public BrowserBackgroundAction(final LoginService login,
                                   final Controller controller,
                                   final Session<?> session,
                                   final Cache<Path> cache) {
        super(login, controller, session, cache, controller, controller);
    }

    @Override
    public void init() {
        // Add to the registry so it will be displayed in the activity window.
        registry.add(this);
        super.init();
    }

    @Override
    public void cleanup() {
        registry.remove(this);
        super.cleanup();
    }

    @Override
    protected boolean connect(final Session session) throws BackgroundException {
        final boolean opened = super.connect(session);
        if(opened) {
            // Only notify bookmarks when connection opened.

            final Host bookmark = session.getHost();

            final HistoryCollection history = HistoryCollection.defaultCollection();
            if(history.isLoaded()) {
                history.add(new HostDictionary().deserialize(bookmark.serialize(SerializerFactory.get())));
            }
            // Notify changed bookmark
            final BookmarkCollection bookmarks = BookmarkCollection.defaultCollection();
            if(bookmarks.isLoaded()) {
                if(bookmarks.contains(bookmark)) {
                    bookmarks.collectionItemChanged(bookmark);
                }
            }
        }
        return opened;
    }
}