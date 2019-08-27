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

using ch.cyberduck.core;
using org.apache.log4j;
using System;
using System.Collections.Generic;
using System.Drawing;

namespace Ch.Cyberduck.Ui.Core.Resources
{
    using ch.cyberduck.core.preferences;
    using Ch.Cyberduck.Core;
    using Ch.Cyberduck.Core.Collections;

    public partial class IconCache
    {
        private static readonly LRUCache<string, Icon> iconCache = new LRUCache<string, Icon>(IconCacheSize);
        private static readonly LRUCache<string, Image> imageCache = new LRUCache<string, Image>(IconCacheSize);

        private static readonly Logger Log = Logger.getLogger(typeof(IconCache).FullName);

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

        private static int IconCacheSize => PreferencesFactory.get().getInteger("icon.cache.size");

        public static Image GetAppImage(string path, IconSize size)
        {
            var key = path + "." + size.Size();
            if (imageCache.TryGetValue(key, out var image))
            {
                return image;
            }

            return GetCachedIconImage(key,
                () => GetFileIconFromExecutable(path, size),
                () => IconForName("notfound", size));
        }

        public static Image GetDefaultBrowserIcon()
        {
            var key = "defaultbrowser.32";
            if (imageCache.TryGetValue(key, out var image))
            {
                return image;
            }

            return GetCachedIconImage(key, () =>
            {
                string browser = Utils.GetSystemDefaultBrowser();
                return GetFileIconFromExecutable(browser, IconSize.Large);
            }, () => IconForName("notfound", 32));
        }

        public static Image GetProtocolDisk(Protocol protocol, IconSize size)
        {
            var disk = protocol.disk();

            return IconForName(disk, size);
        }

        public static Image GetProtocolDisk(Protocol protocol, int size)
        {
            var disk = protocol.disk();

            return IconForName(disk, size);
        }

        public static Image GetProtocolIcon(Protocol protocol, IconSize size)
        {
            var icon = protocol.icon();

            return IconForName(icon, size);
        }

        public static Image GetProtocolIcon(Protocol protocol, int size)
        {
            var icon = protocol.icon();

            return IconForName(icon, size);
        }

        [Obsolete]
        public static Dictionary<string, Image> GetProtocolIcons()
        {
            var lookup = new Dictionary<string, Image>();
            var iterator = ProtocolFactory.get().find().iterator();
            while (iterator.hasNext())
            {
                var protocol = (Protocol)iterator.next();
                if (!lookup.ContainsKey(protocol.disk()))
                {
                    lookup.Add(protocol.disk(), IconForName(protocol.icon(), 16));
                }
            }
            return lookup;
        }

        public static Image IconForFilename(string file, IconSize size)
        {
            var key = file + "." + size.Size();
            if (imageCache.TryGetValue(key, out var image))
            {
                return image;
            }

            var icon = GetFileIconFromExtension(file, false, size, false);
            if (!(icon is null))
            {
                image = icon.ToBitmap();

                imageCache.Add(key, image);
            }
            if (image is null)
            {
                Log.error("Cannot get icon for " + file);
                return IconForName("notfound", size);
            }
            return image;
        }

        public static Image IconForFolder(IconSize size)
        {
            return GetFolderIcon(size);
        }

        public static Image IconForName(string name)
        {
            return GetIconForName(name, 0);
        }

        public static Image IconForName(string name, IconSize size)
        {
            return GetIconForName(name, size.Size());
        }

        public static Image IconForName(string name, int size)
        {
            return GetIconForName(name, size);
        }

        public static Image IconForPath(Path path, IconSize size)
        {
            Image overlay = default;
            if (path.getType().contains(AbstractPath.Type.decrypted))
            {
                overlay = IconForName("unlockedbadge", size);
            }
            else if (path.isSymbolicLink())
            {
                overlay = IconForName("aliasbadge", size);
            }

            if (!(overlay is null))
            {
                Image image;
                if (path.isDirectory())
                {
                    image = IconForFolder(size);
                }
                else
                {
                    image = IconForFilename(path.getName(), size);
                }

                return OverlayImages(image, overlay);
            }

            var permission = path.attributes().getPermission();
            if (path.isFile())
            {
                if (string.IsNullOrWhiteSpace(path.getExtension()))
                {
                    if (permission.isExecutable())
                    {
                        return IconForName("executable", size);
                    }
                }
                return IconForFilename(path.getName(), size);
            }
            else if (path.isDirectory())
            {
                Image image = IconForFolder(size);

                if (!Permission.EMPTY.equals(permission))
                {
                    if (!permission.isExecutable())
                    {
                        overlay = IconForName("privatefolderbadge", size);
                    }
                    else if (!permission.isReadable())
                    {
                        if (permission.isWritable())
                        {
                            overlay = IconForName("dropfolderbadge", size);
                        }
                    }
                    else if (!permission.isWritable())
                    {
                        overlay = IconForName("readonlyfolderbadge", size);
                    }
                }
                if (!(overlay is null))
                {
                    return OverlayImages(image, overlay);
                }
                return image;
            }
            return IconForName("notfound", size);
        }

        public static Image OverlayFilenameIcon(string filename, string overlay, IconSize size)
        {
            var image = IconForFilename(filename, size);
            var icon = IconForName(overlay, size);

            return OverlayImages(image, icon);
        }

        [Obsolete]
        public static Image ResizeImage(Image image, int size) => _ResizeImage(image, size);

        private static Image GetCachedIconImage(string key, Func<Icon> iconGetter, Func<Image> defaultHandler)
        {
            if (!iconCache.TryGetValue(key, out var icon))
            {
                icon = iconGetter();

                if (icon is null)
                {
                    Log.error("Cannot get icon for " + key);
                    return defaultHandler();
                }

                iconCache.Add(key, icon);
            }

            var image = icon.ToBitmap();
            imageCache.Add(key, image);
            return image;
        }
    }

    internal static class IconCacheIconSizeExtensions
    {
        public static int Size(this IconCache.IconSize size)
        {
            switch (size)
            {
                case IconCache.IconSize.Large:
                    return 32;

                case IconCache.IconSize.Small:
                    return 16;

                default:
                    throw new InvalidOperationException();
            }
        }
    }
}
