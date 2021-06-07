// Copyright (c) 2019. All rights reserved. http://cyberduck.io/
//
// This program is free software; you can redistribute it and/or modify it under the terms of the GNU
// General Public License as published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
// even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// General Public License for more details.
//
// Bug fixes, suggestions and comments should be sent to: feedback@cyberduck.io
using Ch.Cyberduck.Core.Microsoft.Windows.Sdk;
using System.Drawing;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.FILE_FLAGS_AND_ATTRIBUTES;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.PInvoke;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.SHGFI_FLAGS;

namespace Ch.Cyberduck.Ui.Core.Resources
{
    partial class IconCache
    {
        /// <summary>
        /// Options to specify whether folders should be in the open or closed state.
        /// </summary>
        private enum FolderType
        {
            /// <summary>
            /// Specify open folder.
            /// </summary>
            Open = 0,

            /// <summary>
            /// Specify closed folder.
            /// </summary>
            Closed = 1
        }

        private static Icon CloneIcon(in HICON fileInfo)
        {
            using var temp = Icon.FromHandle(fileInfo.Value);
            return (Icon)temp.Clone();
        }

        private static Icon GetFileIconFromExecutable(string filename, IconSize size)
        {
            SHFILEINFOW shfi = new SHFILEINFOW();
            SHGFI_FLAGS flags = SHGFI_ICON | SHGFI_USEFILEATTRIBUTES;
            if (IconSize.Small == size)
            {
                flags |= SHGFI_SMALLICON;
            }
            else
            {
                flags |= SHGFI_LARGEICON;
            }

            if (SHGetFileInfo(filename, FILE_ATTRIBUTE_NORMAL, shfi, flags) == 0)
            {
                return null;
            }
            using (shfi.hIcon)
            {
                return CloneIcon(shfi.hIcon);
            }
        }

        private static Icon GetFileIconFromName(string filename, bool isFolder, IconSize size, bool linkOverlay)
        {
            SHFILEINFOW shfi = new SHFILEINFOW();
            SHGFI_FLAGS flags = SHGFI_ICON | SHGFI_USEFILEATTRIBUTES;

            if (linkOverlay)
            {
                flags |= SHGFI_LINKOVERLAY;
            }

            if (IconSize.Small == size)
            {
                flags |= SHGFI_SMALLICON;
            }
            else
            {
                flags |= SHGFI_LARGEICON;
            }

            FILE_FLAGS_AND_ATTRIBUTES fileAttributes;
            if (isFolder)
            {
                fileAttributes = FILE_ATTRIBUTE_DIRECTORY;
            }
            else
            {
                fileAttributes = FILE_ATTRIBUTE_NORMAL;
            }

            if (SHGetFileInfo(filename, fileAttributes, shfi, flags) == 0)
            {
                return null;
            }
            using (shfi.hIcon)
            {
                return CloneIcon(shfi.hIcon);
            }
        }

        /// <summary>
        /// Used to access system folder icons.
        /// </summary>
        /// <param name="size">Specify large or small icons.</param>
        /// <param name="folderType">Specify open or closed FolderType.</param>
        /// <returns>System.Drawing.Icon</returns>
        private static Icon GetFolderIcon(IconSize size, FolderType folderType)
        {
            SHFILEINFOW shfi = new SHFILEINFOW();
            // Need to add size check, although errors generated at present!
            SHGFI_FLAGS flags = SHGFI_ICON | SHGFI_USEFILEATTRIBUTES;

            if (FolderType.Open == folderType)
            {
                flags |= SHGFI_OPENICON;
            }

            if (IconSize.Small == size)
            {
                flags |= SHGFI_SMALLICON;
            }
            else
            {
                flags |= SHGFI_LARGEICON;
            }

            // Get the folder icon

            nuint hSuccess = SHGetFileInfo("_unknown", FILE_ATTRIBUTE_DIRECTORY, shfi, flags);
            if (hSuccess == 0)
            {
                return null;
            }

            using (shfi.hIcon)
            {
                return CloneIcon(shfi.hIcon);
            }
        }
    }
}
