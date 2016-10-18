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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostKeyCallbackFactory;
import ch.cyberduck.core.LoginService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;

public abstract class RegistryBackgroundAction<T> extends ControllerBackgroundAction<T> {

    private BackgroundActionRegistry registry
            = BackgroundActionRegistry.global();

    public RegistryBackgroundAction(final Controller controller,
                                    final Session<?> session,
                                    final Cache<Path> cache) {
        super(controller, session, cache);
    }

    public RegistryBackgroundAction(final Controller controller,
                                    final Session<?> session,
                                    final Cache<Path> cache,
                                    final ProgressListener progress,
                                    final TranscriptListener transcript) {
        super(controller, session, cache, progress, transcript);
    }

    public RegistryBackgroundAction(final ConnectionService connection,
                                    final Controller controller,
                                    final Session<?> session,
                                    final Cache<Path> cache) {
        super(connection, controller, session, cache, controller, controller);
    }

    public RegistryBackgroundAction(final ConnectionService connection,
                                    final Controller controller,
                                    final Session<?> session,
                                    final Cache<Path> cache,
                                    final ProgressListener progress,
                                    final TranscriptListener transcript) {
        super(connection, controller, session, cache, progress, transcript);
    }

    public RegistryBackgroundAction(final LoginService login,
                                    final Controller controller,
                                    final Session<?> session,
                                    final Cache<Path> cache,
                                    final ProgressListener progress,
                                    final TranscriptListener transcript,
                                    final HostKeyCallback key) {
        super(login, controller, session, cache, progress, transcript, key);
    }

    public RegistryBackgroundAction(final LoginService login,
                                    final Controller controller,
                                    final Session<?> session,
                                    final Cache<Path> cache) {
        super(login, controller, session, cache, controller, controller,
                HostKeyCallbackFactory.get(controller, session.getHost().getProtocol()));
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
}