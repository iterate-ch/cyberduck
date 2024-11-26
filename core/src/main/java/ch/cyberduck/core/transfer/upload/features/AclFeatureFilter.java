package ch.cyberduck.core.transfer.upload.features;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Optional;

public class AclFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(AclFeatureFilter.class);

    private final Session<?> session;

    public AclFeatureFilter(final Session<?> session) {
        this.session = session;
    }

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        final AclPermission feature = session.getFeature(AclPermission.class);
        if(feature != null) {
            if(status.isExists()) {
                progress.message(MessageFormat.format(LocaleFactory.localizedString("Getting permission of {0}", "Status"),
                        file.getName()));
                try {
                    status.setAcl(feature.getPermission(file));
                }
                catch(NotfoundException | AccessDeniedException | InteroperabilityException e) {
                    status.setAcl(feature.getDefault(file));
                }
            }
            else {
                status.setAcl(feature.getDefault(file));
            }
        }
        return status;
    }

    @Override
    public void complete(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(!Acl.EMPTY.equals(status.getAcl())) {
            final AclPermission feature = session.getFeature(AclPermission.class);
            if(feature != null) {
                try {
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                            file.getName(), StringUtils.isBlank(status.getAcl().getCannedString()) ? LocaleFactory.localizedString("Unknown") : status.getAcl().getCannedString()));
                    feature.setPermission(file, status);
                }
                catch(BackgroundException e) {
                    // Ignore
                    log.warn(e.getMessage());
                }
            }
        }
    }
}
