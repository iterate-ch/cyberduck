package ch.cyberduck.core.storegate;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;

public class StoregateTouchFeature extends DefaultTouchFeature<File> {

    public StoregateTouchFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        super(new StoregateWriteFeature(session, fileid));
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        return !workdir.isRoot();
    }
}
