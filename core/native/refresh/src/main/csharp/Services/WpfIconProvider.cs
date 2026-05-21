using System;
using System.Buffers.Text;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Windows;
using System.Windows.Interop;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using ch.cyberduck.core;
using ch.cyberduck.core.profiles;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    public class WpfIconProvider : IconProvider<BitmapSource>
    {
        public WpfIconProvider(IconCache cache, IIconProviderImageSource BitmapSource) : base(cache, BitmapSource)
        {
        }

        public override BitmapSource GetDisk(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out BitmapSource image, "Disk")
            ? image
            : Get(protocol, protocol.disk(), size, "Disk", true);

        public IEnumerable<BitmapSource> GetDisk(Protocol protocol)
            => Get(protocol, protocol.disk(), "Disk", false, true, out _);

        public override BitmapSource GetIcon(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out BitmapSource image, "Icon")
            ? image
            : Get(protocol, protocol.icon(), size, "Icon", true);

        public IEnumerable<BitmapSource> GetIcon(Protocol protocol)
            => Get(protocol, protocol.icon(), "Icon", false, true, out _);

        public IEnumerable<BitmapSource> GetResources(string name) => Get(name, name, default, false, false, out var _);

        public BitmapSource GetThumbnail(ProfileDescription profile, int size)
        {
            if (profile.getThumbnail() is not { } thumbnail)
            {
                return null;
            }

            var key = thumbnail.GetHashCode();
            if (!IconCache.TryGetIcon(key, size, out BitmapSource image, "Thumbnail"))
            {
                image = Get(key, thumbnail, size, "Thumbnail", true);
            }

            return image;
        }

        protected override BitmapSource Get(IntPtr nativeIcon, CacheIconCallback cacheIcon)
        {
            var source = Imaging.CreateBitmapSourceFromHIcon(nativeIcon, default, default);
            cacheIcon(IconCache, source.PixelWidth, source);
            return source;
        }

        protected override BitmapSource Get(string name, int size)
            => Get(name, name, size, default, false);

        protected override BitmapSource Get(string name)
            => Get(name, name, default, false);

        protected override BitmapSource NearestFit(IEnumerable<BitmapSource> sources, int size, CacheIconCallback cacheCallback)
        {
            var nearest = int.MaxValue;
            BitmapSource nearestFit = null;

            foreach (var item in sources)
            {
                int d = size - item.PixelWidth;
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

            nearestFit = ResizeImage(nearestFit, size);
            cacheCallback(IconCache, size, nearestFit);
            return nearestFit;
        }

        protected override BitmapSource Overlay(BitmapSource baseImage, BitmapSource overlay, int size)
        {
            DrawingVisual visual = new();
            using (var context = visual.RenderOpen())
            {
                Rect r = new(0, 0, size, size);
                context.DrawImage(baseImage, r);
                context.DrawImage(overlay, r);
            }

            RenderTargetBitmap bmp = new(size, size, 96, 96, baseImage.Format);
            RenderOptions.SetBitmapScalingMode(bmp, BitmapScalingMode.Fant);
            bmp.Render(visual);
            bmp.Freeze();
            return bmp;
        }

        private BitmapSource FixDPI(BitmapSource source)
        {
            var writeableBitmap = new WriteableBitmap(source.PixelWidth, source.PixelHeight, 96, 96, source.Format, source.Palette);
            var bytesPerPixel = (source.Format.BitsPerPixel + 7) / 8;
            var stride = bytesPerPixel * source.PixelWidth;
            var pixelBuffer = new byte[stride * source.PixelHeight];
            source.CopyPixels(pixelBuffer, stride, 0);
            writeableBitmap.Lock();
            writeableBitmap.WritePixels(new(0, 0, source.PixelWidth, source.PixelHeight), pixelBuffer, stride, 0);
            writeableBitmap.Unlock();
            writeableBitmap.Freeze();
            return writeableBitmap;
        }

        private BitmapSource Get(object key, string path, string classifier, bool isBase64)
            => IconCache.TryGetIcon(key, out BitmapSource image, classifier)
            ? image
            : Get(key, path, 0, classifier, true, isBase64);

        private BitmapSource Get(object key, string path, int size, string classifier, bool isBase64)
            => IconCache.TryGetIcon(key, size, out BitmapSource image, classifier)
            ? image
            : Get(key, path, size, classifier, false, isBase64);

        private BitmapSource Get(object key, string path, int size, string classifier, bool returnDefault, bool isBase64)
        {
            using (IconCache.UpgradeableReadLock())
            {
                var images = Get(key, path, classifier, returnDefault, isBase64, out var image);
                return image ?? NearestFit(images, size, (c, s, i) =>
                {
                    c.CacheIcon(key, s, i, classifier);
                    c.MarkResized(key, s, classifier);
                });
            }
        }

        private IEnumerable<BitmapSource> Get(object key, string name, string classifier, bool returnDefault, bool isBase64, out BitmapSource @default)
        {
            BitmapSource image = default;
            var images = IconCache.Filter<BitmapSource>(((object key, string classifier, int) f) => Equals(key, f.key) && Equals(classifier, f.classifier));
            if (!images.Any())
            {
                using (IconCache.WriteLock())
                {
                    bool isDefault = !IconCache.TryGetIcon<BitmapSource>(key, out _, classifier);
                    images = GetImages(name, (c, s) => c.TryGetIcon<BitmapSource>(key, s, out _, classifier), (c, s, i) =>
                    {
                        if (isDefault)
                        {
                            isDefault = false;
                            if (returnDefault)
                            {
                                image = i;
                            }
                            IconCache.CacheIcon<BitmapSource>(key, s, classifier);
                        }
                        IconCache.CacheIcon(key, s, i, classifier);
                    }, isBase64);
                }
            }
            @default = image;

            return images;
        }

        private IEnumerable<BitmapSource> GetImages(string name, GetCacheIconCallback getCache, CacheIconCallback cacheIcon, bool isBase64)
        {
            if (!isBase64 || !TryGetBase64Images(name, getCache, cacheIcon, out var images))
            {
                using var stream = GetStream(name);
                images = GetImages(stream, getCache, cacheIcon);
            }

            return images;
        }

        private bool TryGetBase64Images(string name, GetCacheIconCallback getCache, CacheIconCallback cacheIcon, out IEnumerable<BitmapSource> images)
        {
#if NETCOREAPP
            if (!Base64.IsValid(name))
            {
                goto exit;
            }
#endif

            try
            {
                using MemoryStream imageStream = new(Convert.FromBase64String(name), false);
                images = GetImages(imageStream, getCache, cacheIcon);
                return true;
            }
            catch { /* We don't have an easy way of validating Base64 input. Let it error out. */ }

#if NETCOREAPP
        exit:
#endif
            images = null;
            return false;
        }

        private IEnumerable<BitmapSource> GetImages(Stream stream, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            var list = new List<BitmapSource>();

            var decoder = BitmapDecoder.Create(stream, BitmapCreateOptions.IgnoreColorProfile, BitmapCacheOption.OnLoad);
            if (decoder is GifBitmapDecoder)
            {
                var frame = decoder.Frames[0];
                if (!getCache(IconCache, frame.PixelWidth))
                {
                    cacheIcon(IconCache, frame.PixelWidth, frame);
                }
            }
            else
            {
                foreach (var item in decoder.Frames)
                {
                    if (getCache(IconCache, item.PixelWidth))
                    {
                        continue;
                    }

                    var resized = FixDPI(item);
                    list.Add(resized);
                    cacheIcon(IconCache, resized.PixelWidth, resized);
                }
            }

            return list;
        }

        private BitmapSource ResizeImage(BitmapSource source, int size)
        {
            Rect rect = new(new(size, size));
            DrawingVisual targetVisual = new();
            using (var context = targetVisual.RenderOpen())
            {
                context.DrawImage(source, rect);
            }

            RenderTargetBitmap resized = new(size, size, 96, 96, PixelFormats.Default);
            RenderOptions.SetBitmapScalingMode(targetVisual, BitmapScalingMode.Fant);
            resized.Render(targetVisual);
            resized.Freeze();
            return resized;
        }
    }
}
