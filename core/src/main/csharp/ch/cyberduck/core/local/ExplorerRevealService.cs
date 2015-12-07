// 
// Copyright (c) 2010-2015 Yves Langisch. All rights reserved.
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
using System.IO;
using ch.cyberduck.core.local;

namespace Ch.Cyberduck.Core.Local
{
    public sealed class ExplorerRevealService : RevealService
    {
        public bool reveal(ch.cyberduck.core.Local l)
        {
            if (l.exists())
            {
                string path = l.getAbsolute();
                DirectoryInfo d = new DirectoryInfo(path);
                string parameter = "";
                if (d.Parent != null)
                {
                    parameter = "/select,";
                }
                return ApplicationLauncherFactory.get()
                    .open(new Application("explorer.exe", null), parameter + path);
            }
            return false;
        }
    }
}