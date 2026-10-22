package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.dav.DAVMoveFeature;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * Deny renaming virtual folders and root folder of spaces and shares. Server side move is not supported between spaces.
 */
public class OwncloudSpacesMoveFeature extends DAVMoveFeature {

    public OwncloudSpacesMoveFeature(final DAVSession session) {
        super(session);
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        if(OwncloudSpaces.isVirtual(source) || OwncloudSpaces.isContainer(source)) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString(
                    "Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
        if(target.isPresent()) {
            if(OwncloudSpaces.isVirtual(target.get().getParent())) {
                throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString(
                        "Cannot rename {0}", "Error"), source.getName())).withFile(source);
            }
            if(!new SimplePathPredicate(OwncloudSpaces.getContainer(source)).test(OwncloudSpaces.getContainer(target.get()))) {
                throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString(
                        "Cannot rename {0}", "Error"), source.getName())).withFile(source);
            }
        }
        super.preflight(source, target);
    }
}
