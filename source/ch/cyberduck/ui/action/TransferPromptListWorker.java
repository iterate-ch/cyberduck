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

import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class TransferPromptListWorker extends Worker<AttributedList<Path>> {
    private static final Logger log = Logger.getLogger(TransferPromptListWorker.class);

    private Path directory;

    private Session session;

    private Transfer transfer;

    public TransferPromptListWorker(final Session session, final Transfer transfer, final Path directory) {
        this.session = session;
        this.directory = directory;
        this.transfer = transfer;
    }

    @Override
    public AttributedList<Path> run() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List directory %s", directory));
        }
        return transfer.list(session, directory, new ActionListProgressListener(this));
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Listing directory {0}", "Status"), directory.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final TransferPromptListWorker that = (TransferPromptListWorker) o;
        if(directory != null ? !directory.equals(that.directory) : that.directory != null) {
            return false;
        }
        if(transfer != null ? !transfer.equals(that.transfer) : that.transfer != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = directory != null ? directory.hashCode() : 0;
        result = 31 * result + (transfer != null ? transfer.hashCode() : 0);
        return result;
    }
}