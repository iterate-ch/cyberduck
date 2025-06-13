﻿//
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
//

using System;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.Local;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Preferences;

public class AssemblyApplicationResourcesFinder : ApplicationResourcesFinder
{
    private static readonly Lazy<SystemLocal> local = new(() => new(EnvironmentInfo.ResourcesLocation), true);

    public CoreLocal find() => local.Value;
}
