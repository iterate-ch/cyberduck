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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferErrorCallback;

public class TerminalTransferErrorCallback implements TransferErrorCallback {

    private final TerminalProgressListener console
            = new TerminalProgressListener();

    private final TerminalPromptReader prompt;

    public TerminalTransferErrorCallback() {
        this.prompt = new InteractiveTerminalPromptReader();
    }

    public TerminalTransferErrorCallback(final TerminalPromptReader prompt) {
        this.prompt = prompt;
    }

    @Override
    public boolean prompt(final BackgroundException failure) throws BackgroundException {
        final StringAppender appender = new StringAppender();
        appender.append(failure.getMessage());
        appender.append(failure.getDetail());
        console.message(appender.toString());
        if(!prompt.prompt(LocaleFactory.localizedString("Continue", "Credentials"))) {
            throw failure;
        }
        return true;
    }
}
