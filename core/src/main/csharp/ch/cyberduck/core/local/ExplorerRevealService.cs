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
using System;
using System.Runtime.InteropServices;
using Windows.Win32.UI.Shell;
using Windows.Win32.UI.Shell.Common;
using static Windows.Win32.CorePInvoke;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Local
{
    public sealed class ExplorerRevealService : RevealService
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(ExplorerRevealService).FullName);

        public bool reveal(CoreLocal file) => RevealService.__DefaultMethods.reveal(this, file);

        public unsafe bool reveal(CoreLocal l, bool select)
        {
            if (!select)
            {
                return ApplicationLauncherFactory.get().open(l);
            }


            if (SHCreateItemFromParsingName(l.getAbsolute(), null, typeof(IShellItem).GUID, out var ppv) is { Failed: true, Value: { } parseErrorCode })
            {
                return false;
            }

            IParentAndItem pai;
            try
            {
                pai = (IParentAndItem)ppv;
            }
            catch (Exception e)
            {
                if (Log.isDebugEnabled())
                {
                    Log.debug("Cast IShellitem to IParentAndItem", e);
                }

                return false;
            }

            ITEMIDLIST* parent = null, self = null;
            try
            {
                pai.GetParentAndItem(&parent, out _, &self);
            }
            catch (Exception e)
            {
                if (Log.isDebugEnabled())
                {
                    Log.debug("Get Parent And Item", e);
                }

                return false;
            }

            try
            {
                if (SHOpenFolderAndSelectItems(*parent, 1, &self, 0) is { Failed: true, Value: { } hr })
                {
                    if (Log.isDebugEnabled())
                    {
                        Log.debug("OpenFolderAndSelectItems", Marshal.GetExceptionForHR(hr));
                    }

                    return false;
                }

                return true;
            }
            finally
            {
                Marshal.FreeCoTaskMem((nint)self);
                Marshal.FreeCoTaskMem((nint)parent);
            }
        }
    }
}
