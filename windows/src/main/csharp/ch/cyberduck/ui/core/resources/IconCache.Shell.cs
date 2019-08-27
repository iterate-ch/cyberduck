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
using System;
using System.Drawing;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Ui.Core.Resources
{
    using Ch.Cyberduck.Core;

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

        private static Icon CloneIcon(Shell32.SHFILEINFO fileInfo)
        {
            Icon icon = default;
            try
            {
                using (var temp = Icon.FromHandle(fileInfo.hIcon))
                {
                    icon = (Icon)temp.Clone();
                }
            }
            catch { }
            finally
            {
                User32.DestroyIcon(fileInfo.hIcon);
            }
            return icon;
        }

        /// <summary>
        /// Used to access system folder icons.
        /// </summary>
        /// <param name="size">Specify large or small icons.</param>
        /// <param name="folderType">Specify open or closed FolderType.</param>
        /// <returns>System.Drawing.Icon</returns>
        private static Icon GetFolderIcon(IconSize size, FolderType folderType)
        {
            // Need to add size check, although errors generated at present!
            uint flags = Shell32.SHGFI_ICON | Shell32.SHGFI_USEFILEATTRIBUTES;

            if (FolderType.Open == folderType)
            {
                flags |= Shell32.SHGFI_OPENICON;
            }

            if (IconSize.Small == size)
            {
                flags |= Shell32.SHGFI_SMALLICON;
            }
            else
            {
                flags |= Shell32.SHGFI_LARGEICON;
            }

            // Get the folder icon
            Shell32.SHFILEINFO shfi = new Shell32.SHFILEINFO();
            IntPtr hSuccess = Shell32.SHGetFileInfo("_unknown", Shell32.FILE_ATTRIBUTE_DIRECTORY, ref shfi,
                (uint)Marshal.SizeOf(shfi), flags);
            if (hSuccess == IntPtr.Zero)
            {
                return null;
            }

            return CloneIcon(shfi);
        }

        private static Icon GetFileIconFromName(string filename, bool isFolder, IconSize size, bool linkOverlay)
        {
            Shell32.SHFILEINFO shfi = new Shell32.SHFILEINFO();
            uint flags = Shell32.SHGFI_ICON | Shell32.SHGFI_USEFILEATTRIBUTES;

            if (linkOverlay)
            {
                flags |= Shell32.SHGFI_LINKOVERLAY;
            }

            if (IconSize.Small == size)
            {
                flags |= Shell32.SHGFI_SMALLICON;
            }
            else
            {
                flags |= Shell32.SHGFI_LARGEICON;
            }

            uint fileAttributes;
            if (isFolder)
            {
                fileAttributes = Shell32.FILE_ATTRIBUTE_DIRECTORY;
            }
            else
            {
                fileAttributes = Shell32.FILE_ATTRIBUTE_NORMAL;
            }

            IntPtr hSuccess = Shell32.SHGetFileInfo(
                filename, fileAttributes,
                ref shfi, (uint)Marshal.SizeOf(shfi),
                flags);
            if (hSuccess == IntPtr.Zero)
            {
                return null;
            }

            return CloneIcon(shfi);
        }

        private static Icon GetFileIconFromExecutable(string filename, IconSize size)
        {
            Shell32.SHFILEINFO shfi = new Shell32.SHFILEINFO();
            uint flags = Shell32.SHGFI_ICON | Shell32.SHGFI_USEFILEATTRIBUTES;
            if (IconSize.Small == size)
            {
                flags += Shell32.SHGFI_SMALLICON;
            }
            else
            {
                flags += Shell32.SHGFI_LARGEICON;
            }
            IntPtr hSuccess = Shell32.SHGetFileInfo(filename, Shell32.FILE_ATTRIBUTE_NORMAL, ref shfi,
                (uint)Marshal.SizeOf(shfi), flags);
            if (hSuccess == IntPtr.Zero)
            {
                return null;
            }

            return CloneIcon(shfi);
        }
    }
}
