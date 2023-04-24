// 
// Copyright (c) 2010-2022 Yves Langisch. All rights reserved.
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

using ch.cyberduck.core.local;
using Windows.Win32.UI.Shell.Common;
using static Windows.Win32.CorePInvoke;

namespace Ch.Cyberduck.Core.Local
{
    public sealed class ExplorerRevealService : RevealService
    {
        public unsafe bool reveal(ch.cyberduck.core.Local l, bool select)
        {
            if (!select)
            {
                return ApplicationLauncherFactory.get().open(l);
            }

            using PIDLIST_ABSOLUTEHandle nativeFolder = ILCreateFromPath2(l.getParent().getAbsolute());
            if (!nativeFolder)
            {
                return false;
            }

            using PIDLIST_ABSOLUTEHandle nativeFile = ILCreateFromPath2(l.getAbsolute());
            if (!nativeFile)
            {
                return false;
            }

            uint count = 0;
            ITEMIDLIST* target = default;
            if (nativeFile)
            {
                count = 1;
                target = nativeFile.Pointer;
            }
            SHOpenFolderAndSelectItems(nativeFolder.Value, count, &target, 0);
            return true;
        }

        public bool reveal(ch.cyberduck.core.Local file)
        {
            return reveal(file, true);
        }
    }
}
