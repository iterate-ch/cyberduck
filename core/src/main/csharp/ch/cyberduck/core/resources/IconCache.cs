// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
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

using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.InteropServices;
using ch.cyberduck.core;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.Collections;
using org.apache.commons.io;
using org.apache.log4j;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Core.Resources
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

        private static readonly Logger Log = Logger.getLogger(typeof(IconCache).FullName);

        private static readonly IconCache instance = new IconCache();

        private readonly TypedLRUCache<Bitmap> _bitmapCache =
            new TypedLRUCache<Bitmap>(PreferencesFactory.get().getInteger("icon.cache.size"));

        /// <summary>
        /// Cache limited to n entries
        /// </summary>
        private readonly TypedLRUCache<Icon> _iconCache =
            new TypedLRUCache<Icon>(PreferencesFactory.get().getInteger("icon.cache.size"));

        private readonly Dictionary<int, IDictionary<String, Bitmap>> _protocolImages =
            new Dictionary<int, IDictionary<String, Bitmap>>();

        /// <summary>
        /// 16x16 protocol icons
        /// </summary>
        private IDictionary<String, Bitmap> _protocolIcons;

        public static IconCache Instance
        {
            get { return instance; }
        }

        private Bitmap OverlayImages(Bitmap original, Bitmap overlay)
        {
            if (null == original)
                return null;
            Image cloned = (Image) original.Clone();
            using (Graphics gra = Graphics.FromImage(cloned))
            {
                gra.DrawImage(overlay, new Point(0, 0));
            }
            return (Bitmap) cloned;
        }

        public Bitmap OverlayIcon(string file, string overlay, IconSize size)
        {
            return OverlayImages(IconForFilename(file, size), IconForName(overlay, size));
        }

        /// <summary>
        /// Return a bitmap for a given path
        /// </summary>
        /// <param name="path"></param>
        /// <returns></returns>
        public Bitmap IconForPath(Path path, IconSize size)
        {
            if (path.getType().contains(AbstractPath.Type.decrypted))
            {
                Bitmap overlay = IconForName("unlockedbadge", size);
                if (path.isDirectory())
                {
                    return IconForFolder(overlay, size);
                }
                Bitmap unlocked = IconForFilename(path.getName(), size);
                return OverlayImages(unlocked, overlay);
            }
            if (path.isSymbolicLink())
            {
                Bitmap overlay = IconForName("aliasbadge", size);
                if (path.isDirectory())
                {
                    return IconForFolder(overlay, size);
                }
                Bitmap symlink = IconForFilename(path.getName(), size);
                return OverlayImages(symlink, overlay);
            }
            if (path.isFile())
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
            if (path.isDirectory())
            {
                if (!Permission.EMPTY.equals(path.attributes().getPermission()))
                {
                    if (!path.attributes().getPermission().isExecutable())
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

        public Bitmap IconForName(string name)
        {
            return IconForName(name, 0);
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
                if (FilenameUtils.getPrefix(name) != string.Empty)
                {
                    try
                    {
                        image = new Icon(name, size, size).ToBitmap();
                    }
                    catch (ArgumentException)
                    {
                        //was not an icon, try as plain bitmap
                        image = (Bitmap) Image.FromFile(name);
                        if (size > 0)
                        {
                            image = ResizeImage(image, new Size(size, size));
                        }
                    }
                }
                else
                {
                    object obj = ResourcesBundle.ResourceManager.GetObject(FilenameUtils.getBaseName(name),
                        ResourcesBundle.Culture);
                    if (obj is Icon)
                    {
                        image = (new Icon(obj as Icon, size, size)).ToBitmap();
                    }
                    else if (obj is Bitmap)
                    {
                        image = (Bitmap) obj;
                    }
                }
                if (image != null && image.RawFormat == ImageFormat.Tiff)
                {
                    // handle multi-page tiffs
                    image = GetTiffImage(image, size);
                }
                else if (size > 0)
                {
                    image = ResizeImage(image, new Size(size, size));
                }
                _bitmapCache.Put(name, image, size);
            }
            return image;
        }

        public Bitmap VolumeIcon(Protocol protocol, IconSize size)
        {
            return IconForName(protocol.disk(), size);
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
            if (null == icon)
            {
                return IconForName("notfound", s);
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

        public Bitmap ExtractIconFromExecutable(string exe, IconSize size)
        {
            int s = size == IconSize.Small ? 16 : 32;

            Bitmap bitmap = _bitmapCache.Get(exe, s);
            if (null != bitmap) return bitmap;

            try
            {
                using (Icon icon = Icon.ExtractAssociatedIcon(exe))
                {
                    if (null != icon)
                    {
                        Bitmap res = icon.ToBitmap();
                        if (size == IconSize.Small)
                        {
                            res = ResizeImage(res, s);
                        }
                        _bitmapCache.Put(exe, res, s);
                        return res;
                    }
                }
            }
            catch
            {
            }
            //return default icon
            return IconForName("notfound", s);
        }

        public Bitmap ExtractIconForFilename(string file)
        {
            try
            {
                using (Icon icon = Icon.ExtractAssociatedIcon(file))
                {
                    if (null != icon)
                    {
                        return icon.ToBitmap();
                    }
                }
            }
            catch (Exception)
            {
            }
            return null;
        }

        /// <summary>
        /// Get the associated icon for a given file type (according to its extension)
        /// </summary>
        /// <param name="file"></param>
        /// <returns></returns>
        public Bitmap IconForFilename(string file, IconSize size)
        {
            Icon icon = GetFileIconFromExtension(file, size, false);
            if (null == icon)
            {
                return IconForName("notfound", size);
            }
            Bitmap iconForFilename = icon.ToBitmap();
            return iconForFilename;
        }

        /// <summary>
        /// Returns an icon for a given file - indicated by the name parameter.
        /// </summary>
        /// <param name="filename">Pathname for file.</param>
        /// <param name="size">Large or small</param>
        /// <param name="linkOverlay">Whether to include the link icon</param>
        /// <returns>System.Drawing.Icon</returns>
        private Icon GetFileIconFromExtension(string filename, IconSize size, bool linkOverlay)
        {
            return GetFileIconFromExtension(filename, false, size, linkOverlay);
        }

        public IDictionary<String, Bitmap> GetProtocolImages(int size)
        {
            IDictionary<String, Bitmap> dict;
            if (!_protocolImages.TryGetValue(size, out dict))
            {
                dict = new Dictionary<string, Bitmap>();
                foreach (Protocol p in ProtocolFactory.getEnabledProtocols().toArray(new Protocol[] {}))
                {
                    dict[p.disk()] = IconForName(p.disk(), size);
                }
                _protocolImages.Add(size, dict);
            }
            return dict;
        }

        public IDictionary<String, Bitmap> GetProtocolIcons()
        {
            if (null == _protocolIcons)
            {
                _protocolIcons = new Dictionary<string, Bitmap>();
                foreach (Protocol p in ProtocolFactory.getEnabledProtocols().toArray(new Protocol[] {}))
                {
                    _protocolIcons[p.getProvider()] = IconForName(p.icon(), 16);
                }
            }
            return _protocolIcons;
        }

        private Icon GetFileIconFromExtension(string filename, bool isFolder, IconSize size, bool linkOverlay)
        {
            //by extension
            string key = Utils.GetSafeExtension(filename);
            if (isFolder)
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

                IntPtr hSuccess = Shell32.SHGetFileInfo(filename, fileAttributes, ref shfi, (uint) Marshal.SizeOf(shfi),
                    flags);
                if (hSuccess != IntPtr.Zero)
                {
                    // Copy (clone) the returned icon to a new object, thus allowing us to clean-up properly
                    try
                    {
                        icon = (Icon) Icon.FromHandle(shfi.hIcon).Clone();
                    }
                    catch (Exception)
                    {
                        Log.error("Cannot get icon for " + filename);
                        return Icon.FromHandle(IconForName("notfound", size).GetHicon());
                    }
                    _iconCache.Put(key, icon, s);
                    // Release icon handle
                    User32.DestroyIcon(shfi.hIcon);
                }
            }
            return icon;
        }

        public Icon GetFileIconFromExecutable(string filename, IconSize size)
        {
            string key = filename;
            int s = size == IconSize.Small ? 16 : 32;
            Icon icon = _iconCache.Get(key, s);
            if (null == icon)
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
                    (uint) Marshal.SizeOf(shfi), flags);
                if (hSuccess != IntPtr.Zero)
                {
                    // Copy (clone) the returned icon to a new object, thus allowing us to clean-up properly
                    icon = (Icon) Icon.FromHandle(shfi.hIcon).Clone();
                    _iconCache.Put(key, icon, s);
                    // Release icon handle
                    User32.DestroyIcon(shfi.hIcon);
                }
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
            IntPtr hSuccess = Shell32.SHGetFileInfo("_unknown", Shell32.FILE_ATTRIBUTE_DIRECTORY, ref shfi,
                (uint) Marshal.SizeOf(shfi), flags);
            if (hSuccess != IntPtr.Zero)
            {
                Icon.FromHandle(shfi.hIcon); // Load the icon from an HICON handle

                // Now clone the icon, so that it can be successfully stored in an ImageList
                Icon icon = (Icon) Icon.FromHandle(shfi.hIcon).Clone();

                User32.DestroyIcon(shfi.hIcon); // Cleanup
                return icon;
            }
            return null;
        }

        private static Bitmap ResizeImage(Image imgToResize, int size)
        {
            return ResizeImage(imgToResize, new Size(size, size));
        }

        public static Bitmap ResizeImage(Image imgToResize, Size size)
        {
            if (imgToResize == null)
            {
                return null;
            }
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
            public TypedLRUCache(int capacity) : base(capacity)
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
}