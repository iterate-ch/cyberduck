using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
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
                    }, out _);
                }
            }

            @default = image;
            return images;
        }

        private IEnumerable<Image> GetImages(string name, GetCacheIconCallback getCache, CacheIconCallback cacheIcon, out bool dispose)
        {
            if (!TryGetBase64Images(name, getCache, cacheIcon, out var images))
            {
                using var stream = GetStream(name);
                _ = TryGetImages(stream, getCache, cacheIcon, out images);
            }

            dispose = false;
            return images;
        }

        private unsafe bool TryGetBase64Images(string name, GetCacheIconCallback getCache, CacheIconCallback cacheIcon, out IEnumerable<Image> images)
        {
#if NETCOREAPP
            if (System.Buffers.Text.Base64.IsValid(name))
            {
                var buffer = MemoryMarshal.AsBytes(name.AsSpan());
                fixed (byte* nameLocal = buffer)
                {
                    using UnmanagedMemoryStream bufferStream = new(nameLocal, 0, buffer.Length, FileAccess.Read);
                    // Instead of prefilling the buffer like in .NET Framework, just convert the Base64-buffer on-the-fly.
                    using var memoryStream = Encoding.CreateTranscodingStream(bufferStream, Encoding.Unicode, Encoding.UTF8);
                    using CryptoStream imageStream = new(memoryStream, new FromBase64Transform(), CryptoStreamMode.Read);
#else
            try
            {
                using (MemoryStream imageStream = new(Convert.FromBase64String(name), false))
                {
#endif
                    return TryGetImages(imageStream, getCache, cacheIcon, out images);
                }
            }
#if !NETCOREAPP
            catch { /* We don't have an easy way of validating Base64 input. Let it error out. */ }
#endif

            images = null;
            return false;
        }

        private bool TryGetImages(Stream stream, GetCacheIconCallback getCache, CacheIconCallback cacheIcon, out IEnumerable<Image> images)
        {
            var source = Image.FromStream(stream);

            if (ImageFormat.Gif.Equals(source.RawFormat))
            {
                // Gif cannot be cached/duplicated/or anything else, really.
                // underlying stream must not be closed (learned the hard way).
                //dispose = false;
                cacheIcon(IconCache, source.Width, source);
                images = [source];
                return true;
            }

            //dispose = true;
            using (source)
            {
                if (ImageFormat.Icon.Equals(source.RawFormat))
                {
                    if (!stream.CanSeek)
                    {
                        images = null;
                        return false;
                    }

                    source.Dispose();
                    stream.Position = 0;
                    GDIIcon icon = new(stream);
                    foreach (var item in icon.Frames)
                    {
                        cacheIcon(IconCache, item.Width, item);
                    }
                    images = icon.Frames;
                    return true;
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
                    return true;
                }
                else
                {
                    Bitmap copy = new(source);
                    copy.SetResolution(96, 96);
                    cacheIcon(IconCache, copy.Width, copy);
                    images = [copy];
                    return true;
                }
            }

        }
    }
}
