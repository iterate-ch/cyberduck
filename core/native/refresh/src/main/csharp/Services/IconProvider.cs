using System;
using System.Runtime.InteropServices;
using ch.cyberduck.core;
using static Windows.Win32.CoreConstants;
using static Windows.Win32.UI.Shell.SHGFI_FLAGS;
using static Windows.Win32.CorePInvoke;
using Windows.Win32.Storage.FileSystem;
using static Windows.Win32.Storage.FileSystem.FILE_FLAGS_AND_ATTRIBUTES;
using Windows.Win32.UI.Shell;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    using System.IO;

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
        protected IconProvider(IconCache iconCache, IIconProviderImageSource imageSource) : base(iconCache, imageSource)
        {
        }

        public delegate void CacheIconCallback(IconCache cache, int size, T source);

        public delegate bool GetCacheIconCallback(IconCache cache, int size);

        public abstract T GetDisk(Protocol protocol, int size);

        public abstract T GetIcon(Protocol protocol, int size);

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
