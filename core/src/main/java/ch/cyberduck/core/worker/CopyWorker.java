package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.comparator.TimestampComparator;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CopyWorker extends Worker<Map<Path, Path>> {

    private final Map<Path, Path> files;
    private final SessionPool target;
    private final ProgressListener listener;
    private final Cache<Path> cache;
    private final ConnectionCallback callback;

    public CopyWorker(final Map<Path, Path> files, final SessionPool target, final Cache<Path> cache, final ProgressListener listener, final ConnectionCallback callback) {
        this.files = files;
        this.target = target;
        this.listener = listener;
        this.cache = cache;
        this.callback = callback;
    }

    @Override
    public Map<Path, Path> run(final Session<?> session) throws BackgroundException {
        final Session<?> destination = target.borrow(new BackgroundActionState() {
            @Override
            public boolean isCanceled() {
                return CopyWorker.this.isCanceled();
            }

            @Override
            public boolean isRunning() {
                return true;
            }
        });
        try {
            final Copy copy = session.getFeature(Copy.class).withTarget(destination);
            final ListService list = session.getFeature(ListService.class);
            final Map<Path, Path> result = new HashMap<>();
            for(Map.Entry<Path, Path> entry : files.entrySet()) {
                if(this.isCanceled()) {
                    throw new ConnectionCanceledException();
                }
                final Map<Path, Path> recursive = this.compile(copy, list, entry.getKey(), entry.getValue());
                for(Map.Entry<Path, Path> r : recursive.entrySet()) {
                    if(r.getKey().isDirectory() && !copy.isRecursive(r.getKey(), r.getValue())) {
                        // Create directory unless copy implementation is recursive
                        final Directory directory = session.getFeature(Directory.class);
                        result.put(r.getKey(), directory.mkdir(r.getValue(),
                                new TransferStatus().withRegion(r.getKey().attributes().getRegion())));
                    }
                    else {
                        final TransferStatus status = new TransferStatus()
                                .withMime(new MappingMimeTypeService().getMime(r.getValue().getName()))
                                .exists(new CachingFindFeature(cache, session.getFeature(Find.class, new DefaultFindFeature(session))).find(r.getValue()))
                                .withLength(r.getKey().attributes().getSize());
                        final Path copied = copy.copy(r.getKey(), r.getValue(), status, callback, new DisabledStreamListener());
                        if(PathAttributes.EMPTY.equals(copied.attributes())) {
                            copied.withAttributes(session.getFeature(AttributesFinder.class).find(copied));
                        }
                        result.put(r.getKey(), copied);
                    }
                }
            }
            return result;
        }
        finally {
            target.release(destination, null);
        }
    }

    protected Map<Path, Path> compile(final Copy copy, final ListService list, final Path source, final Path target) throws BackgroundException {
        // Compile recursive list
        final Map<Path, Path> recursive = new LinkedHashMap<>();
        if(source.isFile() || source.isSymbolicLink()) {
            recursive.put(source, target);
        }
        else if(source.isDirectory()) {
            // Add parent before children
            recursive.put(source, target);
            if(!copy.isRecursive(source, target)) {
                // sort ascending by timestamp to copy older versions first
                final AttributedList<Path> children = list.list(source, new WorkerListProgressListener(this, listener)).
                        filter(new TimestampComparator(true));
                for(Path child : children) {
                    if(this.isCanceled()) {
                        throw new ConnectionCanceledException();
                    }
                    recursive.putAll(this.compile(copy, list, child, new Path(target, child.getName(), child.getType())));
                }
            }
        }
        return recursive;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Copying {0} to {1}", "Status"),
                files.keySet().iterator().next().getName(), files.values().iterator().next().getName());
    }

    @Override
    public Map<Path, Path> initialize() {
        return Collections.emptyMap();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final CopyWorker that = (CopyWorker) o;
        if(!Objects.equals(files, that.files)) {
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
        final StringBuilder sb = new StringBuilder("CopyWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
