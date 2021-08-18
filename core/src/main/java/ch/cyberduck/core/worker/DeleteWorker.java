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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.browser.PathReloadFinder;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DeleteWorker extends Worker<List<Path>> {

    private static final Logger log = Logger.getLogger(DeleteWorker.class);

    /**
     * Selected files.
     */
    private final List<Path> files;
    private final LoginCallback prompt;
    private final Cache<Path> cache;
    private final ProgressListener listener;
    private final Filter<Path> filter;

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final Cache<Path> cache, final ProgressListener listener) {
        this(prompt, files, cache, new NullFilter<Path>(), listener);
    }

    public DeleteWorker(final LoginCallback prompt, final List<Path> files, final Cache<Path> cache, final Filter<Path> filter, final ProgressListener listener) {
        this.files = files;
        this.prompt = prompt;
        this.cache = cache;
        this.listener = listener;
        this.filter = filter;
    }

    @Override
    public List<Path> run(final Session<?> session) throws BackgroundException {
        final Delete delete = session.getFeature(Delete.class);
        final ListService list = session.getFeature(ListService.class);
        final Map<Path, TransferStatus> recursive = new LinkedHashMap<>();
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            recursive.putAll(this.compile(session.getHost(), delete, list, new WorkerListProgressListener(this, listener), file));
        }
        // Iterate again to delete any files that can be omitted when recursive operation is supported
        if(delete.isRecursive()) {
            recursive.keySet().removeIf(f -> recursive.keySet().stream().anyMatch(f::isChild));
        }
        delete.delete(recursive, prompt, new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                listener.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                    file.getName()));
            }
        });
        return new ArrayList<>(recursive.keySet());
    }

    protected Map<Path, TransferStatus> compile(final Host host, final Delete delete, final ListService list, final ListProgressListener listener, final Path file) throws BackgroundException {
        // Compile recursive list
        final Map<Path, TransferStatus> recursive = new LinkedHashMap<>();
        if(file.isFile() || file.isSymbolicLink()) {
            switch(host.getProtocol().getType()) {
                case s3:
                    if(!file.attributes().isDuplicate()) {
                        if(!file.getType().contains(Path.Type.upload)) {
                            // Add delete marker
                            final Path marker = new Path(file);
                            log.debug(String.format("Nullify version to add delete marker for %s", file));
                            marker.attributes().setVersionId(null);
                            recursive.put(marker, new TransferStatus().withLockId(this.getLockId(marker)));
                        }
                    }
                    break;
                default:
                    recursive.put(file, new TransferStatus().withLockId(this.getLockId(file)));
                    break;
            }
        }
        else if(file.isDirectory()) {
            if(!delete.isRecursive()) {
                for(Path child : list.list(file, listener).filter(filter)) {
                    if(this.isCanceled()) {
                        throw new ConnectionCanceledException();
                    }
                    recursive.putAll(this.compile(host, delete, list, listener, child));
                }
            }
            // Add parent after children
            recursive.put(file, new TransferStatus().withLockId(this.getLockId(file)));
        }
        return recursive;
    }

    protected String getLockId(final Path file) {
        return null;
    }

    @Override
    public void cleanup(final List<Path> deleted) {
        for(Path folder : new PathReloadFinder().find(new ArrayList<>(deleted))) {
            cache.invalidate(folder);
        }
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
