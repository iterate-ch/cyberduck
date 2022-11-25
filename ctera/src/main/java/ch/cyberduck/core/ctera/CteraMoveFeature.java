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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVMoveFeature;

public class CteraMoveFeature extends DAVMoveFeature {

    public CteraMoveFeature(final CteraSession session) {
        super(session);
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(!CteraTouchFeature.validate(target.getName())) {
            return false;
        }
        return super.isSupported(source, target);
    }
}
