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
using org.apache.logging.log4j;
using Windows.Win32;
using static Windows.Win32.CorePInvoke;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Local;

public sealed class ExplorerRevealService : RevealService
{
    private static readonly Logger Log = LogManager.getLogger(typeof(ExplorerRevealService));

    public unsafe bool reveal(CoreLocal l, bool select)
    {
        if (!select)
        {
            return ApplicationLauncherFactory.get().open(l);
        }

        SafeITEMIDLISTHandle selectItem = null;
        SafeITEMIDLISTHandle parent = null;
        try
        {
            if (Shell.ItemIdListFromLocal(l, out selectItem) is { } error)
            {
                if (Log.isDebugEnabled())
                {
                    Log.debug($"Create IdList {l}", error);
                }

                return false;
            }

            if (!Shell.GetParent(ref selectItem, out parent))
            {
                if (Log.isDebugEnabled())
                {
                    Log.debug($"Could not get parent IDL of {l}");
                }

                return false;
            }

            return SHOpenFolderAndSelectItems(parent, selectItem).Succeeded;
        }
        finally
        {
            parent?.Dispose();
            selectItem?.Dispose();
        }
    }

    public bool reveal(CoreLocal file)
    {
        return reveal(file, true);
    }
}
