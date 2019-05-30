package ch.cyberduck.core.nextcloud;

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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.dav.DAVSSLProtocol;

public class NextcloudProtocol extends AbstractProtocol {

    @Override
    public Type getType() {
        return Type.nextcloud;
    }

    @Override
    public String getIdentifier() {
        return Type.nextcloud.name();
    }

    @Override
    public String getDescription() {
        return "NextCloud";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String disk() {
        return new DAVSSLProtocol().disk();
    }

    @Override
    public String icon() {
        return new DAVSSLProtocol().icon();
    }
}
