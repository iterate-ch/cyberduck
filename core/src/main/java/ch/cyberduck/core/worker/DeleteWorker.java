package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Delete;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeleteWorker extends Worker<Boolean> {

    /**
     * Selected files.
     */
    private List<Path> files;

    private LoginCallback prompt;

    private ProgressListener listener;

    private Filter<Path> filter;

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final ProgressListener listener) {
        this(prompt, files, listener, new NullFilter<Path>());
    }

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final ProgressListener listener, final Filter<Path> filter) {
        this.files = files;
        this.prompt = prompt;
        this.listener = listener;
        this.filter = filter;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final List<Path> recursive = new ArrayList<Path>();
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            recursive.addAll(this.compile(session, file));
        }
        final Delete feature = session.getFeature(Delete.class);
        feature.delete(recursive, prompt, new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                listener.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                        file.getName()));
            }
        });
        return true;
    }

    protected List<Path> compile(final Session<?> session, final Path file) throws BackgroundException {
        // Compile recursive list
        final List<Path> recursive = new ArrayList<Path>();
        if(file.isFile() || file.isSymbolicLink()) {
            recursive.add(file);
        }
        else if(file.isDirectory()) {
            for(Path child : session.list(file, new ActionListProgressListener(this, listener)).filter(filter)) {
                if(this.isCanceled()) {
                    throw new ConnectionCanceledException();
                }
                recursive.addAll(this.compile(session, child));
            }
            // Add parent after children
            recursive.add(file);
        }
        return recursive;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                this.toString(files));
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof DeleteWorker)) {
            return false;
        }
        final DeleteWorker that = (DeleteWorker) o;
        return Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeleteWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
