using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Windows.Forms;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    using ch.cyberduck.core;

    public class WinFormsIconProvider : IconProvider<Bitmap>
    {
        public WinFormsIconProvider(ProtocolFactory protocols, IconCache iconCache, IIconProviderImageSource imageSource) : base(iconCache, imageSource)
        {
            BuildProtocolImageList(protocols);
        }

        public ImageList ProtocolList { get; } = new ImageList() { ImageSize = new Size(16, 16), ColorDepth = ColorDepth.Depth32Bit };

        public Bitmap AliasFolder()
        {
            if (IconCache.TryGetIcon("path:folder", out Bitmap image, "alias"))
            {
                return image;
            }

            var baseImage = GetFileIcon(default, true, true, false);
            var overlay = GetResource("aliasbadge", 32);
            var overlayed = Overlay(baseImage, overlay, 32);
            IconCache.CacheIcon("path:folder", 32, overlayed, "alias");
            return overlayed;
        }

        public Bitmap DefaultBrowser() => GetFileIcon(Utils.GetSystemDefaultBrowser(), false, true, true);

        public Bitmap GetPath(Path path, int size)
        {
            string key = "path:" + (path.isDirectory() ? "folder" : path.getExtension());

            Func<(string, Func<int, Bitmap>)> overlayFactory = default;
            if (path.getType().contains(AbstractPath.Type.decrypted))
            {
                overlayFactory = () => ("unlocked", (int size) => GetResource("unlockedbadge", size));
            }
            else if (path.isSymbolicLink())
            {
                overlayFactory = () => ("alias", (int size) => GetResource("aliasbadge", size));
            }
            else
            {
                var permission = path.attributes().getPermission();
                if (path.isFile())
                {
                    return string.IsNullOrWhiteSpace(path.getExtension()) && permission.isExecutable()
                        ? GetResource("executable", size)
                        : GetFileIcon(path.getName(), false, size >= 32, false);
                }
                else if (path.isDirectory())
                {
                    if (Permission.EMPTY != permission)
                    {
                        if (!permission.isExecutable())
                        {
                            overlayFactory = () => ("privatefolder", (int size) => GetResource("privatefolderbadge", size));
                        }
                        else if (!permission.isReadable() && permission.isWritable())
                        {
                            overlayFactory = () => ("dropfolder", (int size) => GetResource("dropfolderbadge", size));
                        }
                        else if (!permission.isWritable() && permission.isReadable())
                        {
                            overlayFactory = () => ("readonlyfolder", (int size) => GetResource("readonlyfolderbadge", size));
                        }
                    }
                }
            }

            (string Class, Func<int, Bitmap> factory) = overlayFactory?.Invoke() ?? default;
            if (IconCache.TryGetIcon(key, out Bitmap image, Class))
            {
                return image;
            }

            var baseImage = GetFileIcon(path.getExtension(), path.isDirectory(), size >= 32, false);
            if (factory is not null)
            {
                var overlayed = Overlay(baseImage, factory(size), size);
                IconCache.CacheIcon(key, size, overlayed, Class);
            }
            return baseImage;
        }

        public Bitmap ResizeImageDangerous(Image image, int size) => new Bitmap(image, new Size(size, size));

        protected override Bitmap FindNearestFit(IEnumerable<Bitmap> sources, int size, CacheIconCallback cacheCallback)
        {
            var nearestFitWidth = int.MaxValue;
            Bitmap nearestFit = null;

            foreach (var item in sources)
            {
                if (item.Width == size)
                {
                    return item;
                }

                if (item.Width > size && nearestFitWidth > item.Width)
                {
                    nearestFitWidth = item.Width;
                    nearestFit = item;
                }
            }
            nearestFit = new Bitmap(nearestFit, size, size);
            cacheCallback(IconCache, size, nearestFit);
            return nearestFit;
        }

        protected override IEnumerable<Bitmap> GetImages(Stream stream, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            try
            {
                using var icon = new Icon(stream);
                return GetImages(icon, getCache, cacheIcon);
            }
            catch { }

            using var source = Image.FromStream(stream);
            return GetImages(source, getCache, cacheIcon);
        }

        protected override IEnumerable<Bitmap> GetImages(IntPtr nativeIcon, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            using var icon = Icon.FromHandle(nativeIcon);
            return GetImages(icon, getCache, cacheIcon);
        }

        private static Bitmap Overlay(Image original, Image overlay, int size)
        {
            var surface = new Bitmap(original, new Size(size, size));
            using var graphics = Graphics.FromImage(surface);
            graphics.DrawImage(overlay, graphics.ClipBounds);
            return surface;
        }

        private void BuildProtocolImageList(ProtocolFactory protocols)
        {
            var iterator = ProtocolFactory.get().find().iterator();
            while (iterator.hasNext())
            {
                var protocol = (Protocol)iterator.next();
                ProtocolList.Images.Add(protocol.disk(), GetDisk(protocol, 16));
            }
        }

        private IEnumerable<Bitmap> GetImages(Icon nativeIcon, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            using var copy = nativeIcon.ToBitmap();
            return GetImages(copy, getCache, cacheIcon);
        }

        private IEnumerable<Bitmap> GetImages(Image source, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            List<Bitmap> images = new();

            if (ImageFormat.Tiff.Equals(source.RawFormat) && source is Bitmap fileBitmap)
            {
                FrameDimension frameDimension = new(fileBitmap.FrameDimensionsList[0]);
                var pages = fileBitmap.GetFrameCount(FrameDimension.Page);
                for (int i = 0; i < pages; i++)
                {
                    fileBitmap.SelectActiveFrame(frameDimension, i);
                    if (getCache(IconCache, fileBitmap.Width)) continue;

                    Bitmap copy = new(fileBitmap);
                    copy.SetResolution(96, 96);
                    images.Add(copy);
                    cacheIcon(IconCache, copy.Width, copy);
                }
            }
            else
            {
                Bitmap copy = new(source);
                copy.SetResolution(96, 96);
                images.Add(copy);
                cacheIcon(IconCache, copy.Width, copy);
            }

            return images;
        }
    }
}
