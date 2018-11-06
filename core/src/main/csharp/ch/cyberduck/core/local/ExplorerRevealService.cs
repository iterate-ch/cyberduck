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

namespace Ch.Cyberduck.Core.Local
{
    public sealed class ExplorerRevealService : RevealService
    {
        public bool reveal(ch.cyberduck.core.Local l)
        {
            IntPtr nativeFolder = IntPtr.Zero;
            try
            {
                uint psfgaoOut;
                NativeMethods.SHParseDisplayName(l.getParent().getAbsolute(), IntPtr.Zero, out nativeFolder, 0, out psfgaoOut);

                if (nativeFolder == IntPtr.Zero)
                {
                    return false;
                }

                IntPtr nativeFile = IntPtr.Zero;
                try
                {
                    NativeMethods.SHParseDisplayName(l.getAbsolute(), IntPtr.Zero, out nativeFile, 0, out psfgaoOut);

                    IntPtr[] fileArray;
                    if (nativeFile != IntPtr.Zero)
                    {
                        fileArray = new IntPtr[] { nativeFile };
                    }
                    else
                    {
                        fileArray = new IntPtr[] { };
                    }

                    NativeMethods.SHOpenFolderAndSelectItems(nativeFolder, (uint)fileArray.Length, fileArray, 0);
                }
                finally
                {
                    if (nativeFile != IntPtr.Zero)
                    {
                        Marshal.FreeCoTaskMem(nativeFile);
                    }
                }
            }
            finally
            {
                if (nativeFolder != IntPtr.Zero)
                {
                    Marshal.FreeCoTaskMem(nativeFolder);
                }
            }
            return true;
        }
    }
}
