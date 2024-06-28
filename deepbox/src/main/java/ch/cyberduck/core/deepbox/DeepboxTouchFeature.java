package ch.cyberduck.core.deepbox;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Acl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.EnumSet;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANADDCHILDREN;

public class DeepboxTouchFeature extends DefaultTouchFeature<Void> {
    private static final Logger log = LogManager.getLogger(DeepboxTouchFeature.class);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxTouchFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        super(new DeepboxWriteFeature(session, fileid));
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        final Path result = super.touch(file, status);
        return result.withAttributes(new DeepboxAttributesFinderFeature(session, fileid).find(result));
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot() || new DeepboxPathContainerService().isDeepbox(workdir) || new DeepboxPathContainerService().isBox(workdir)) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
        }
        final Acl acl = workdir.attributes().getAcl();
        if(!acl.get(new Acl.CanonicalUser()).contains(CANADDCHILDREN)) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("ACL %s for %s does not include %s", acl, workdir, CANADDCHILDREN));
            }
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
        }
        // prevent duplicates
        if(fileid.getFileId(new Path(workdir, filename, EnumSet.of(AbstractPath.Type.file))) != null) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Target already exists %s/%s", workdir, filename));
            }
            throw new ConflictException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
        }
    }
}
