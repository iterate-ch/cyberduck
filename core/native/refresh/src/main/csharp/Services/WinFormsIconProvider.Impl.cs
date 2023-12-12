using Ch.Cyberduck.Core.Refresh.Media.Imaging;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;

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
            }

            @default = image;
            return images;
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
                    stream.Position = 0;
                    GDIIcon icon = new(stream);
                    foreach (var item in icon.Frames)
                    {
                        cacheIcon(IconCache, item.Width, item);
                    }
                    return icon.Frames;
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
    }
}
