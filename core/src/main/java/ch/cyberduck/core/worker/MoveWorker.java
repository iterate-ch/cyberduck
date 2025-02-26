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
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultVersioningFeature;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.comparator.VersionsComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MoveWorker extends Worker<Map<Path, Path>> {
    private static final Logger log = LogManager.getLogger(MoveWorker.class);

    private final Map<Path, Path> files;
    private final SessionPool target;
    private final Cache<Path> cache;
    private final ProgressListener listener;
    private final ConnectionCallback callback;

    public MoveWorker(final Map<Path, Path> files, final SessionPool target, final Cache<Path> cache, final ProgressListener listener, final ConnectionCallback callback) {
        this.files = files;
        this.target = target;
        this.cache = cache;
        this.listener = listener;
        this.callback = callback;
    }

    @Override
    public Map<Path, Path> run(final Session<?> session) throws BackgroundException {
        final Session<?> destination = target.borrow(new BackgroundActionState() {
            @Override
            public boolean isCanceled() {
                return MoveWorker.this.isCanceled();
            }

            @Override
            public boolean isRunning() {
                return true;
            }
        });
        try {
            final Move feature = session.getFeature(Move.class).withTarget(destination);
            log.debug("Run with feature {}", feature);
            final ListService list = session.getFeature(ListService.class);
            // Sort ascending by timestamp to move older versions first
            final Map<Path, Path> sorted = new TreeMap<>(new VersionsComparator(true));
            sorted.putAll(files);
            final Map<Path, Path> result = new HashMap<>();
            for(Map.Entry<Path, Path> entry : sorted.entrySet()) {
                if(this.isCanceled()) {
                    throw new ConnectionCanceledException();
                }
                final Map<Path, Path> recursive = this.compile(feature, list, entry.getKey(), entry.getValue());
                log.debug("Compiled recursive list {}", recursive);
                for(Map.Entry<Path, Path> r : recursive.entrySet()) {
                    if(r.getKey().isDirectory() && !feature.isRecursive(r.getKey(), r.getValue())) {
                        log.warn("Move operation is not recursive. Create directory {}", r.getValue());
                        // Create directory unless copy implementation is recursive
                        result.put(r.getKey(), session.getFeature(Directory.class).mkdir(r.getValue(),
                                new TransferStatus().withLength(0L).withRegion(r.getKey().attributes().getRegion())));
                    }
                    else {
                        final TransferStatus status = new TransferStatus()
                                .withLockId(this.getLockId(r.getKey()))
                                .withMime(new MappingMimeTypeService().getMime(r.getValue().getName()))
                                .withAcl(r.getKey().attributes().getAcl())
                                .withPermission(r.getKey().attributes().getPermission())
                                .withEncryption(r.getKey().attributes().getEncryption())
                                .withStorageClass(r.getKey().attributes().getStorageClass())
                                .exists(new CachingFindFeature(session, cache).find(r.getValue()))
                                .withLength(r.getKey().attributes().getSize());
                        if(status.isExists()) {
                            status.withRemote(new CachingAttributesFinderFeature(session, cache, session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session))).find(r.getValue()));
                        }
                        final Delete.Callback delete = new Delete.Callback() {
                            @Override
                            public void delete(final Path file) {
                                listener.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                                        file.getName()));
                            }
                        };
                        final Path moved = feature.move(r.getKey(), r.getValue(), status, delete, callback);
                        if(PathAttributes.EMPTY.equals(moved.attributes())) {
                            moved.withAttributes(session.getFeature(AttributesFinder.class).find(moved));
                        }
                        result.put(r.getKey(), moved);
                        final HostPreferences preferences = HostPreferencesFactory.get(session.getHost());
                        if(preferences.getBoolean("versioning.enable") && preferences.getBoolean("versioning.move.enable")) {
                            switch(session.getHost().getProtocol().getVersioningMode()) {
                                case custom:
                                    // Move previous versions of file
                                    final Versioning versioning = session.getFeature(Versioning.class);
                                    if(versioning != null) {
                                        if(versioning.getConfiguration(r.getKey()).isEnabled()) {
                                            log.debug("List previous versions of {}", r.getKey());
                                            for(Path version : versioning.list(r.getKey(), new DisabledListProgressListener())) {
                                                final Path target = new Path(new DefaultVersioningFeature.DefaultVersioningDirectoryProvider().provide(r.getValue()),
                                                        version.getName(), version.getType());
                                                final Path directory = target.getParent();
                                                if(!new CachingFindFeature(session, cache).find(directory)) {
                                                    log.debug("Create directory {} for versions", directory);
                                                    session.getFeature(Directory.class).mkdir(directory, new TransferStatus());
                                                }
                                                if(version.isDirectory()) {
                                                    if(!session.getFeature(Move.class).isRecursive(version, target)) {
                                                        log.warn("Skip directory {}", version);
                                                        continue;
                                                    }
                                                }
                                                log.debug("Move previous version {} to {}", version, target);
                                                feature.move(version, target, new TransferStatus()
                                                        .withLockId(this.getLockId(version))
                                                        .withMime(new MappingMimeTypeService().getMime(version.getName()))
                                                        .exists(new CachingFindFeature(session, cache).find(target))
                                                        .withLength(version.attributes().getSize()), delete, callback);
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
                // Find previous folders to be deleted
                final List<Path> folders = recursive.entrySet().stream()
                        .filter(f -> !feature.isRecursive(f.getKey(), f.getValue()))
                        .collect(Collectors.toCollection(ArrayList::new)).stream()
                        .map(Map.Entry::getKey).filter(Path::isDirectory)
                        .collect(Collectors.toCollection(ArrayList::new));
                if(!folders.isEmpty()) {
                    // Must delete inverse
                    Collections.reverse(folders);
                    final Delete delete = session.getFeature(Delete.class);
                    for(Path folder : folders) {
                        log.warn("Delete source directory {}", folder);
                        final TransferStatus status = new TransferStatus().withLockId(this.getLockId(folder));
                        delete.delete(Collections.singletonMap(folder, status), callback, new Delete.DisabledCallback());
                    }
                }
            }
            return result;
        }
        finally {
            target.release(destination, null);
        }
    }

    protected String getLockId(final Path file) {
        return null;
    }

    protected Map<Path, Path> compile(final Move move, final ListService list, final Path source, final Path target) throws BackgroundException {
        // Compile recursive list
        final Map<Path, Path> recursive = new LinkedHashMap<>();
        recursive.put(source, target);
        if(source.isDirectory()) {
            if(!move.isRecursive(source, target)) {
                // sort ascending by timestamp to move older versions first
                final AttributedList<Path> children = list.list(source, new WorkerListProgressListener(this, listener))
                        .filter(new VersionsComparator(true));
                for(Path child : children) {
                    if(this.isCanceled()) {
                        throw new ConnectionCanceledException();
                    }
                    recursive.putAll(this.compile(move, list, child, new Path(target, child.getName(), child.getType())));
                }
            }
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
        final StringBuilder sb = new StringBuilder("MoveWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
