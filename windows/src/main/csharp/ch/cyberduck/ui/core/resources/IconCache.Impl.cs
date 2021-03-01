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
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;

namespace Ch.Cyberduck.Ui.Core.Resources
{
    using Ch.Cyberduck.Core;

    partial class IconCache
    {
        private static Image _ResizeImage(Image image, int size)
        {
            if (image is null)
            {
                return null;
            }
            var resized = new Bitmap(size, size);
            using (Graphics g = Graphics.FromImage(resized))
            {
                g.InterpolationMode = InterpolationMode.HighQualityBicubic;
                g.DrawImage(image, 0, 0, size, size);
            }
            return resized;
        }

        private static Icon GetFileIconFromExtension(string filename, bool isFolder, IconSize size, bool linkOverlay)
        {
            string key = Utils.GetSafeExtension(filename);
            if (isFolder)
                key += "-folder";
            if (linkOverlay)
                key += "-overlay";
            key += "." + size.Size();

            if (iconCache.get(key) is Icon icon)
            {
                return icon;
            }
            icon = GetFileIconFromName(filename, isFolder, size, linkOverlay);
            if (icon is null)
            {
                return null;
            }

            iconCache.put(key, icon);
            return icon;
        }

        private static Image GetFolderIcon(IconSize size)
        {
            var key = "folder." + size.Size();

            if (imageCache.get(key) is Image image)
            {
                return image;
            }

            return GetCachedIconImage(key,
                () => GetFolderIcon(size, FolderType.Open),
                () => IconForName("notfound", size));
        }

        private static Image GetIconForName(string name, int size)
        {
            if (string.IsNullOrWhiteSpace(name))
            {
                name = "notfound";
            }

            var key = name + "." + size;
            var image = imageCache.get(key) as Image;
            if (image != null)
            {
                return image;
            }

            if (!string.IsNullOrWhiteSpace(Path.GetDirectoryName(name)))
            {
                image = Image.FromFile(name);
                if (image.RawFormat == ImageFormat.Icon && image is Bitmap fileBitmap)
                {
                    // Always dispose loaded image when working with icons
                    using (fileBitmap)
                    using (var hIcon = new HICON(fileBitmap.GetHicon()))
                    using (var icon = Icon.FromHandle(hIcon.Value))
                    using (var nested = new Icon(icon, size, size))
                    {
                        image = nested.ToBitmap();
                        // hIcon must be destroyed independently from Icon.FromHandle
                    }
                }
            }
            else
            {
                object obj = ResourcesBundle.ResourceManager.GetObject(
                    Path.GetFileNameWithoutExtension(name),
                    ResourcesBundle.Culture);
                if (obj is Icon icon)
                {
                    // Always dispose of icon and resized icon
                    using (icon)
                    using (var resized = new Icon(icon, size, size))
                    {
                        // ToBitmap always create a memcopy.
                        image = resized.ToBitmap();
                    }
                }
                else if (obj is Image bitmap)
                {
                    image = bitmap;
                }
            }

            if (image is null)
            {
                return null;
            }

            if (!(image is null) && image.RawFormat == ImageFormat.Tiff)
            {
                // Dispose image regardless of Image.FromFile or ResourceManager.GetObject() as both
                // always create new objects.
                using (var temp = image)
                {
                    image = HandleMultipageTiff(temp, size);
                }
            }
            else if (image.Width != size && size > 0)
            {
                // after resizing image, dispose old image
                using (var temp = image)
                {
                    image = _ResizeImage(temp, size);
                }
            }
            if (image.HorizontalResolution != 96 && image is Bitmap rescaleImage)
            {
                rescaleImage.SetResolution(96, 96);
            }
            imageCache.put(key, image);
            return image;
        }

        private static Image HandleMultipageTiff(Image image, int size)
        {
            var frameDimension = new FrameDimension(image.FrameDimensionsList[0]);
            var pageCount = image.GetFrameCount(FrameDimension.Page);
            int maxWidth = int.MinValue;
            int biggestIndex = 0;

            for (int i = pageCount - 1; i >= 0; i--)
            {
                image.SelectActiveFrame(frameDimension, i);
                if (image.Size.Width == size)
                {
                    return new Bitmap(image);
                    // found
                }

                if (image.Size.Width > maxWidth)
                {
                    maxWidth = image.Size.Width;
                    biggestIndex = i;
                }
            }

            image.SelectActiveFrame(frameDimension, biggestIndex);
            return _ResizeImage(image, size);
        }

        private static Image OverlayImages(Image original, Image overlay)
        {
            if (original is null)
            {
                return null;
            }

            var cloned = (Image)original.Clone();
            using (Graphics g = Graphics.FromImage(cloned))
            {
                g.DrawImage(overlay, 0, 0);
            }
            return cloned;
        }
    }
}
