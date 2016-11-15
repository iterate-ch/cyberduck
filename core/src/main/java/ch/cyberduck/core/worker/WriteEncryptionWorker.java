package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Encryption;

import java.text.MessageFormat;
import java.util.List;

public class WriteEncryptionWorker extends Worker<Boolean> {

    /**
     * Selected files.
     */
    private final List<Path> files;

    /**
     * Algorithm
     */
    private final Encryption.Algorithm algorithm;

    /**
     * Descend into directories
     */
    private final RecursiveCallback<Encryption.Algorithm> callback;

    private final ProgressListener listener;

    public WriteEncryptionWorker(final List<Path> files, final Encryption.Algorithm algorithm,
                                 final boolean recursive, final ProgressListener listener) {
        this(files, algorithm, new BooleanRecursiveCallback<Encryption.Algorithm>(recursive), listener);
    }

    public WriteEncryptionWorker(final List<Path> files, final Encryption.Algorithm algorithm,
                                 final RecursiveCallback<Encryption.Algorithm> callback, final ProgressListener listener) {
        this.files = files;
        this.algorithm = algorithm;
        this.callback = callback;
        this.listener = listener;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final Encryption feature = session.getFeature(Encryption.class);
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            this.write(session, feature, file);
        }
        return true;
    }

    protected void write(final Session<?> session, final Encryption feature, final Path file) throws BackgroundException {
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        listener.message(MessageFormat.format(LocaleFactory.localizedString("Writing metadata of {0}", "Status"),
                file.getName()));
        feature.setEncryption(file, algorithm);
        if(file.isDirectory()) {
            if(callback.recurse(file, algorithm)) {
                for(Path child : session.list(file, new ActionListProgressListener(this, listener))) {
                    this.write(session, feature, child);
                }
            }
        }
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Writing metadata of {0}", "Status"),
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
        final WriteEncryptionWorker that = (WriteEncryptionWorker) o;
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
        final StringBuilder sb = new StringBuilder("WriteRedundancyWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
