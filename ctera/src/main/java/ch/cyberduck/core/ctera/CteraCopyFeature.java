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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVCopyFeature;
import ch.cyberduck.core.exception.BackgroundException;

import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.*;

public class CteraCopyFeature extends DAVCopyFeature {

    public CteraCopyFeature(final CteraSession session) {
        super(session);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        super.preflight(source, target);
        if((source.getParent().attributes().getAcl() != Acl.EMPTY) && (target.getParent().attributes().getAcl() != Acl.EMPTY)) {
            if(source.isDirectory()) {
                checkCteraRole(target.getParent(), CREATEDIRECTORIESPERMISSION);
            }
            else {
                checkCteraRole(target.getParent(), CREATEFILEPERMISSION);
            }
        }
    }
}
