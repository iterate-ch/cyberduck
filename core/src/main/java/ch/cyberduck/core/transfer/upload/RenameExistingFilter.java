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
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

public class RenameExistingFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(RenameExistingFilter.class);

    private Move move;

    public RenameExistingFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session) {
        this(symlinkResolver, session, new UploadFilterOptions());
    }

    public RenameExistingFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session,
                                final UploadFilterOptions options) {
        super(symlinkResolver, session, options);
        this.move = session.getFeature(Move.class);
    }

    /**
     * Rename existing file on server if there is a conflict.
     */
    @Override
    public void apply(final Path file, final Local local, final TransferStatus status,
                      final ProgressListener listener) throws BackgroundException {
        // Rename existing file before putting new file in place
        if(status.isExists()) {
            Path rename;
            do {
                final String proposal = MessageFormat.format(PreferencesFactory.get().getProperty("queue.upload.file.rename.format"),
                        FilenameUtils.getBaseName(file.getName()),
                        UserDateFormatterFactory.get().getMediumFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, '-').replace(':', '-'),
                        StringUtils.isNotEmpty(file.getExtension()) ? "." + file.getExtension() : StringUtils.EMPTY);
                rename = new Path(file.getParent(), proposal, file.getType());
            }
            while(find.find(rename));
            if(log.isInfoEnabled()) {
                log.info(String.format("Rename existing file %s to %s", file, rename));
            }
            move.move(file, rename, false, new Delete.DisabledCallback());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Clear exist flag for file %s", file));
            }
            status.setExists(false);
        }
        super.apply(file, local, status, listener);
    }
}