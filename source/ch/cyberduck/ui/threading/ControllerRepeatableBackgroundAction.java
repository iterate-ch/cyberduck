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

import ch.cyberduck.core.HostKeyControllerFactory;
import ch.cyberduck.core.LoginControllerFactory;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.threading.RepeatableBackgroundAction;
import ch.cyberduck.ui.Controller;

/**
 * @version $Id$
 */
public abstract class ControllerRepeatableBackgroundAction extends RepeatableBackgroundAction {

    public ControllerRepeatableBackgroundAction(final Controller controller,
                                                final AlertCallback alert,
                                                final ProgressListener progressListener,
                                                final TranscriptListener transcriptListener) {
        super(alert, progressListener, transcriptListener,
                LoginControllerFactory.get(controller), HostKeyControllerFactory.get(controller));
    }

}