package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Protocol;

import com.google.auto.service.AutoService;

@AutoService(Protocol.class)
public class OneDriveProtocol extends GraphProtocol {
    @Override
    public String getIdentifier() {
        return "onedrive";
    }

    @Override
    public String getDescription() {
        return "Microsoft OneDrive";
    }

    @Override
    public String getName() {
        return "OneDrive";
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", OneDriveProtocol.class.getPackage().getName(), "OneDrive");
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.storage;
    }

    @Override
    public Case getCaseSensitivity() {
        return Case.insensitive;
    }
}
