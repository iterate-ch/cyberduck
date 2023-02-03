using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Text;
using static System.Runtime.CompilerServices.Unsafe;

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
                    var images = ReadIconStream(stream);
                    foreach (var item in images)
                    {
                        cacheIcon(IconCache, item.Width, item);
                    }
                    return images;
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

        private List<Image> ReadIconStream(in Stream stream)
        {
            var dir = ICONDIR.Parse(stream);
            for (int i = 0; i < dir.Count; i++)
            {
                dir.Entries[i] = ICONDIRENTRY.Parse(stream);
            }

            List<Image> images = new();
            foreach (var item in dir.Entries)
            {
                ICONDIR temp = new(dir.Reserved, dir.Type, 1, new[] { item });
                ref ICONDIRENTRY entry = ref temp.Entries[0];
                using MemoryStream memory = new();
                entry.Offset = temp.Write(memory);
                entry.Write(memory);
                stream.Seek(item.Offset, SeekOrigin.Begin);
                stream.CopyToLimit(memory, item.Length);
                memory.Seek(0, SeekOrigin.Begin);
                using Icon icon = new(memory);
                images.Add(icon.ToBitmap());
            }
            return images;
        }

        private struct ICONDIR
        {
            public ICONDIR(short reserved, short type, short count, ICONDIRENTRY[] entries)
            {
                Reserved = reserved;
                Type = type;
                Count = count;
                Entries = entries;
            }

            public short Count { get; set; }

            public ICONDIRENTRY[] Entries { get; }

            public short Reserved { get; set; }

            public short Type { get; set; }

            public static ICONDIR Parse(in Stream stream)
            {
                using var reader = new BinaryReader(stream, Encoding.Default, true);
                var reserved = reader.ReadInt16();
                var type = reader.ReadInt16();
                var count = reader.ReadInt16();
                return new ICONDIR(reserved, type, count, new ICONDIRENTRY[count]);
            }

            public int Write(in Stream stream)
            {
                using var writer = new BinaryWriter(stream, Encoding.Default, true);
                writer.Write(Reserved);
                writer.Write(Type);
                writer.Write(Count);
                return 6 + Entries.Length * SizeOf<ICONDIRENTRY>();
            }
        }

        private struct ICONDIRENTRY
        {
            public ICONDIRENTRY(byte width, byte height, byte colorCount, byte reserved, short planes, short bitCount, int length, int offset)
            {
                Width = width;
                Height = height;
                ColorCount = colorCount;
                Reserved = reserved;
                Planes = planes;
                BitCount = bitCount;
                Length = length;
                Offset = offset;
            }

            public short BitCount { get; set; }

            public byte ColorCount { get; set; }

            public byte Height { get; set; }

            public int Length { get; set; }

            public int Offset { get; set; }

            public short Planes { get; set; }

            public byte Reserved { get; set; }

            public byte Width { get; set; }

            public static ICONDIRENTRY Parse(in Stream stream)
            {
                using var reader = new BinaryReader(stream, Encoding.Default, true);
                return new ICONDIRENTRY(
                    reader.ReadByte(), reader.ReadByte(),
                    reader.ReadByte(), reader.ReadByte(),
                    reader.ReadInt16(), reader.ReadInt16(),
                    reader.ReadInt32(), reader.ReadInt32());
            }

            public void Write(in Stream stream)
            {
                using var writer = new BinaryWriter(stream, Encoding.Default, true);
                writer.Write(Width);
                writer.Write(Height);
                writer.Write(ColorCount);
                writer.Write(Reserved);
                writer.Write(Planes);
                writer.Write(BitCount);
                writer.Write(Length);
                writer.Write(Offset);
            }
        }
    }
}
