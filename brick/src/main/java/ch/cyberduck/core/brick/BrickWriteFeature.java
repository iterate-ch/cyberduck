package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVWriteFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultFindFeature;

public class BrickWriteFeature extends DAVWriteFeature {

    private final Find finder;

    public BrickWriteFeature(final DAVSession session) {
        super(session);
        this.finder = new DefaultFindFeature(session);
    }

    public BrickWriteFeature(final DAVSession session, final boolean expect) {
        super(session, expect);
        this.finder = new DefaultFindFeature(session);
    }

    public BrickWriteFeature(final DAVSession session, final Find finder, final AttributesFinder attributes, final boolean expect) {
        super(session, finder, attributes, expect);
        this.finder = finder;
    }

    @Override
    public boolean random() {
        return false;
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.find(file)) {
            return Write.override;
        }
        return Write.notfound;
    }
}
