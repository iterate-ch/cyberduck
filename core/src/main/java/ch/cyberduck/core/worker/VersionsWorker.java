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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Versioning;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Objects;

public class VersionsWorker extends Worker<AttributedList<Path>> {
    private static final Logger log = LogManager.getLogger(VersionsWorker.class);

    private final Path file;
    private final ListProgressListener listener;

    public VersionsWorker(final Path file, final ListProgressListener listener) {
        this.file = file;
        this.listener = listener;
    }

    @Override
    public AttributedList<Path> run(final Session<?> session) throws BackgroundException {
        final Versioning feature = session.getFeature(Versioning.class);
        if(log.isDebugEnabled()) {
            log.debug("Run with feature {}", feature);
        }
        if(feature.getConfiguration(file).isEnabled()) {
            final AttributedList<Path> list = feature.list(file, listener);
            if(list.isEmpty()) {
                listener.chunk(file.getParent(), list);
            }
            return list;
        }
        return AttributedList.emptyList();
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                file.getName());
    }

    @Override
    public AttributedList<Path> initialize() {
        return AttributedList.emptyList();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final VersionsWorker that = (VersionsWorker) o;
        return Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VersionsWorker{");
        sb.append("file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
