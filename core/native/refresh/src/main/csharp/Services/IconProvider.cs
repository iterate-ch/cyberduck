using ch.cyberduck.core;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    using System.IO;

    public abstract class IconProvider
    {
        private readonly IIconProviderImageSource imageSource;

        public IconProvider(IconCache iconCache, IIconProviderImageSource imageSource)
        {
            IconCache = iconCache;
            this.imageSource = imageSource;
        }

        protected IconCache IconCache { get; }

        protected Stream GetStream(string name)
        {
            if (Path.IsPathRooted(name))
            {
                return new FileStream(name, FileMode.Open);
            }
            return imageSource.GetStream(name);
        }
    }

    public abstract class IconProvider<T> : IconProvider
    {
        protected IconProvider(IconCache iconCache, IIconProviderImageSource imageSource) : base(iconCache, imageSource)
        {
        }

        protected delegate void CacheIconCallback(IconCache cache, int size, T source);

        protected delegate bool GetCacheIconCallback(IconCache cache, int size);

        protected delegate IEnumerable<T> QueryIconCacheCallback(IconCache cache);

        public T GetDisk(Protocol protocol, int size) => Get(
            protocol, protocol.disk(), size, "Disk");

        public T GetFileIcon(string filename, bool isFolder, bool large, bool isExecutable)
        {
            string key = isFolder ? "folder" : isExecutable ? filename : Path.GetExtension(filename);
            if (IconCache.TryGetIcon("ext", large ? 32 : 16, out T image, key))
            {
                return image;
            }

            IEnumerable<T> icons;
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
                icons = GetImages(shfi.hIcon, (c, s) => c.TryGetIcon<T>("ext", out _, key), (c, s, i) => c.CacheIcon("ext", s, i, key));
            }
            finally
            {
                User32.DestroyIcon(shfi.hIcon);
            }
            return FindNearestFit(icons, large ? 32 : 16, (c, s, i) => c.CacheIcon("ext", s, i, key));
        }

        public T GetIcon(Protocol protocol, int size) => Get(
            protocol, protocol.icon(), size, "Icon", false);

        public T GetResource(string name) => Get(name, name, default);

        public T GetResource(string name, int size) => Get(name, name, size, default);

        protected abstract T FindNearestFit(IEnumerable<T> icons, int size, CacheIconCallback cacheCallback);

        protected T Get(object key, string path, string classifier) => IconCache.TryGetIcon(key, out T image, classifier)
            ? image
            : Get(key, path, 0, classifier, true);

        protected T Get(object key, string path, int size, string classifier)
        {
            if (IconCache.TryGetIcon(key, size, out T image, classifier))
            {
                return image;
            }
            return Get(key, path, size, classifier, false);
        }

        protected T Get(object key, string path, int size, string classifier, bool returnDefault)
        {
            T image = default;
            var images = IconCache.Filter<T>(((object key, string classifier, int) f) => Equals(key, f.key) && Equals(classifier, f.classifier));
            if (!images.Any())
            {
                bool isDefault = !IconCache.TryGetIcon<T>(key, out _, classifier);
                using Stream stream = GetStream(path);
                images = GetImages(stream, (c, s) => c.TryGetIcon<T>(key, s, out _, classifier), (c, s, i) =>
                {
                    if (isDefault)
                    {
                        isDefault = false;
                        if (returnDefault)
                        {
                            image = i;
                        }
                        IconCache.CacheIcon<T>(key, s, classifier);
                    }
                    IconCache.CacheIcon(key, s, i, classifier);
                });
            }
            return image ?? FindNearestFit(images, size, (c, s, i) => c.CacheIcon(key, s, i, classifier));
        }

        protected abstract IEnumerable<T> GetImages(Stream stream, GetCacheIconCallback getCache, CacheIconCallback cacheIcon);

        protected abstract IEnumerable<T> GetImages(IntPtr nativeIcon, GetCacheIconCallback getCache, CacheIconCallback cacheIcon);
    }
}
