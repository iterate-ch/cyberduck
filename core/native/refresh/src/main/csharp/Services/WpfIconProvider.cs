using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading;
using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;

namespace Cyberduck.Core.Refresh.Services
{
    public class WpfIconProvider : IconProvider<BitmapSource>
    {
        private readonly ConcurrentDictionary<string, AutoResetEvent> sync = new ConcurrentDictionary<string, AutoResetEvent>();

        public WpfIconProvider(IconCache cache) : base(cache)
        {
        }

        protected override BitmapSource FindNearestFit(IEnumerable<BitmapSource> sources, int size, CacheIconCallback cacheCallback)
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

        protected override IEnumerable<BitmapSource> GetImages(Stream stream, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            var list = new List<BitmapSource>();

            var decoder = BitmapDecoder.Create(stream, BitmapCreateOptions.None, BitmapCacheOption.None);
            foreach (var item in decoder.Frames)
            {
                if (getCache(IconCache, item.PixelWidth)) continue;

                var fixedImage = ResizeImage(item);
                list.Add(fixedImage);
                cacheIcon(IconCache, item.PixelWidth, fixedImage);
            }

            return list;
        }

        protected override IEnumerable<BitmapSource> GetImages(IntPtr nativeIcon, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            return Enumerable.Empty<BitmapSource>();
        }

        private IDisposable AutoLock(string key)
        {
            var @event = sync.GetOrAdd(key, _ => new AutoResetEvent(true));
            @event.WaitOne();
            return new SyncKey(@event);
        }

        private BitmapSource ResizeImage(BitmapSource source)
        {
            var writeableBitmap = new WriteableBitmap(source.PixelWidth, source.PixelHeight, 96, 96, source.Format, source.Palette);
            var bytesPerPixel = (source.Format.BitsPerPixel + 7) / 8;
            var stride = bytesPerPixel * source.PixelWidth;
            var pixelBuffer = new byte[stride * source.PixelHeight];
            source.CopyPixels(pixelBuffer, stride, 0);
            writeableBitmap.Lock();
            writeableBitmap.WritePixels(new Int32Rect(0, 0, source.PixelWidth, source.PixelHeight), pixelBuffer, stride, 0);
            writeableBitmap.Unlock();
            writeableBitmap.Freeze();
            return writeableBitmap;
        }

        private BitmapSource ResizeImage(BitmapSource source, int size)
        {
            var rect = new Rect(0, 0, size, size);
            var group = new DrawingGroup();
            RenderOptions.SetBitmapScalingMode(group, BitmapScalingMode.Fant);
            group.Children.Add(new ImageDrawing(source, rect));

            var targetVisual = new DrawingVisual();
            using (var context = targetVisual.RenderOpen())
            {
                context.DrawDrawing(group);
            }

            var resized = new RenderTargetBitmap(size, size, 96, 96, PixelFormats.Default);
            resized.Render(targetVisual);
            resized.Freeze();
            return resized;
        }

        private class SyncKey : IDisposable
        {
            private readonly AutoResetEvent resetEvent;
            private bool disposedValue;

            public SyncKey(AutoResetEvent resetEvent)
            {
                this.resetEvent = resetEvent;
            }

            ~SyncKey()
            {
                Dispose(disposing: false);
            }

            void IDisposable.Dispose()
            {
                Dispose(disposing: true);
                GC.SuppressFinalize(this);
            }

            protected virtual void Dispose(bool disposing)
            {
                if (!disposedValue)
                {
                    resetEvent.Set();

                    disposedValue = true;
                }
            }
        }
    }
}
