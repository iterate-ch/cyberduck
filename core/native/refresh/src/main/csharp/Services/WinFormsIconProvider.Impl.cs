using System;
using System.Buffers.Text;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using Ch.Cyberduck.Core.Refresh.Media.Imaging;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    partial class WinFormsIconProvider
    {
        private IEnumerable<Image> Get(object key, string path, string classifier, bool returnDefault, out Image @default)
        {
            Image image = default;

            var images = IconCache.Filter<Image>(((object key, string classifier, int) f) => Equals(key, f.key) && Equals(classifier, f.classifier));
            if (!images.Any())
            {
                using (IconCache.WriteLock())
                {
                    bool isDefault = !IconCache.TryGetIcon<Image>(key, out _, classifier);
                    images = GetImages(path, (c, s) => c.TryGetIcon<Image>(key, out _, classifier), (c, s, i) =>
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
                    });
                }
            }

            @default = image;
            return images;
        }

        private IEnumerable<Image> GetImages(string name, GetCacheIconCallback getCache, CacheIconCallback cacheIcon)
        {
            if (!TryGetBase64Images(name, getCache, cacheIcon, out var images))
            {
                using var stream = GetStream(name);
                _ = TryGetImages(stream, getCache, cacheIcon, out images);
            }

            return images;
        }

        private bool TryGetBase64Images(string name, GetCacheIconCallback getCache, CacheIconCallback cacheIcon, out IEnumerable<Image> images)
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
                return TryGetImages(imageStream, getCache, cacheIcon, out images);
            }
            catch { /* We don't have an easy way of validating Base64 input. Let it error out. */ }

#if NETCOREAPP
        exit:
#endif
            images = null;
            return false;
        }

        private bool TryGetImages(Stream stream, GetCacheIconCallback getCache, CacheIconCallback cacheIcon, out IEnumerable<Image> images)
        {
            using var source = Image.FromStream(stream);

            if (ImageFormat.Gif.Equals(source.RawFormat))
            {
                MemoryStream gifStream = new();
                source.Save(gifStream, ImageFormat.Gif);
                var image = Image.FromStream(gifStream);
                cacheIcon(IconCache, source.Width, image);
                images = [image];
            }
            else if (ImageFormat.Icon.Equals(source.RawFormat))
            {
                if (!stream.CanSeek)
                {
                    images = null;
                    return false;
                }

                stream.Position = 0;
                GDIIcon icon = new(stream);
                foreach (var item in icon.Frames)
                {
                    cacheIcon(IconCache, item.Width, item);
                }

                images = icon.Frames;
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
                images = frames;
            }
            else
            {
                Bitmap copy = new(source);
                copy.SetResolution(96, 96);
                cacheIcon(IconCache, copy.Width, copy);
                images = [copy];
            }

            return true;
        }
    }
}
