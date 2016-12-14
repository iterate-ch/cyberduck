package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

public class TransferPromptListWorker extends TransferWorker<List<TransferItem>> {
    private static final Logger log = Logger.getLogger(TransferPromptListWorker.class);

    private final Path directory;

    private final Local local;

    private final Transfer transfer;

    private final ProgressListener listener;

    public TransferPromptListWorker(final Transfer transfer,
                                    final Path directory, final Local local,
                                    final ProgressListener listener) {
        this.directory = directory;
        this.local = local;
        this.transfer = transfer;
        this.listener = listener;
    }

    @Override
    public List<TransferItem> run(final Session<?> source, final Session<?> destination) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List directory %s", directory));
        }
        return transfer.list(source, destination, directory, local, new ActionListProgressListener(this, listener));
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Listing directory {0}", "Status"), directory.getName());
    }

    @Override
    public List<TransferItem> initialize() {
        return Collections.emptyList();
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransferPromptListWorker{");
        sb.append("transfer=").append(transfer);
        sb.append('}');
        return sb.toString();
    }
}