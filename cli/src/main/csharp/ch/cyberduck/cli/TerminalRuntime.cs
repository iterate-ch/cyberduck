//
// Copyright (c) 2010-2017 Yves Langisch. All rights reserved.
// http://cyberduck.io/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// Bug fixes, suggestions and comments should be sent to:
// feedback@cyberduck.io
//

using Ch.Cyberduck.Core.Preferences;
using System;

namespace Ch.Cyberduck.Cli
{
    public record class TerminalRuntime(
        string CompanyName,
        string DataFolderName,
        string Location,
        bool Packaged,
        string ProductName,
        string ResourcesLocation,
        Version Version) : IRuntime
    {
        public string CompanyName { get; } = CompanyName;

        public string DataFolderName { get; } = DataFolderName;

        public string Location { get; } = Location;

        public string ProductName { get; } = ProductName;

        public string ResourcesLocation { get; } = ResourcesLocation;

        public bool Packaged { get; } = Packaged;

        public string Revision { get; } = Version.Revision.ToString();

        public Version Version { get; } = Version;

        public string VersionString { get; } = Version.ToString(3);

        public TerminalRuntime() : this(Runtime.CreateDefault<TerminalRuntime>())
        {
        }

        public TerminalRuntime(in Runtime.ValueRuntime runtime) : this(
            runtime.CompanyName,
            "Cyberduck",
            runtime.Location,
            runtime.Packaged,
            runtime.ProductName,
            runtime.ResourcesLocation,
            runtime.Version)
        {
        }
    }
}
