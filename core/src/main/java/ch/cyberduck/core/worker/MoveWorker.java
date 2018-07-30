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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionPoolFactory;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.comparator.TimestampComparator;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class MoveWorker extends Worker<Map<Path, Path>> {

    private final Map<Path, Path> files;
    private final HostPasswordStore keychain;
    private final ProgressListener listener;
    private final Cache<Path> cache;
    private final LoginCallback callback;
    private final HostKeyCallback key;
    private final TranscriptListener transcript;

    public MoveWorker(final Map<Path, Path> files, final Cache<Path> cache,
                      final HostPasswordStore keychain, final LoginCallback callback, final HostKeyCallback key,
                      final ProgressListener listener, final TranscriptListener transcript) {
        this.files = files;
        this.keychain = keychain;
        this.listener = listener;
        this.cache = cache;
        this.callback = callback;
        this.key = key;
        this.transcript = transcript;
    }

    @Override
    public Map<Path, Path> run(final Session<?> session) throws BackgroundException {
        final Move move = session.getFeature(Move.class);
        final ListService list = session.getFeature(ListService.class);
        final Map<Path, Path> sorted = new TreeMap<>(new TimestampComparator(true));
        sorted.putAll(files);
        final Map<Path, Path> result = new HashMap<>();
        for(Map.Entry<Path, Path> entry : sorted.entrySet()) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            if(!move.isSupported(entry.getKey(), entry.getValue())) {
                final Map<Path, Path> copy = new CopyWorker(Collections.singletonMap(entry.getKey(), entry.getValue()),
                    SessionPoolFactory.create(cache, session.getHost(), keychain, callback, key, listener, transcript), cache, listener, callback).run(session);
                for(Map.Entry<Path, Path> r : sorted.entrySet()) {
                    // Delete source files recursively after copy is complete
                    new DeleteWorker(callback, Collections.singletonList(r.getKey()), listener).run(session);
                }
                result.putAll(copy);
            }
            else {
                final Map<Path, Path> recursive = this.compile(move, list, entry.getKey(), entry.getValue());
                for(Map.Entry<Path, Path> r : recursive.entrySet()) {
                    final TransferStatus status = new TransferStatus()
                        .withMime(new MappingMimeTypeService().getMime(r.getValue().getName()))
                        .exists(session.getFeature(Find.class, new DefaultFindFeature(session)).withCache(cache).find(r.getValue()))
                        .length(r.getKey().attributes().getSize());
                    result.put(r.getKey(), move.move(r.getKey(), r.getValue(), status,
                        new Delete.Callback() {
                            @Override
                            public void delete(final Path file) {
                                listener.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                                    file.getName()));
                            }
                        }, callback)
                    );
                }
            }
        }
        return result;
    }

    protected Map<Path, Path> compile(final Move move, final ListService list, final Path source, final Path target) throws BackgroundException {
        // Compile recursive list
        final Map<Path, Path> recursive = new LinkedHashMap<>();
        if(source.isFile() || source.isSymbolicLink()) {
            recursive.put(source, target);
        }
        else if(source.isDirectory()) {
            if(!move.isRecursive(source, target)) {
                // sort ascending by timestamp to move older versions first
                final AttributedList<Path> children = list.list(source, new WorkerListProgressListener(this, listener)).
                    filter(new TimestampComparator(true));
                for(Path child : children) {
                    if(this.isCanceled()) {
                        throw new ConnectionCanceledException();
                    }
                    recursive.putAll(this.compile(move, list, child, new Path(target, child.getName(), child.getType())));
                }
            }
            // Add parent after children
            recursive.put(source, target);
        }
        return recursive;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Renaming {0} to {1}", "Status"),
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
        final MoveWorker that = (MoveWorker) o;
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
        final StringBuilder sb = new StringBuilder("MoveWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
