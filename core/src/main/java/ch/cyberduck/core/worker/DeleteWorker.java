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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Delete;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DeleteWorker extends Worker<List<Path>> {

    /**
     * Selected files.
     */
    private final List<Path> files;
    private final LoginCallback prompt;
    private final ProgressListener listener;
    private final Filter<Path> filter;

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final ProgressListener listener) {
        this(prompt, files, new NullFilter<Path>(), listener);
    }

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final Filter<Path> filter, final ProgressListener listener) {
        this.files = files;
        this.prompt = prompt;
        this.listener = listener;
        this.filter = filter;
    }

    @Override
    public List<Path> run(final Session<?> session) throws BackgroundException {
        final Delete delete = session.getFeature(Delete.class);
        final ListService list = session.getFeature(ListService.class);
        final List<Path> recursive = new ArrayList<Path>();
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            recursive.addAll(this.compile(delete, list, new WorkerListProgressListener(this, listener), file));
        }
        delete.delete(recursive, prompt, new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                listener.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                    file.getName()));
            }
        });
        return recursive;
    }

    protected Set<Path> compile(final Delete delete, final ListService list, final ListProgressListener listener, final Path file) throws BackgroundException {
        // Compile recursive list
        final Set<Path> recursive = new LinkedHashSet<>();
        if(file.isFile() || file.isSymbolicLink()) {
            if(file.attributes().isDuplicate()) {
                // Explicitly delete versioned file
            }
            else {
                // Add delete marker
                file.attributes().withVersionId(new VersionId(null));
            }
            recursive.add(file);
        }
        else if(file.isDirectory()) {
            if(!delete.isRecursive()) {
                for(Path child : list.list(file, listener).filter(filter)) {
                    if(this.isCanceled()) {
                        throw new ConnectionCanceledException();
                    }
                    recursive.addAll(this.compile(delete, list, listener, child));
                }
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
    public List<Path> initialize() {
        return Collections.emptyList();
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
