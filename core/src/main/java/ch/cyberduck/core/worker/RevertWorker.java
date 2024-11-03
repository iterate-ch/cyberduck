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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Versioning;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RevertWorker extends Worker<List<Path>> {
    private static final Logger log = LogManager.getLogger(RevertWorker.class);

    private final List<Path> files;

    public RevertWorker(final List<Path> files) {
        this.files = files;
    }

    @Override
    public List<Path> run(final Session<?> session) throws BackgroundException {
        final Versioning feature = session.getFeature(Versioning.class);
        if(log.isDebugEnabled()) {
            log.debug("Run with feature {}", feature);
        }
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            feature.revert(file);
        }
        return files;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Reverting {0}", "Status"),
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
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final RevertWorker that = (RevertWorker) o;
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
        final StringBuilder sb = new StringBuilder("RevertWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
