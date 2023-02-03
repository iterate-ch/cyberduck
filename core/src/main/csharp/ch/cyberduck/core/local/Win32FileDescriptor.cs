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

using ch.cyberduck.core;
using ch.cyberduck.core.local;
using Windows.Win32.UI.Shell;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.UI.Shell.SHGFI_FLAGS;
namespace Ch.Cyberduck.Core.Local
{
    public sealed class Win32FileDescriptor : AbstractFileDescriptor
    {
        public override string getKind(string filename)
        {
            var extension = AbstractPath.getExtension(filename);
            string kind = null;
            if (Utils.IsBlank(extension))
            {
                kind = this.kind(filename);
                if (Utils.IsBlank(kind))
                {
                    return LocaleFactory.localizedString("Unknown");
                }
                return kind;
            }
            kind = this.kind(AbstractPath.getExtension(filename));
            if (Utils.IsBlank(kind))
            {
                return LocaleFactory.localizedString("Unknown");
            }
            return kind;
        }

        private unsafe string kind(string extension)
        {
            SHFILEINFOW fileInfo = new();
            if (SHGetFileInfo(extension, 0, fileInfo, SHGFI_TYPENAME | SHGFI_USEFILEATTRIBUTES) != 0)
            {
                return fileInfo.szTypeName.ToString();
            }
            return null;
        }
    }
}
