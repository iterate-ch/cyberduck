package ch.cyberduck.core.transfer.download;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.synchronization.ComparePathFilter;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.DefaultComparePathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompareFilter extends AbstractDownloadFilter {
    private static final Logger log = LogManager.getLogger(CompareFilter.class);

    private final ComparePathFilter comparison;

    public CompareFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session) {
        this(symlinkResolver, session, new DownloadFilterOptions(session.getHost()));
    }

    public CompareFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session, final Find find, final AttributesFinder attribute) {
        this(symlinkResolver, session, find, attribute, new DefaultComparePathFilter(session, find, attribute), new DownloadFilterOptions(session.getHost()));
    }

    public CompareFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session, final DownloadFilterOptions options) {
        this(symlinkResolver, session, new DefaultComparePathFilter(session), options);
    }

    public CompareFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session, final ComparePathFilter comparison, final DownloadFilterOptions options) {
        this(symlinkResolver, session, session.getFeature(Find.class), session.getFeature(AttributesFinder.class), comparison, options);
    }

    public CompareFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session, final Find find, final AttributesFinder attribute, final ComparePathFilter comparison, final DownloadFilterOptions options) {
        super(symlinkResolver, session, attribute, options);
        this.comparison = comparison;
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        if(super.accept(file, local, parent, progress)) {
            final Comparison result = comparison.compare(file, local, progress);
            switch(result) {
                case local:
                    log.info("Skip file {} with result {}", file, result);
                    return false;
                case equal:
                    if(file.isDirectory()) {
                        return true;
                    }
                    log.info("Skip file {} with result {}", file, result);
                    return false;
                case remote:
                    return true;
            }
            log.warn("Invalid result result {}", result);
        }
        return false;
    }
}
