using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Linq;

namespace Cyberduck.Core.Refresh.Services
{
    using System.IO;

    public class WinFormsIconProvider : IconProvider<Bitmap>
    {
        public WinFormsIconProvider(IconCache iconCache) : base(iconCache)
        {
        }

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
            List<Bitmap> images = new();

            using var source = Image.FromStream(stream);
            if (source.RawFormat == ImageFormat.Icon && source is Bitmap fileBitmap)
            {
            }
            else if (source.RawFormat == ImageFormat.Tiff)
            {
                FrameDimension frameDimension = new(source.FrameDimensionsList[0]);
                var pages = source.GetFrameCount(FrameDimension.Page);
                for (int i = 0; i < pages; i++)
                {
                    source.SelectActiveFrame(frameDimension, i);
                    if (getCache(IconCache, source.Width) != null) continue;

                    Bitmap copy = new(source);
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

        protected override IEnumerable<Bitmap> GetImages(IntPtr nativeIcon, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            return Enumerable.Empty<Bitmap>();
        }
    }
}
