//
// Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
// https://cyberduck.io/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//

using System;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.Local;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Preferences;

/// <summary>
/// Machine-wide support directory shared by all users, i.e. C:\ProgramData\{DataFolderName}
/// </summary>
public class CommonSupportDirectoryFinder : SupportDirectoryFinder
{
    private static readonly Lazy<SystemLocal> local = new(() => new(EnvironmentInfo.CommonAppDataPath, EnvironmentInfo.DataFolderName), true);

    public static SystemLocal Local => local.Value;

    public CoreLocal find() => Local;

    public SupportDirectoryFinder setup()
    {
        return this;
    }
}
