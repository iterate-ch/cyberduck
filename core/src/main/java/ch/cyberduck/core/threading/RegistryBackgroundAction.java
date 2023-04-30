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

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.pool.SessionPool;

public abstract class RegistryBackgroundAction<T> extends ControllerBackgroundAction<T> {

    private final BackgroundActionRegistry global
            = BackgroundActionRegistry.global();

    public RegistryBackgroundAction(final Controller controller,
                                    final SessionPool session) {
        super(controller, session);
    }

    public RegistryBackgroundAction(final Controller controller,
                                    final SessionPool session,
                                    final ProgressListener progress) {
        super(controller, session, progress);
    }

    public RegistryBackgroundAction(final BackgroundActionListener controller,
                                    final SessionPool session,
                                    final ProgressListener progress,
                                    final AlertCallback alert) {
        super(controller, session, progress, alert);
    }

    @Override
    public void init() throws BackgroundException {
        // Add to the registry, so it will be displayed in the activity window.
        global.add(this);
        super.init();
    }
}
