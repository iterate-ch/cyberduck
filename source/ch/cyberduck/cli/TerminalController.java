package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.MainAction;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import java.util.concurrent.Future;

/**
 * @version $Id$
 */
public class TerminalController extends AbstractController {

    private TranscriptListener transcript;

    private ProgressListener progress;

    public TerminalController(final ProgressListener progress,
                              final TranscriptListener transcript) {
        this.transcript = transcript;
        this.progress = progress;
    }

    @Override
    public <T> Future<T> background(final BackgroundAction<T> action) {
        try {
            action.prepare();
            return ConcurrentUtils.constantFuture(action.call());
        }
        catch(BackgroundException e) {
            final StringAppender b = new StringAppender();
            b.append(e.getMessage());
            b.append(e.getDetail());
            this.message(b.toString());
        }
        return null;
    }

    @Override
    public void invoke(final MainAction runnable, final boolean wait) {
        runnable.run();
    }

    @Override
    public void message(final String message) {
        progress.message(message);
    }

    @Override
    public void log(final boolean request, final String message) {
        transcript.log(request, message);
    }
}
