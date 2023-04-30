﻿using ch.cyberduck.core;
using ch.cyberduck.core.local;
using Ch.Cyberduck.Core.Local;
using org.apache.logging.log4j;
using System;
using System.IO;
using Windows.Win32;
using Windows.Win32.Storage.FileSystem;
using Windows.Win32.UI.Shell;
using Windows.Win32.UI.WindowsAndMessaging;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.Storage.FileSystem.FILE_FLAGS_AND_ATTRIBUTES;
using static Windows.Win32.UI.Shell.SHGFI_FLAGS;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    public abstract class IconProvider
    {
        public IconProvider(IconCache iconCache, IIconProviderImageSource imageSource)
        {
            IconCache = iconCache;
            ImageSource = imageSource;
        }

        protected IconCache IconCache { get; }

        protected IIconProviderImageSource ImageSource { get; }

        protected Stream GetStream(string name)
        {
            if (Path.IsPathRooted(name))
            {
                return new FileStream(name, FileMode.Open);
            }
            return ImageSource.GetStream(name);
        }
    }

    public abstract class IconProvider<T> : IconProvider
    {
        protected static readonly Logger Log = LogManager.getLogger(typeof(T));

        protected IconProvider(IconCache iconCache, IIconProviderImageSource imageSource) : base(iconCache, imageSource)
        {
        }

        public delegate void CacheIconCallback(IconCache cache, int size, T source);

        public delegate bool GetCacheIconCallback(IconCache cache, int size);

        public T GetApplication(Application application, int size)
        {
            string key = "app:" + application.getIdentifier();
            if (!IconCache.TryGetIcon(key, size, out T image))
            {
                string iconPath, realIconPath;
                int iconIndex;
                switch (application)
                {
                    case ShellApplicationFinder.ShellApplication shell:
                        iconPath = shell.IconPath;
                        iconIndex = shell.IconIndex;
                        break;

                    case ShellApplicationFinder.ProgIdApplication progId:
                        iconPath = progId.IconPath;
                        iconIndex = progId.IconIndex;
                        break;

                    default:
                        return default;
                }
                realIconPath = iconPath;
                
                try
                {
                    iconPath = SHLoadIndirectString(iconPath);

                    SHCreateFileExtractIcon(iconPath, 0, out IExtractIconW icon);
                    using HICON_Handle largeIcon = new();
                    using HICON_Handle smallIcon = new();

                    icon.Extract(iconPath, (uint)iconIndex, ref largeIcon.Handle, ref smallIcon.Handle, 0);
                    Get(largeIcon, (c, s, i) => c.CacheIcon(key, s, i));
                    Get(smallIcon, (c, s, i) => c.CacheIcon(key, s, i));
                    image = Get(key, size);
                }
                catch (Exception genericException)
                {
                    Log.error(string.Format("Failure extracting icon for {0}. Icon path: {1} (Index: {2}, Indirect: \"{3}\")", application, iconPath, iconIndex, realIconPath), genericException);
                }
            }
            return image;
        }

        public abstract T GetDisk(Protocol protocol, int size);

        public T GetFileIcon(string filename, bool isFolder, bool large, bool isExecutable)
        {
            string key = string.Empty;
            if (isFolder)
            {
                key = "folder";
            }
            else if (isExecutable)
            {
                key = filename;
            }
            else if (filename.LastIndexOf('.') is int index && index != -1)
            {
                key = filename.Substring(index + 1);
            }

            if (IconCache.TryGetIcon("ext", large ? 32 : 16, out T image, key))
            {
                return image;
            }

            SHGFI_FLAGS flags = SHGFI_ICON | SHGFI_USEFILEATTRIBUTES;
            flags |= large ? SHGFI_LARGEICON : SHGFI_SMALLICON;

            FILE_FLAGS_AND_ATTRIBUTES fileAttributes = isFolder ? FILE_ATTRIBUTE_DIRECTORY : FILE_ATTRIBUTE_NORMAL;

            SHFILEINFOW shfi = new();
            try
            {
                if (SHGetFileInfo(isFolder ? "_unknown" : filename, fileAttributes, shfi, flags) == 0)
                {
                    return default;
                }
                return Get(shfi.hIcon, (c, s, i) => c.CacheIcon("ext", s, i, key));
            }
            finally
            {
                DestroyIcon(shfi.hIcon);
            }
        }

        public abstract T GetIcon(Protocol protocol, int size);

        public T GetResource(string name, int? requestSize = default) => requestSize switch
        {
            int size => Get(name, size),
            _ => Get(name)
        };

        protected abstract T Get(string name);

        protected abstract T Get(string name, int size);

        protected abstract T Get(IntPtr nativeIcon, CacheIconCallback cacheIcon);
    }
}
