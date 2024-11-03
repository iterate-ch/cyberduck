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
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.regex.Pattern;

public class DecryptingListProgressListener extends IndexedListProgressListener {
    private static final Logger log = LogManager.getLogger(DecryptingListProgressListener.class);

    private final Session<?> session;
    private final Vault vault;
    private final ListProgressListener delegate;
    private final Filter<Path> skip;

    public DecryptingListProgressListener(final Session<?> session, final Vault vault, final ListProgressListener delegate) {
        this(session, vault, delegate, new SkipRegexFilter(Pattern.compile(PreferencesFactory.get().getProperty("cryptomator.vault.skip.regex"))));
    }

    public DecryptingListProgressListener(final Session<?> session, final Vault vault, final ListProgressListener delegate, final Filter<Path> skip) {
        this.session = session;
        this.vault = vault;
        this.delegate = delegate;
        this.skip = skip;
    }

    @Override
    public void visit(final AttributedList<Path> list, final int index, final Path f) {
        if(skip.accept(f)) {
            if(log.isDebugEnabled()) {
                log.debug("Skip decrypting {}", f);
            }
            list.remove(index);
            return;
        }
        try {
            f.getType().add(Path.Type.encrypted);
            list.set(index, vault.decrypt(session, f));
        }
        catch(BackgroundException e) {
            log.error("Failure {} decrypting {}", e, f);
            list.remove(index);
        }
    }

    @Override
    public void chunk(final Path directory, final AttributedList<Path> list) throws ConnectionCanceledException {
        super.chunk(directory, list);
        delegate.chunk(directory, list);
        super.chunk(directory, list);
    }

    @Override
    public void finish(final Path directory, final AttributedList<Path> list, final Optional<BackgroundException> e) throws ConnectionCanceledException {
        delegate.finish(directory, list, e);
    }

    @Override
    public void message(final String message) {
        delegate.message(message);
    }

    private static final class SkipRegexFilter implements Filter<Path> {

        private final Pattern pattern;

        public SkipRegexFilter(final Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean accept(final Path file) {
            return pattern.matcher(file.getName()).matches();
        }

        @Override
        public Pattern toPattern() {
            return pattern;
        }
    }
}
