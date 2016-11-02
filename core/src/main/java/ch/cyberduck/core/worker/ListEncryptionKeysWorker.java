package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Encryption;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListEncryptionKeysWorker extends Worker<Set<Encryption.Algorithm>> {

    /**
     * Selected files.
     */
    private final List<Path> files;

    private final LoginCallback prompt;

    public ListEncryptionKeysWorker(final List<Path> files, final LoginCallback prompt) {
        this.files = files;
        this.prompt = prompt;
    }

    @Override
    public Set<Encryption.Algorithm> run(final Session<?> session) throws BackgroundException {
        final Encryption feature = session.getFeature(Encryption.class);
        final Set<Encryption.Algorithm> keys = new HashSet<>();
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            keys.addAll(feature.getKeys(file, prompt));
        }
        return keys;
    }


    @Override
    public Set<Encryption.Algorithm> initialize() {
        return Collections.emptySet();
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                this.toString(files));
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final ListEncryptionKeysWorker that = (ListEncryptionKeysWorker) o;
        if(files != null ? !files.equals(that.files) : that.files != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return files != null ? files.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReadEncryptionKeysWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
