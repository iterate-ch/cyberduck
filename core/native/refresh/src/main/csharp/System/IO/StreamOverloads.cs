using System.Buffers;

namespace System.IO
{
    public static class StreamOverloads
    {
        public static void CopyToLimit(this Stream @this, Stream destination) => CopyToLimit(@this, destination, destination.Length - destination.Position);

        public static void CopyToLimit(this Stream @this, Stream destination, long limit)
        {
            var pool = ArrayPool<byte>.Shared;
            byte[] buffer = pool.Rent(4096);
            try
            {
                CopyToLimit(@this, destination, limit, buffer);
            }
            finally
            {
                pool.Return(buffer);
            }
        }

        public static void CopyToLimit(this Stream @this, Stream destination, long limit, byte[] buffer)
        {
            int count;
            while (limit > 0 && (count = @this.Read(buffer, 0, (int)Math.Min(limit, buffer.Length))) != 0)
            {
                limit -= count;
                destination.Write(buffer, 0, count);
            }
        }
    }
}
