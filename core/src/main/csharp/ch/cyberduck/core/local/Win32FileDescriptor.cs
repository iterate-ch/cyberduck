// 
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
using System.Runtime.InteropServices;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using org.apache.commons.io;

namespace Ch.Cyberduck.Core.Local
{
    public sealed class Win32FileDescriptor : AbstractFileDescriptor
    {
        public override string getKind(string filename)
        {
            String extension = FilenameUtils.getExtension(filename);
            String kind = null;
            if (Utils.IsBlank(extension))
            {
                kind = this.kind(filename);
                if (Utils.IsBlank(kind))
                {
                    return LocaleFactory.localizedString("Unknown");
                }
                return kind;
            }
            kind = this.kind(FilenameUtils.getExtension(filename));
            if (Utils.IsBlank(kind))
            {
                return LocaleFactory.localizedString("Unknown");
            }
            return kind;
        }

        private string kind(string extension)
        {
            Shell32.SHFILEINFO shinfo = new Shell32.SHFILEINFO();
            IntPtr hSuccess = Shell32.SHGetFileInfo(extension, 0, ref shinfo, (uint) Marshal.SizeOf(shinfo),
                                                    Shell32.SHGFI_TYPENAME | Shell32.SHGFI_USEFILEATTRIBUTES);
            if (hSuccess != IntPtr.Zero)
            {
                return Convert.ToString(shinfo.szTypeName.Trim());
            }
            return null;
        }
    }
}