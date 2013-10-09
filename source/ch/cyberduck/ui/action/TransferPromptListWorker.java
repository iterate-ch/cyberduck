package ch.cyberduck.ui.action;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class TransferPromptListWorker extends Worker<AttributedList<Path>> {

    private Path directory;

    private Session session;

    private Transfer transfer;

    private TransferStatus status;

    public TransferPromptListWorker(final Session session, final Transfer transfer, final Path directory, final TransferStatus status) {
        this.directory = directory;
        this.transfer = transfer;
        this.status = status;
    }

    @Override
    public AttributedList<Path> run() throws BackgroundException {
        return transfer.list(session, directory, status);
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Listing directory {0}", "Status"), directory.getName());
    }
}