package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

public class SharepointSiteProtocol extends GraphProtocol {
    @Override
    public String getIdentifier() {
        return "sharepoint-site";
    }

    @Override
    public String getDescription() {
        return "SharePoint Online Site";
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public String getName() {
        return "Sharepoint Site";
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", SharepointProtocol.class.getPackage().getName(), "SharepointSite");
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.storage;
    }
}
