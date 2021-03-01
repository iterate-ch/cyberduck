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
using ch.cyberduck.core.local;
using System;
using System.IO;
using System.Runtime.InteropServices;
using Ch.Cyberduck.Core.Microsoft.Windows.Sdk;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.PInvoke;

namespace Ch.Cyberduck.Core.Local
{
    public unsafe sealed class ExplorerRevealService : RevealService
    {
        public bool reveal(ch.cyberduck.core.Local l)
        {
            uint psfgaoOut;
            using PIDLIST_ABSOLUTEHandle nativeFolder = new PIDLIST_ABSOLUTEHandle();
            SHParseDisplayName(l.getParent().getAbbreviatedPath(), null, out nativeFolder.Put(), 0, &psfgaoOut);

            if (!nativeFolder)
            {
                return false;
            }

            using PIDLIST_ABSOLUTEHandle nativeFile = new PIDLIST_ABSOLUTEHandle();
            SHParseDisplayName(l.getAbsolute(), null, out nativeFile.Put(), 0, &psfgaoOut);

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
    }
}
