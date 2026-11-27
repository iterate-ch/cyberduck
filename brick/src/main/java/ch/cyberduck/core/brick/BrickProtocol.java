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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;

import com.google.auto.service.AutoService;

@AutoService(Protocol.class)
public class BrickProtocol extends AbstractProtocol {

    @Override
    public Type getType() {
        return Type.brick;
    }

    @Override
    public String getIdentifier() {
        return Type.brick.name();
    }

    @Override
    public String getDescription() {
        return "Files.com";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public Case getCaseSensitivity() {
        return Case.insensitive;
    }

    @Override
    public boolean validate(final Credentials credentials, final LoginOptions options) {
        // Authorize in the browser if no API key is saved.
        return true;
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.storage;
    }
}
