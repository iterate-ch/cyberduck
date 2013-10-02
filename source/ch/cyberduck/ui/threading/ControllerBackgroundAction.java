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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.threading.SessionBackgroundAction;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.HostKeyControllerFactory;
import ch.cyberduck.ui.LoginControllerFactory;

import java.util.List;

/**
 * @version $Id$
 */
public abstract class ControllerBackgroundAction<T> extends SessionBackgroundAction<T> {

    public ControllerBackgroundAction(final Session<?> session, final Cache cache,
                                      final Controller controller, final AlertCallback alert,
                                      final ProgressListener progressListener, final TranscriptListener transcriptListener) {
        super(session, cache, alert, progressListener, transcriptListener,
                LoginControllerFactory.get(controller), HostKeyControllerFactory.get(controller));
    }


    public ControllerBackgroundAction(final List<Session<?>> sessions,
                                      final Cache cache,
                                      final Controller controller, final AlertCallback alert,
                                      final ProgressListener progressListener,
                                      final TranscriptListener transcriptListener) {
        super(sessions, cache, alert, progressListener, transcriptListener,
                LoginControllerFactory.get(controller), HostKeyControllerFactory.get(controller));
    }
}