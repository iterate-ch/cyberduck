package ch.cyberduck.core.vault;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.features.Vault;

import org.apache.log4j.Logger;

public class DecryptingListProgressListener extends IndexedListProgressListener {
    private static final Logger log = Logger.getLogger(DecryptingListProgressListener.class);

    private final Session<?> session;
    private final Vault vault;
    private final ListProgressListener delegate;

    public DecryptingListProgressListener(final Session<?> session, final Vault vault,
                                          final ListProgressListener delegate) {
        this.session = session;
        this.vault = vault;
        this.delegate = delegate;
    }

    @Override
    public void visit(final AttributedList<Path> list, final int index, final Path file) throws ListCanceledException {
        for(int i = index; i < list.size(); i++) {
            final Path f = list.get(i);
            try {
                list.set(i, vault.decrypt(session, f));
            }
            catch(BackgroundException e) {
                log.error(String.format("Failure decrypting %s. %s", f, e.getDetail()));
            }
        }
    }

    @Override
    public void chunk(final Path folder, final AttributedList<Path> list) throws ConnectionCanceledException {
        super.chunk(folder, list);
        delegate.chunk(folder, list);
    }

    @Override
    public void message(final String message) {
        delegate.message(message);
    }
}
