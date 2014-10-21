package ch.cyberduck.ui.threading;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.SessionBackgroundAction;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.HostKeyCallbackFactory;
import ch.cyberduck.ui.LoginCallbackFactory;

/**
 * @version $Id$
 */
public abstract class ControllerBackgroundAction<T> extends SessionBackgroundAction<T> {

    private Controller controller;

    public ControllerBackgroundAction(final Controller controller, final Session<?> session, final Cache<Path> cache) {
        super(session, cache, controller, controller, controller,
                LoginCallbackFactory.get(controller), HostKeyCallbackFactory.get(controller, session.getHost().getProtocol()));
        this.controller = controller;
    }

    public ControllerBackgroundAction(final Controller controller, final Session<?> session, final Cache<Path> cache,
                                      final ProgressListener listener) {
        super(session, cache, controller, listener, controller,
                LoginCallbackFactory.get(controller), HostKeyCallbackFactory.get(controller, session.getHost().getProtocol()));
        this.controller = controller;
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        this.addListener(controller);
        super.prepare();
    }

    @Override
    public void finish() {
        super.finish();
        this.removeListener(controller);
    }
}