using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Windows.Forms;
using System.Windows.Media.Imaging;
using static System.Drawing.Imaging.PixelFormat;
using static System.Windows.Media.PixelFormats;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    using ch.cyberduck.core;
    using ch.cyberduck.core.features;

    public class WinFormsIconProvider : IconProvider<Image>
    {
        public WinFormsIconProvider(ProtocolFactory protocols, IconCache iconCache, IIconProviderImageSource imageSource) : base(iconCache, imageSource)
        {
            BuildProtocolImageList(protocols);
        }

        public override Image GetDisk(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out Image image, "Disk")
            ? image
            : Get(protocol, protocol.disk(), size, "Disk");

        public override Image GetIcon(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out Image image, "Icon")
            ? image
            : Get(protocol, protocol.icon(), size, "Icon");

        protected override Image Get(string name, int size)
            => Get(name, name, size, default);

        protected override Image Get(string name)
            => Get(name, name, default);

        private Image Get(object key, string path, string classifier)
            => IconCache.TryGetIcon(key, out Image image, classifier)
            ? image
            : Get(key, path, 0, classifier, true);

        private Image Get(object key, string path, int size, string classifier)
            => IconCache.TryGetIcon(key, size, out Image image, classifier)
            ? image
            : Get(key, path, size, classifier, false);

        private Image Get(object key, string path, int size, string classifier, bool returnDefault)
        {
            var images = Get(key, path, classifier, returnDefault, out var image);
            return image ?? FindNearestFit(images, size, (c, s, i) => c.CacheIcon(key, s, i, classifier));
        }

        public ImageList ProtocolList { get; } = new ImageList() { ImageSize = new Size(16, 16), ColorDepth = ColorDepth.Depth32Bit };

        public Image AliasFolder()
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

        public Image DefaultBrowser() => GetFileIcon(Utils.GetSystemDefaultBrowser(), false, true, true);

        public Image GetPath(Path path, int size)
        {
            string key = "path:" + (path.isDirectory() ? "folder" : path.getExtension());

            Func<(string, Func<int, Image>)> overlayFactory = default;
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

            (string Class, Func<int, Image> factory) = overlayFactory?.Invoke() ?? default;
            if (IconCache.TryGetIcon(key, out Image image, Class))
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

        public Image ResizeImageDangerous(Image image, int size) => new Bitmap(image, new Size(size, size));

        private static Image Overlay(Image original, Image overlay, int size)
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

        private IEnumerable<Image> Get(object key, string path, string classifier, bool returnDefault, out Image @default)
        {
            Image image = default;

            var images = IconCache.Filter<Image>(((object key, string classifier, int) f) => Equals(key, f.key) && Equals(classifier, f.classifier));
            if (!images.Any())
            {
                bool isDefault = !IconCache.TryGetIcon<Image>(key, out _, classifier);
                Stream stream = default;
                bool dispose = true;
                try
                {
                    stream = GetStream(path);
                    images = GetImages(stream, (c, s) => c.TryGetIcon<Image>(key, out _, classifier), (c, s, i) =>
                    {
                        if (isDefault)
                        {
                            isDefault = false;
                            if (returnDefault)
                            {
                                image = i;
                            }
                            IconCache.CacheIcon<Image>(key, s, classifier);
                        }
                        IconCache.CacheIcon(key, s, i, classifier);
                    }, out dispose);
                }
                finally
                {
                    if (dispose && stream != null)
                    {
                        stream.Dispose();
                    }
                }
            }

            @default = image;
            return images;
        }

        private Image FindNearestFit(IEnumerable<Image> sources, int size, CacheIconCallback cacheCallback)
        {
            var nearest = int.MaxValue;
            Image nearestFit = null;

            foreach (var item in sources)
            {
                int d = size - item.Width;
                if (d == 0)
                {
                    return item;
                }

                if ((d < 0 && (nearest > 0 || nearest < d)) || (nearest > 0 && d < nearest))
                {
                    nearest = d;
                    nearestFit = item;
                }
            }
            nearestFit = new Bitmap(nearestFit, size, size);
            cacheCallback(IconCache, size, nearestFit);
            return nearestFit;
        }

        private IEnumerable<Image> GetImages(Stream stream, GetCacheIconCallback getCache, CacheIconCallback cacheIcon, out bool dispose)
        {
            Image source = Image.FromStream(stream);

            if (ImageFormat.Gif.Equals(source.RawFormat))
            {
                // Gif cannot be cached/duplicated/or anything else, really.
                // underlying stream must not be closed (learned the hard way).
                dispose = false;
                cacheIcon(IconCache, source.Width, source);
                return new[] { source };
            }

            dispose = true;
            using (source)
            {
                if (ImageFormat.Icon.Equals(source.RawFormat))
                {
                    source.Dispose();
                    dispose = true;
                    stream.Position = 0;
                    using Icon icon = new(stream);
                    Image image = icon.ToBitmap();
                    cacheIcon(IconCache, image.Width, image);
                    return new[] { image };
                }
                else if (ImageFormat.Tiff.Equals(source.RawFormat))
                {
                    List<Image> frames = new();
                    FrameDimension frameDimension = new(source.FrameDimensionsList[0]);
                    var pageCount = source.GetFrameCount(FrameDimension.Page);
                    for (int i = 0; i < pageCount; i++)
                    {
                        source.SelectActiveFrame(frameDimension, i);
                        if (getCache(IconCache, source.Width)) continue;

                        Bitmap copy = new(source);
                        copy.SetResolution(96, 96);
                        frames.Add(copy);
                        cacheIcon(IconCache, copy.Width, copy);
                    }
                    return frames;
                }
                else
                {
                    Bitmap copy = new(source);
                    copy.SetResolution(96, 96);
                    cacheIcon(IconCache, copy.Width, copy);
                    return new[] { copy };
                }
            }
        }

        protected override Image Get(IntPtr nativeIcon, CacheIconCallback cacheIcon)
        {
            using var icon = Icon.FromHandle(nativeIcon);
            Image bitmap = icon.ToBitmap();
            cacheIcon(IconCache, bitmap.Width, bitmap);
            return bitmap;
        }
    }
}
