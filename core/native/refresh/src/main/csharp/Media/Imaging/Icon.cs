using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace Ch.Cyberduck.Core.Refresh.Media.Imaging;

public abstract class Icon<T>()
{
    public List<T> Frames { get; } = [];

    protected Icon(Stream stream) : this()
    {
        if (!stream.CanSeek)
        {
            throw new NotSupportedException();
        }

        var icons = ICONDIR.Parse(stream);
        Array.Sort(icons.Entries, (l, r) => l.Offset.CompareTo(r.Offset));
        using MemoryStream buffer = new();
        new ICONDIR.HEADER(0, 1, 1).Write(buffer);
        var origin = buffer.Length;
        foreach (ref readonly var icon in icons.Entries.AsSpan())
        {
            buffer.SetLength(origin);
            (icon with { Offset = 22 }).Write(buffer);
            var moveOffset = icon.Offset - stream.Position;
            if (moveOffset < 0)
            {
                throw new InvalidDataException();
            }

            if (moveOffset > 0)
            {
                stream.Seek(moveOffset, SeekOrigin.Current);
            }

            stream.CopyToLimit(buffer, icon.Length);
            buffer.Position = 0;
            Frames.Add(Decode(buffer));
        }
    }

    public bool Save(Stream stream)
    {
        try
        {
            return Save(this, stream);
        }
        catch
        {
            return false;
        }

        static bool Save(Icon<T> @this, Stream stream)
        {
            using MemoryStream imageDataStream = new();
            var frames = @this.Frames;
            var entries = new ICONDIRENTRY[frames.Count];
            for (int i = 0; i < entries.Length; i++)
            {
                var offset = imageDataStream.Position;
                @this.Encode(frames[i], imageDataStream, out var size);
                var length = imageDataStream.Position - offset;
                if (size.Width > 256 || size.Height > 256)
                {
                    return false;
                }

                entries[i] = new((byte)(size.Width & 0xFF), (byte)(size.Height & 0xFF), 0, 0, 0, 32, (int)length, (int)offset);
            }

            Array.Sort(entries, (l, r) => l.Width.CompareTo(r.Width));
            var headerLength = ICONDIR.HEADER.Size + entries.Length * ICONDIRENTRY.Size;
            foreach (ref var entry in entries.AsSpan())
            {
                entry.Offset += headerLength;
            }

            if (!imageDataStream.TryGetBuffer(out var buffer))
            {
                return false;
            }

            ICONDIR iconFile = new(0, 1, entries);
            iconFile.Write(stream);
            stream.Write(buffer.Array, buffer.Offset, buffer.Count);
            return true;
        }
    }

    protected abstract T Decode(Stream stream);

    protected abstract void Encode(T image, Stream stream, out (int Width, int Height) size);
}

file readonly record struct ICONDIR(short Reserved, short Type, ICONDIRENTRY[] Entries)
{
    public ICONDIRENTRY[] Entries { get; } = Entries;

    public int Length { get; } = HEADER.Size + ICONDIRENTRY.Size * Entries.Length;

    public short Reserved { get; } = Reserved;

    public short Type { get; } = Type;

    public static ICONDIR Parse(Stream stream)
    {
        var header = HEADER.Parse(stream);
        var entries = new ICONDIRENTRY[header.Count];
        for (int i = 0; i < header.Count; i++)
        {
            entries[i] = ICONDIRENTRY.Parse(stream);
        }

        return new(header.Reserved, header.Type, entries);
    }

    public void Write(Stream stream)
    {
        using BinaryWriter writer = new(stream, Encoding.Default, true);
        writer.Write(Reserved);
        writer.Write(Type);
        writer.Write((short)Entries.Length);
        foreach (ref readonly var item in Entries.AsSpan())
        {
            item.Write(stream);
        }
    }

    public record struct HEADER(short Reserved, short Type, short Count)
    {
        public const int Size = 6;

        public short Count { get; set; } = Count;

        public short Reserved { get; set; } = Reserved;

        public short Type { get; set; } = Type;

        public static HEADER Parse(Stream stream)
        {
            using BinaryReader reader = new(stream, Encoding.Default, true);
            return new(
                Reserved: reader.ReadInt16(),
                Type: reader.ReadInt16(),
                Count: reader.ReadInt16());
        }

        public readonly void Write(Stream stream)
        {
            using BinaryWriter writer = new(stream, Encoding.Default, true);
            writer.Write(Reserved);
            writer.Write(Type);
            writer.Write(Count);
        }
    }
}

file record struct ICONDIRENTRY(
    byte Width,
    byte Height,
    byte ColorCount,
    byte Reserved,
    short Planes,
    short BitCount,
    int Length,
    int Offset)
{
    public const int Size = 16;

    public short BitCount { get; set; } = BitCount;

    public byte ColorCount { get; set; } = ColorCount;

    public byte Height { get; set; } = Height;

    public int Length { get; set; } = Length;

    public int Offset { get; set; } = Offset;

    public short Planes { get; set; } = Planes;

    public byte Reserved { get; set; } = Reserved;

    public byte Width { get; set; } = Width;

    public static ICONDIRENTRY Parse(Stream stream)
    {
        using BinaryReader reader = new(stream, Encoding.Default, true);
        return new(
            Width: reader.ReadByte(), Height: reader.ReadByte(), ColorCount: reader.ReadByte(), Reserved: reader.ReadByte(),
            Planes: reader.ReadInt16(), BitCount: reader.ReadInt16(),
            Length: reader.ReadInt32(),
            Offset: reader.ReadInt32());
    }

    public readonly void Write(Stream stream)
    {
        using BinaryWriter writer = new(stream, Encoding.Default, true);
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
