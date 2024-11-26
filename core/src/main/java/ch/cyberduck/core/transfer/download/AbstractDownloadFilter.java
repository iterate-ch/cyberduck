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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.local.ApplicationLauncher;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.download.features.DefaultDownloadOptionsFilterChain;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public abstract class AbstractDownloadFilter implements TransferPathFilter {
    private static final Logger log = LogManager.getLogger(AbstractDownloadFilter.class);

    private final ApplicationLauncher launcher = ApplicationLauncherFactory.get();
    private final SymlinkResolver<Path> resolver;
    protected final AttributesFinder attribute;
    protected final FeatureFilter chain;

    public AbstractDownloadFilter(final SymlinkResolver<Path> resolver, final Session<?> session, final DownloadFilterOptions options) {
        this(resolver, session, session.getFeature(AttributesFinder.class), options);
    }

    public AbstractDownloadFilter(final SymlinkResolver<Path> resolver, final Session<?> session, final AttributesFinder attribute, final DownloadFilterOptions options) {
        this.resolver = resolver;
        this.attribute = attribute;
        this.chain = new DefaultDownloadOptionsFilterChain(session, options);
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        final Local volume = local.getVolume();
        if(!volume.exists()) {
            throw new NotfoundException(String.format("Volume %s not mounted", volume.getAbsolute()));
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        log.debug("Prepare {}", file);
        final TransferStatus status = new TransferStatus();
        if(parent.isExists()) {
            if(local.exists()) {
                if(file.getType().contains(Path.Type.file)) {
                    if(local.isDirectory()) {
                        throw new LocalAccessDeniedException(String.format("Cannot replace folder %s with file %s", local.getAbbreviatedPath(), file.getName()));
                    }
                }
                if(file.getType().contains(Path.Type.directory)) {
                    if(local.isFile()) {
                        throw new LocalAccessDeniedException(String.format("Cannot replace file %s with folder %s", local.getAbbreviatedPath(), file.getName()));
                    }
                }
                status.setExists(true);
            }
        }
        final PathAttributes attributes;
        if(file.isSymbolicLink()) {
            // A server will resolve the symbolic link when the file is requested.
            final Path target = file.getSymlinkTarget();
            // Read remote attributes of symlink target
            attributes = attribute.find(target);
            if(!resolver.resolve(file)) {
                if(file.isFile()) {
                    // Content length
                    status.setLength(attributes.getSize());
                }
            }
            // No file size increase for symbolic link to be created locally
        }
        else {
            // Read remote attributes
            attributes = attribute.find(file);
            if(file.isFile()) {
                // Content length
                status.setLength(attributes.getSize());
            }
        }
        status.setRemote(attributes);
        return chain.prepare(file, Optional.of(local), status, progress);
    }

    @Override
    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        chain.apply(file, status, progress);
    }

    /**
     * Update timestamp and permission
     */
    @Override
    public void complete(final Path file, final Local local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        log.debug("Complete {} with status {}", file.getAbsolute(), status);
        if(status.isSegment()) {
            log.debug("Skip completion for single segment {}", status);
            return;
        }
        if(status.isComplete()) {
            chain.complete(file, Optional.of(local), status, progress);
            if(file.isFile()) {
                // Bounce Downloads folder dock icon by sending download finished notification
                launcher.bounce(local);
            }
        }
    }
}
