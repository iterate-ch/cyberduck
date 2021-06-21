using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Linq;

namespace Cyberduck.Core.Refresh.Services
{
    using ch.cyberduck.core;
    using Ch.Cyberduck.Core;
    using System.IO;
    using System.Windows.Forms;

    public class WinFormsIconProvider : IconProvider<Bitmap>
    {
        public WinFormsIconProvider(ProtocolFactory protocols, IconCache iconCache) : base(iconCache)
        {
            BuildProtocolImageList(protocols);
        }

        public ImageList ProtocolList { get; } = new ImageList() { ImageSize = new Size(16, 16), ColorDepth = ColorDepth.Depth32Bit };

        public Bitmap DefaultBrowser() => GetFileIcon(Utils.GetSystemDefaultBrowser(), false, true, true);

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
                    if (getCache(IconCache, source.Width)) continue;

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

        private void BuildProtocolImageList(ProtocolFactory protocols)
        {
            var iterator = ProtocolFactory.get().find().iterator();
            while (iterator.hasNext())
            {
                var protocol = (Protocol)iterator.next();
                ProtocolList.Images.Add(protocol.disk(), GetDisk(protocol, 16));
            }
        }
    }
}
