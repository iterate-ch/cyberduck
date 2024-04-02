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
import ch.cyberduck.core.exception.NotfoundException;

import static ch.cyberduck.core.ctera.CteraAttributesFinderFeature.*;

public class CteraCopyFeature extends DAVCopyFeature {

    private final CteraAttributesFinderFeature attributes;

    public CteraCopyFeature(final CteraSession session, final CteraAttributesFinderFeature attributes) {
        super(session);
        this.attributes = attributes;
    }

    public CteraCopyFeature(final CteraSession session) {
        super(session);
        this.attributes = new CteraAttributesFinderFeature(session);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        boolean targetExists = true;
        try {
            attributes.find(target);
        }
        catch(NotfoundException e) {
            targetExists = false;
        }
        if(targetExists) {
            assumeRole(target, WRITEPERMISSION);
        }
        // no createfilespermission required for now
        if(source.isDirectory()) {
            assumeRole(target.getParent(), target.getName(), CREATEDIRECTORIESPERMISSION);
        }
    }
}
