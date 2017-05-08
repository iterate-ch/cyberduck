package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.pasteboard.PathPasteboardFactory;
import ch.cyberduck.core.pool.SessionPool;

import java.text.MessageFormat;

public class DisconnectBackgroundAction extends RegistryBackgroundAction<Void> {
    private final SessionPool session;

    public DisconnectBackgroundAction(final Controller controller, final SessionPool session) {
        super(controller, session);
        this.session = session;
    }

    @Override
    public void prepare() {
        super.prepare();
    }

    @Override
    public Void run() throws BackgroundException {
        session.evict();
        if(session == SessionPool.DISCONNECTED) {
            throw new ConnectionCanceledException();
        }
        PathPasteboardFactory.delete(session.getHost());
        return null;
    }

    @Override
    public Void run(final Session<?> session) throws BackgroundException {
        throw new ConnectionCanceledException();
    }

    @Override
    public String getActivity() {
        if(session == SessionPool.DISCONNECTED) {
            return super.getActivity();
        }
        return MessageFormat.format(LocaleFactory.localizedString("Disconnecting {0}", "Status"),
                BookmarkNameProvider.toString(session.getHost()));
    }
}
