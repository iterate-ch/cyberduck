package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;

import java.text.MessageFormat;

import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.CREATEDIRECTORIESPERMISSION;

public class CteraDirectoryFeature extends DAVDirectoryFeature {

    private final CteraSession session;

    public CteraDirectoryFeature(final CteraSession session) {
        super(session, new CteraAttributesFinderFeature(session));
        this.session = session;
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(!CteraTouchFeature.validate(filename)) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename));
        }
        // TODO CTERA-136 do we need to require writepermission as well?
        super.preflight(workdir, filename);
        session.checkCteraRole(workdir, CREATEDIRECTORIESPERMISSION);
    }
}
