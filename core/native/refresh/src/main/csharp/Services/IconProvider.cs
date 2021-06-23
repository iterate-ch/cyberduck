using System;
using System.Runtime.InteropServices;
using ch.cyberduck.core;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    using System.IO;

    public abstract class IconProvider
    {
        public IconProvider(IconCache iconCache)
        {
            IconCache = iconCache;
        }

        protected IconCache IconCache { get; }
    }

    public abstract class IconProvider<T> : IconProvider
    {
        protected IconProvider(IconCache iconCache) : base(iconCache)
        {
        }

        public delegate void CacheIconCallback(IconCache cache, int size, T source);

        public delegate bool GetCacheIconCallback(IconCache cache, int size);

        public abstract T GetDisk(Protocol protocol, int size);

        public abstract T GetIcon(Protocol protocol, int size);

        public T GetFileIcon(string filename, bool isFolder, bool large, bool isExecutable)
        {
            string key = isFolder ? "folder" : isExecutable ? filename : Path.GetExtension(filename);
            if (IconCache.TryGetIcon("ext", large ? 32 : 16, out T image, key))
            {
                return image;
            }

            uint flags = Shell32.SHGFI_ICON | Shell32.SHGFI_USEFILEATTRIBUTES;
            flags |= large ? Shell32.SHGFI_LARGEICON : Shell32.SHGFI_SMALLICON;

            uint fileAttributes = isFolder ? Shell32.FILE_ATTRIBUTE_DIRECTORY : Shell32.FILE_ATTRIBUTE_NORMAL;

            Shell32.SHFILEINFO shfi = new();
            try
            {
                IntPtr hSuccess = Shell32.SHGetFileInfo(isFolder ? "_unknown" : filename, fileAttributes, ref shfi, (uint)Marshal.SizeOf<Shell32.SHFILEINFO>(), flags);
                if (hSuccess == IntPtr.Zero)
                {
                    return default;
                }
                return Get(shfi.hIcon, (c, s, i) => c.CacheIcon("ext", s, i, key));
            }
            finally
            {
                User32.DestroyIcon(shfi.hIcon);
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
