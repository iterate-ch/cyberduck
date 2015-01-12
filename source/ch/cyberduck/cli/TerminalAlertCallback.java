package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.AlertCallback;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Id$
 */
public class TerminalAlertCallback implements AlertCallback {

    private final Console console = new Console();

    private AtomicBoolean retry;

    public TerminalAlertCallback(final boolean retry) {
        this.retry = new AtomicBoolean(retry);
    }

    @Override
    public boolean alert(final Host host, final BackgroundException failure, final StringBuilder transcript) {
        final StringAppender b = new StringAppender();
        b.append(failure.getMessage());
        b.append(failure.getDetail());
        console.printf("%n%s", b.toString());
        return retry.getAndSet(false);
    }
}
