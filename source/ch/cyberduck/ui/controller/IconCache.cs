// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
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
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Collections;
using org.apache.log4j;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    /// <summary>
    /// Provides static methods to read images for both folders and files. Does not provide any caching actually.
    /// </summary>
    public sealed class IconCache
    {
        /// <summary>
        /// Options to specify the size of icons to return.
        /// </summary>
        public enum IconSize
        {
            /// <summary>
            /// Specify large icon - 32 pixels by 32 pixels.
            /// </summary>
            Large = 0,
            /// <summary>
            /// Specify small icon - 16 pixels by 16 pixels.
            /// </summary>
            Small = 1
        }

        private static readonly Logger Log = Logger.getLogger(typeof (IconCache).Name);

        private static readonly bool OverlayFolderImage
            = Preferences.instance().getBoolean("browser.markInaccessibleFolders");

        private static readonly IconCache instance = new IconCache();

        private readonly TypedLRUCache<Bitmap> _bitmapCache =
            new TypedLRUCache<Bitmap>(Preferences.instance().getInteger("icon.cache.size"));

        /// <summary>
        /// Cache limited to n entries
        /// </summary>
        private readonly TypedLRUCache<Icon> _iconCache =
            new TypedLRUCache<Icon>(Preferences.instance().getInteger("icon.cache.size"));

        private readonly Dictionary<int, ImageList> _protocolImages = new Dictionary<int, ImageList>();

        /// <summary>
        /// 16x16 protocol icons
        /// </summary>
        private ImageList _protocolIcons;

        private IconCache()
        {
        }

        public static IconCache Instance
        {
            get { return instance; }
        }

        private Bitmap OverlayImages(Bitmap original, Bitmap overlay)
        {
            Image cloned = (Image) original.Clone();
            Graphics gra = Graphics.FromImage(cloned);
            gra.DrawImage(overlay, new Point(0, 0));
            return (Bitmap) cloned;
        }

        /// <summary>
        /// Return a bitmap for a given path
        /// </summary>
        /// <param name="path"></param>
        /// <returns></returns>
        public Bitmap IconForPath(Path path, IconSize size)
        {
            if (path.attributes().isSymbolicLink())
            {
                Bitmap overlay = IconForName("aliasbadge", size);
                if (path.attributes().isDirectory())
                {
                    return IconForFolder(overlay, size);
                }
                Bitmap symlink = IconForFilename(path.getName(), size);
                return OverlayImages(symlink, overlay);
            }
            if (path.attributes().isFile())
            {
                if (String.IsNullOrEmpty(path.getExtension()))
                {
                    if (path.attributes().getPermission().isExecutable())
                    {
                        return IconForName("executable", size);
                    }
                }
                return IconForFilename(path.getName(), size);
            }
            if (path.attributes().isVolume())
            {
                return IconForName(path.getHost().getProtocol().disk(), size);
            }
            if (path.attributes().isDirectory())
            {
                if (OverlayFolderImage)
                {
                    if (!path.attributes().getPermission().isExecutable()
                        || (path.isCached() && !path.cache().get(path.getReference()).attributes().isReadable()))
                    {
                        return IconForFolder(IconForName("privatefolderbadge", size), size);
                    }
                    if (!path.attributes().getPermission().isReadable())
                    {
                        if (path.attributes().getPermission().isWritable())
                        {
                            return IconForFolder(IconForName("dropfolderbadge", size), size);
                        }
                    }
                    if (!path.attributes().getPermission().isWritable())
                    {
                        return IconForFolder(IconForName("readonlyfolderbadge", size), size);
                    }
                }
                return IconForFolder(size);
            }
            return ResizeImage(IconForName("notfound", size), size);
        }

        private Bitmap ResizeImage(Image imgToResize, IconSize size)
        {
            return ResizeImage(imgToResize, size == IconSize.Small ? 16 : 32);
        }

        public Bitmap IconForName(string name, IconSize size)
        {
            return IconForName(name, size == IconSize.Small ? 16 : 32);
        }

        /// <summary>
        /// Find a bitmap in the ResourcesBundle for a given name
        /// </summary>
        /// <param name="name"></param>
        /// <returns></returns>
        public Bitmap IconForName(string name, int size)
        {
            Bitmap image = _bitmapCache.Get(name, size);
            if (null == image)
            {
                object obj = ResourcesBundle.ResourceManager.GetObject(name, ResourcesBundle.Culture);
                if (obj is Icon)
                {
                    image = (new Icon(obj as Icon, size, size)).ToBitmap();
                    _bitmapCache.Put(name, image, size);
                    return image;
                }
                if (obj is Bitmap)
                {
                    image = (Bitmap) obj;

                    if (image.RawFormat == ImageFormat.Tiff)
                    {
                        // handle multi-page tiffs
                        image = GetTiffImage(image, size);
                    }
                    else if (size > 0)
                    {
                        image = ResizeImage(image, new Size(size, size));
                    }
                }
                _bitmapCache.Put(name, image, size);
            }
            return image;
        }

        public Bitmap IconForName(string name)
        {
            return IconForName(name, 0);
        }

        /// <summary>
        /// Get an overlayed folder icon
        /// </summary>
        /// <param name="overlay"></param>
        /// <returns></returns>
        public Bitmap IconForFolder(Bitmap overlay, IconSize size)
        {
            return OverlayImages(IconForFolder(size), overlay);
        }

        /// <summary>
        /// Return our standard folder image
        /// </summary>
        /// <returns></returns>
        public Bitmap IconForFolder(IconSize size)
        {
            int s = size == IconSize.Small ? 16 : 32;
            Icon icon = _iconCache.Get("folder", s);
            if (null == icon)
            {
                icon = GetFolderIcon(size, FolderType.Open);
                _iconCache.Put("folder", icon, s);
            }
            return icon.ToBitmap();
        }

        public Bitmap GetDefaultBrowserIcon()
        {
            Bitmap bitmap = _bitmapCache.Get("defaultbrowser", 32);
            if (null != bitmap) return bitmap;

            string browser = Utils.GetSystemDefaultBrowser();
            try
            {
                if (null != browser)
                {
                    Icon i = Icon.ExtractAssociatedIcon(browser);
                    if (null != i)
                    {
                        Bitmap res = i.ToBitmap();
                        _bitmapCache.Put("defaultbrowser", res, 32);
                        return res;
                    }
                }
            }
            catch
            {
                //return default icon
            }
            return IconForName("notfound", 32);
        }

        public Bitmap ExtractIconForFilename(string file)
        {
            try
            {
                return Icon.ExtractAssociatedIcon(file).ToBitmap();
            }
            catch (Exception)
            {
                return null;
            }
        }


        /// <summary>
        /// Get the associated icon for a given file type (according to its extension)
        /// </summary>
        /// <param name="file"></param>
        /// <returns></returns>
        public Bitmap IconForFilename(string file, IconSize size)
        {
            return GetFileIcon(file, size, false).ToBitmap();
        }

        /// <summary>
        /// Returns an icon for a given file - indicated by the name parameter.
        /// </summary>
        /// <param name="name">Pathname for file.</param>
        /// <param name="size">Large or small</param>
        /// <param name="linkOverlay">Whether to include the link icon</param>
        /// <returns>System.Drawing.Icon</returns>
        private Icon GetFileIcon(string name, IconSize size, bool linkOverlay)
        {
            return GetFileIcon(name, false, size, linkOverlay);
        }

        public ImageList GetProtocolImages(int size)
        {
            ImageList list;
            if (!_protocolImages.TryGetValue(size, out list))
            {
                list = new ImageList();
                list.ImageSize = new Size(size, size);
                list.ColorDepth = ColorDepth.Depth32Bit;
                foreach (Protocol p in Protocol.getKnownProtocols().toArray(new Protocol[] {}))
                {
                    list.Images.Add(p.getIdentifier(), IconForName(p.disk(), size));
                }
                _protocolImages.Add(size, list);
            }
            return list;
        }

        public ImageList GetProtocolIcons()
        {
            if (null == _protocolIcons)
            {
                _protocolIcons = new ImageList();
                _protocolIcons.ImageSize = new Size(16, 16);
                _protocolIcons.ColorDepth = ColorDepth.Depth32Bit;
                _protocolIcons.Images.Clear();
                foreach (Protocol p in Protocol.getKnownProtocols().toArray(new Protocol[] {}))
                {
                    _protocolIcons.Images.Add(p.getIdentifier(), IconForName(p.icon(), 16));
                }
            }
            return _protocolIcons;
        }

        private Icon GetFileIcon(string name, bool isFolder, IconSize size, bool linkOverlay)
        {
            //by extension
            string key = Utils.GetSafeExtension(name);
            if(isFolder)
            {
                key += "-folder";
            }
            if (linkOverlay)
            {
                key += "-overlay";
            }
            int s = size == IconSize.Small ? 16 : 32;
            Icon icon = _iconCache.Get(key, s);
            if (null == icon)
            {
                Shell32.SHFILEINFO shfi = new Shell32.SHFILEINFO();
                uint flags = Shell32.SHGFI_ICON | Shell32.SHGFI_USEFILEATTRIBUTES;

                if (linkOverlay) flags += Shell32.SHGFI_LINKOVERLAY;

                /* Check the size specified for return. */
                if (IconSize.Small == size)
                {
                    flags += Shell32.SHGFI_SMALLICON;
                }
                else
                {
                    flags += Shell32.SHGFI_LARGEICON;
                }

                uint fileAttributes = 0;
                if (isFolder)
                {
                    fileAttributes = Shell32.FILE_ATTRIBUTE_DIRECTORY;
                }
                else
                {
                    fileAttributes = Shell32.FILE_ATTRIBUTE_NORMAL;
                }

                Shell32.SHGetFileInfo(name,
                                      fileAttributes,
                                      ref shfi,
                                      (uint) Marshal.SizeOf(shfi),
                                      flags);

                // Copy (clone) the returned icon to a new object, thus allowing us to clean-up properly
                icon = (Icon) Icon.FromHandle(shfi.hIcon).Clone();
                _iconCache.Put(key, icon, s);
                User32.DestroyIcon(shfi.hIcon); // Cleanup
            }
            return icon;
        }

        /// <summary>
        /// Used to access system folder icons.
        /// </summary>
        /// <param name="size">Specify large or small icons.</param>
        /// <param name="folderType">Specify open or closed FolderType.</param>
        /// <returns>System.Drawing.Icon</returns>
        private Icon GetFolderIcon(IconSize size, FolderType folderType)
        {
            // Need to add size check, although errors generated at present!
            uint flags = Shell32.SHGFI_ICON | Shell32.SHGFI_USEFILEATTRIBUTES;

            if (FolderType.Open == folderType)
            {
                flags += Shell32.SHGFI_OPENICON;
            }

            if (IconSize.Small == size)
            {
                flags += Shell32.SHGFI_SMALLICON;
            }
            else
            {
                flags += Shell32.SHGFI_LARGEICON;
            }

            // Get the folder icon
            Shell32.SHFILEINFO shfi = new Shell32.SHFILEINFO();
            Shell32.SHGetFileInfo("_unknown",
                                  Shell32.FILE_ATTRIBUTE_DIRECTORY,
                                  ref shfi,
                                  (uint) Marshal.SizeOf(shfi),
                                  flags);

            Icon.FromHandle(shfi.hIcon); // Load the icon from an HICON handle

            // Now clone the icon, so that it can be successfully stored in an ImageList
            Icon icon = (Icon) Icon.FromHandle(shfi.hIcon).Clone();

            User32.DestroyIcon(shfi.hIcon); // Cleanup
            return icon;
        }

        private static Bitmap ResizeImage(Image imgToResize, int size)
        {
            return ResizeImage(imgToResize, new Size(size, size));
        }

        private static Bitmap ResizeImage(Image imgToResize, Size size)
        {
            Bitmap b = new Bitmap(size.Width, size.Height);
            using (Graphics g = Graphics.FromImage(b))
            {
                g.InterpolationMode = InterpolationMode.HighQualityBicubic;
                g.DrawImage(imgToResize, 0, 0, size.Width, size.Height);
            }
            return b;
        }

        private static Bitmap[] GetTiffImages(Image sourceImage)
        {
            int pageCount = sourceImage.GetFrameCount(FrameDimension.Page);

            Bitmap[] returnImage = new Bitmap[pageCount];

            Guid objGuid = sourceImage.FrameDimensionsList[0];
            FrameDimension objDimension = new FrameDimension(objGuid);

            for (int i = 0; i < pageCount; i++)
            {
                using (MemoryStream ms = new MemoryStream())
                {
                    sourceImage.SelectActiveFrame(objDimension, i);
                    sourceImage.Save(ms, ImageFormat.Tiff);
                    returnImage[i] = new Bitmap(ms);
                }
            }

            return returnImage;
        }

        private static Bitmap GetTiffImage(Bitmap image, int size)
        {
            Bitmap[] images = GetTiffImages(image);
            Bitmap biggest = null;
            foreach (Bitmap bitmap in images)
            {
                if (bitmap.Size.Width == size) return bitmap;
                if (null == biggest || bitmap.Size.Width > biggest.Size.Width)
                {
                    biggest = bitmap;
                }
            }
            return ResizeImage(biggest, size);
        }

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

        private class TypedLRUCache<T> : LRUCache<string, IDictionary<int, T>>
        {
            public TypedLRUCache(int capacity)
                : base(capacity)
            {
            }

            public void Put(String key, T image, int size)
            {
                IDictionary<int, T> versions;
                if (ContainsKey(key))
                {
                    versions = this[key];
                }
                else
                {
                    versions = new Dictionary<int, T>();
                    Add(key, versions);
                }
                versions[size] = image;
                //versions.Add(size, image);                
            }

            public T Get(String key, int size)
            {
                IDictionary<int, T> versions;
                if (!TryGetValue(key, out versions))
                {
                    Log.warn("No cached image for " + key);
                    return default(T);
                }
                T result;
                versions.TryGetValue(size, out result);
                return result;
            }
        }
    }

    /// <summary>
    /// Wraps necessary Shell32.dll structures and functions required to retrieve Icon Handles using SHGetFileInfo. Code
    /// courtesy of MSDN Cold Rooster Consulting case study.
    /// </summary>
    /// 
    // This code has been left largely untouched from that in the CRC example. The main changes have been moving
    // the icon reading code over to the IconReader type.
    public class Shell32
    {
        public const uint BIF_BROWSEFORCOMPUTER = 0x1000;
        public const uint BIF_BROWSEFORPRINTER = 0x2000;
        public const uint BIF_BROWSEINCLUDEFILES = 0x4000;
        public const uint BIF_BROWSEINCLUDEURLS = 0x0080;
        public const uint BIF_DONTGOBELOWDOMAIN = 0x0002;
        public const uint BIF_EDITBOX = 0x0010;
        public const uint BIF_NEWDIALOGSTYLE = 0x0040;
        public const uint BIF_RETURNFSANCESTORS = 0x0008;
        public const uint BIF_RETURNONLYFSDIRS = 0x0001;
        public const uint BIF_SHAREABLE = 0x8000;
        public const uint BIF_STATUSTEXT = 0x0004;
        public const uint BIF_USENEWUI = (BIF_NEWDIALOGSTYLE | BIF_EDITBOX);
        public const uint BIF_VALIDATE = 0x0020;
        public const uint FILE_ATTRIBUTE_DIRECTORY = 0x00000010;
        public const uint FILE_ATTRIBUTE_NORMAL = 0x00000080;
        public const int MAX_PATH = 256;
        public const uint SHGFI_ADDOVERLAYS = 0x000000020; // apply the appropriate overlays

        public const uint SHGFI_ATTRIBUTES = 0x000000800; // get attributes
        public const uint SHGFI_ATTR_SPECIFIED = 0x000020000; // get only specified attributes
        public const uint SHGFI_DISPLAYNAME = 0x000000200; // get display name
        public const uint SHGFI_EXETYPE = 0x000002000; // return exe type
        public const uint SHGFI_ICON = 0x000000100; // get icon
        public const uint SHGFI_ICONLOCATION = 0x000001000; // get icon location
        public const uint SHGFI_LARGEICON = 0x000000000; // get large icon
        public const uint SHGFI_LINKOVERLAY = 0x000008000; // put a link overlay on icon
        public const uint SHGFI_OPENICON = 0x000000002; // get open icon
        public const uint SHGFI_OVERLAYINDEX = 0x000000040; // Get the index of the overlay
        public const uint SHGFI_PIDL = 0x000000008; // pszPath is a pidl
        public const uint SHGFI_SELECTED = 0x000010000; // show icon in selected state
        public const uint SHGFI_SHELLICONSIZE = 0x000000004; // get shell size icon
        public const uint SHGFI_SMALLICON = 0x000000001; // get small icon
        public const uint SHGFI_SYSICONINDEX = 0x000004000; // get system icon index
        public const uint SHGFI_TYPENAME = 0x000000400; // get type name
        public const uint SHGFI_USEFILEATTRIBUTES = 0x000000010; // use passed dwFileAttribute

        [DllImport("shell32.dll", CharSet = CharSet.Auto)]
        public static extern IntPtr SHGetFileInfo(
            string pszPath,
            uint dwFileAttributes,
            ref SHFILEINFO psfi,
            uint cbFileInfo,
            uint uFlags
            );

        [StructLayout(LayoutKind.Sequential)]
        public struct BROWSEINFO
        {
            public IntPtr hwndOwner;
            public IntPtr pidlRoot;
            public IntPtr pszDisplayName;
            [MarshalAs(UnmanagedType.LPTStr)] public string lpszTitle;
            public uint ulFlags;
            public IntPtr lpfn;
            public int lParam;
            public IntPtr iImage;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct ITEMIDLIST
        {
            public SHITEMID mkid;
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
        public struct SHFILEINFO
        {
            public IntPtr hIcon;
            public int iIcon;
            public uint dwAttributes;
            [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 260)] public string szDisplayName;
            [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 80)] public string szTypeName;
        } ;

        [StructLayout(LayoutKind.Sequential)]
        public struct SHITEMID
        {
            public ushort cb;
            [MarshalAs(UnmanagedType.LPArray)] public byte[] abID;
        }
    }

    /// <summary>
    /// Wraps necessary functions imported from User32.dll. Code courtesy of MSDN Cold Rooster Consulting example.
    /// </summary>
    public class User32
    {
        /// <summary>
        /// Provides access to function required to delete handle. This method is used internally
        /// and is not required to be called separately.
        /// </summary>
        /// <param name="hIcon">Pointer to icon handle.</param>
        /// <returns>N/A</returns>
        [DllImport("User32.dll")]
        public static extern int DestroyIcon(IntPtr hIcon);
    }
}