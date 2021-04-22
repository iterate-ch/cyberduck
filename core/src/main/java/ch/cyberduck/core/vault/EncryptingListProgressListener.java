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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Vault;

import org.apache.log4j.Logger;

public class EncryptingListProgressListener extends IndexedListProgressListener {
    private static final Logger log = Logger.getLogger(EncryptingListProgressListener.class);

    private final Session<?> session;
    private final Vault vault;
    private final ListProgressListener delegate;

    public EncryptingListProgressListener(final Session<?> session, final Vault vault, final ListProgressListener delegate) {
        this.session = session;
        this.vault = vault;
        this.delegate = delegate;
    }

    @Override
    public void visit(final AttributedList<Path> list, final int index, final Path f) {
        try {
            if(f.attributes().getVersions().isEmpty()) {
                list.set(index, vault.encrypt(session, f, true));
            }
            else {
                final AttributedList<Path> versions = new AttributedList<>();
                for(Path version : f.attributes().getVersions()) {
                    versions.add(vault.encrypt(session, version, true));
                }
                list.set(index, vault.encrypt(session, f, true).withAttributes(new PathAttributes(f.attributes()).withVersions(versions)));
            }
        }
        catch(BackgroundException e) {
            log.error(String.format("Failure decrypting %s. %s", f, e));
            list.remove(index);
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
