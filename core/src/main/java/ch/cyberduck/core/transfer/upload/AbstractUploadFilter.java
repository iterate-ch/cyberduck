package ch.cyberduck.core.transfer.upload;

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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalNotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;
import ch.cyberduck.core.transfer.upload.features.DefaultLocalUploadOptionsFilterChain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.Optional;

public abstract class AbstractUploadFilter implements TransferPathFilter {
    private static final Logger log = LogManager.getLogger(AbstractUploadFilter.class);

    private final SymlinkResolver<Local> resolver;
    protected final Find find;
    protected final AttributesFinder attribute;
    protected final FeatureFilter chain;

    public AbstractUploadFilter(final SymlinkResolver<Local> resolver, final Session<?> session, final UploadFilterOptions options) {
        this(resolver, session, session.getFeature(Find.class), session.getFeature(AttributesFinder.class), options);
    }

    public AbstractUploadFilter(final SymlinkResolver<Local> resolver, final Session<?> session, final Find find, final AttributesFinder attribute, final UploadFilterOptions options) {
        this(resolver, find, attribute, new DefaultLocalUploadOptionsFilterChain(session, options));
    }

    public AbstractUploadFilter(final SymlinkResolver<Local> resolver, final Find find, final AttributesFinder attribute, final FeatureFilter chain) {
        this.resolver = resolver;
        this.find = find;
        this.attribute = attribute;
        this.chain = chain;
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        if(!local.exists()) {
            // Local file is no more here
            throw new LocalNotfoundException(local.getAbsolute());
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        log.debug("Prepare {}", file);
        final TransferStatus status = new TransferStatus().withLockId(parent.getLockId());
        if(file.isFile()) {
            // Set content length from local file
            if(local.isSymbolicLink()) {
                if(!resolver.resolve(local)) {
                    // Will resolve the symbolic link when the file is requested.
                    final Local target = local.getSymlinkTarget();
                    status.setLength(target.attributes().getSize());
                }
                // No file size increase for symbolic link to be created on the server
            }
            else {
                // Read file size from filesystem
                status.setLength(local.attributes().getSize());
            }
        }
        if(file.isDirectory()) {
            status.setLength(0L);
        }
        // Read remote attributes first
        if(parent.isExists()) {
            if(find.find(file)) {
                status.setExists(true);
                // Read remote attributes
                status.setRemote(attribute.find(file));
            }
            else {
                // Look if there is directory or file that clashes with this upload
                if(file.getType().contains(Path.Type.file)) {
                    if(find.find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory)))) {
                        throw new AccessDeniedException(String.format("Cannot replace folder %s with file %s", file.getAbsolute(), file.getName()));
                    }
                }
                if(file.getType().contains(Path.Type.directory)) {
                    if(find.find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.file)))) {
                        throw new AccessDeniedException(String.format("Cannot replace file %s with folder %s", file.getAbsolute(), file.getName()));
                    }
                }
            }
        }
        return chain.prepare(file, Optional.of(local), status, progress);
    }

    @Override
    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        chain.apply(file, status, progress);
    }

    @Override
    public void complete(final Path file, final Local local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        log.debug("Complete {} with status {}", file.getAbsolute(), status);
        if(status.isComplete()) {
            chain.complete(file, Optional.empty(), status, progress);
        }
    }
}