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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class RenameExistingFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(RenameExistingFilter.class);

    private Move move;

    private UploadFilterOptions options;

    public RenameExistingFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session) {
        this(symlinkResolver, session, new UploadFilterOptions());
    }

    public RenameExistingFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session,
                                final UploadFilterOptions options) {
        super(symlinkResolver, session, options);
        this.move = session.getFeature(Move.class);
        this.options = options;
    }

    /**
     * Rename existing file on server if there is a conflict.
     */
    @Override
    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        if(!options.temporary || local.isDirectory()) {
            // Rename existing file before putting new file in place
            if(status.isExists()) {
                this.rename(file, listener);
            }
        }
    }

    private void rename(final Path file, final ProgressListener listener) throws BackgroundException {
        Path renamed = file;
        while(find.find(renamed)) {
            final String proposal = MessageFormat.format(Preferences.instance().getProperty("queue.upload.file.rename.format"),
                    FilenameUtils.getBaseName(file.getName()),
                    UserDateFormatterFactory.get().getLongFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, ':'),
                    StringUtils.isNotEmpty(file.getExtension()) ? "." + file.getExtension() : StringUtils.EMPTY);
            renamed = new Path(renamed.getParent(), proposal, file.getType());
        }
        if(!renamed.equals(file)) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Rename existing file %s to %s", file, renamed));
            }
            move.move(file, renamed, false, listener);
        }
    }

    @Override
    public void complete(final Path file, final Local local, final TransferOptions options, final TransferStatus status,
                         final ProgressListener listener) throws BackgroundException {
        if(local.isFile()) {
            if(this.options.temporary) {
                // If uploaded with temporary name rename existing file after upload
                // is complete but before temporary upload is renamed
                this.rename(file, listener);
            }
        }
        super.complete(file, local, options, status, listener);
    }
}