package ch.cyberduck.core.ctera;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVCopyFeature;
import ch.cyberduck.core.exception.BackgroundException;

import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.*;

class CteraCopyFeature extends DAVCopyFeature {

    public CteraCopyFeature(final CteraSession cteraSession, final CteraSession session) {
        super(cteraSession);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        // TODO CTERA-136 do we require writepermission on target's parent?
        super.preflight(source, target);
        if(source.isDirectory()) {
            checkCteraRole(target.getParent(), CREATEDIRECTORIESPERMISSION);
        }
        else {
            checkCteraRole(target.getParent(), CREATEFILEPERMISSION);
        }
    }
}
