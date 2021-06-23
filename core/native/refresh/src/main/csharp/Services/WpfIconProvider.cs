using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Interop;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using ch.cyberduck.core;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    using System.IO;

    public class WpfIconProvider : IconProvider<BitmapSource>
    {
        private readonly IIconProviderImageSource imageSource;

        public WpfIconProvider(IconCache cache, IIconProviderImageSource imageSource) : base(cache)
        {
            this.imageSource = imageSource;
        }

        public override BitmapSource GetDisk(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out BitmapSource image, "Disk")
            ? image
            : Get(protocol, protocol.icon(), size, "Disk");

        public override BitmapSource GetIcon(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out BitmapSource image, "Icon")
            ? image
            : Get(protocol, protocol.icon(), size, "Icon");

        private Stream GetStream(string name)
        {
            if (Path.IsPathRooted(name))
            {
                return new FileStream(name, FileMode.Open);
            }
            return imageSource.GetStream(name);
        }

        private BitmapSource FindNearestFit(IEnumerable<BitmapSource> sources, int size, CacheIconCallback cacheCallback)
        {
            var nearestFitWidth = int.MaxValue;
            BitmapSource nearestFit = null;

            foreach (var item in sources)
            {
                if (item.PixelWidth == size)
                {
                    return item;
                }

                if (item.PixelWidth > size && nearestFitWidth > item.PixelWidth)
                {
                    nearestFitWidth = item.PixelWidth;
                    nearestFit = item;
                }
            }
            nearestFit = ResizeImage(nearestFit, size);
            cacheCallback(IconCache, size, nearestFit);
            return nearestFit;
        }

        private IEnumerable<BitmapSource> GetImages(Stream stream, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            var list = new List<BitmapSource>();

            var decoder = BitmapDecoder.Create(stream, BitmapCreateOptions.None, BitmapCacheOption.None);
            foreach (var item in decoder.Frames)
            {
                if (getCache(IconCache, item.PixelWidth)) continue;

                var fixedImage = FixDPI(item);
                list.Add(fixedImage);
                cacheIcon(IconCache, item.PixelWidth, fixedImage);
            }

            return list;
        }

        protected override BitmapSource Get(IntPtr nativeIcon, CacheIconCallback cacheIcon)
        {
            var source = Imaging.CreateBitmapSourceFromHIcon(nativeIcon, default, default);
            cacheIcon(IconCache, source.PixelWidth, source);
            return source;
        }

        private BitmapSource FixDPI(BitmapSource source)
        {
            var writeableBitmap = new WriteableBitmap(source.PixelWidth, source.PixelHeight, 96, 96, source.Format, source.Palette);
            var bytesPerPixel = (source.Format.BitsPerPixel + 7) / 8;
            var stride = bytesPerPixel * source.PixelWidth;
            var pixelBuffer = new byte[stride * source.PixelHeight];
            source.CopyPixels(pixelBuffer, stride, 0);
            writeableBitmap.Lock();
            writeableBitmap.WritePixels(new(0, 0, source.PixelHeight, source.PixelHeight), pixelBuffer, stride, 0);
            writeableBitmap.Unlock();
            writeableBitmap.Freeze();
            return writeableBitmap;
        }

        private BitmapSource ResizeImage(BitmapSource source, int size)
        {
            Rect rect = new(new(size, size));
            DrawingGroup group = new();
            RenderOptions.SetBitmapScalingMode(group, BitmapScalingMode.Fant);
            group.Children.Add(new ImageDrawing(source, rect));

            DrawingVisual targetVisual = new();
            using (var context = targetVisual.RenderOpen())
            {
                context.DrawDrawing(group);
            }

            RenderTargetBitmap resized = new(size, size, 96, 96, PixelFormats.Default);
            resized.Render(targetVisual);
            resized.Freeze();
            return resized;
        }

        protected override BitmapSource Get(string name, int size)
            => Get(name, name, size, default);

        protected override BitmapSource Get(string name)
            => Get(name, name, default);

        private BitmapSource Get(object key, string path, string classifier)
            => IconCache.TryGetIcon(key, out BitmapSource image, classifier)
            ? image
            : Get(key, path, 0, classifier, true);

        private BitmapSource Get(object key, string path, int size, string classifier)
            => IconCache.TryGetIcon(key, size, out BitmapSource image, classifier)
            ? image
            : Get(key, path, size, classifier, false);

        private BitmapSource Get(object key, string path, int size, string classifier, bool returnDefault)
        {
            BitmapSource image = default;
            var images = IconCache.Filter<BitmapSource>(((object key, string classifier, int) f) => Equals(path, f.key) && Equals(classifier, f.classifier));
            if (!images.Any())
            {
                bool isDefault = !IconCache.TryGetIcon<BitmapSource>(path, out _, classifier);
                using Stream stream = GetStream(path);
                images = GetImages(stream, (c, s) => c.TryGetIcon<BitmapSource>(path, s, out _, classifier), (c, s, i) =>
                {
                    if (isDefault)
                    {
                        isDefault = false;
                        if (returnDefault)
                        {
                            image = i;
                        }
                        IconCache.CacheIcon<BitmapSource>(path, s, classifier);
                    }
                    IconCache.CacheIcon(path, s, i, classifier);
                });
            }
            return image ?? FindNearestFit(images, size, (c, s, i) => c.CacheIcon(key, s, i, classifier));
        }
    }
}
