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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MoveWorker extends Worker<List<Path>> {

    private final Map<Path, Path> files;

    private final ProgressListener listener;

    private final Cache<Path> cache;

    public MoveWorker(final Map<Path, Path> files, final ProgressListener listener, final Cache<Path> cache) {
        this.files = files;
        this.listener = listener;
        this.cache = cache;
    }

    @Override
    public List<Path> run(final Session<?> session) throws BackgroundException {
        final Move feature = session.getFeature(Move.class);
        for(Map.Entry<Path, Path> entry : files.entrySet()) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            if(!feature.isSupported(entry.getKey(), entry.getValue())) {
                continue;
            }
            final boolean exists;
            if(cache.containsKey(entry.getValue().getParent())) {
                exists = cache.get(entry.getValue().getParent()).contains(entry.getValue());
            }
            else {
                exists = false;
            }
            feature.move(entry.getKey(), entry.getValue(), exists, new Delete.Callback() {
                @Override
                public void delete(final Path file) {
                    listener.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                            file.getName()));
                }
            });
        }
        final List<Path> changed = new ArrayList<Path>();
        changed.addAll(files.keySet());
        changed.addAll(files.values());
        return changed;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Renaming {0} to {1}", "Status"),
                files.keySet().iterator().next().getName(), files.values().iterator().next().getName());
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
