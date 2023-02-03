namespace System.IO
{
    public static class StreamOverloads
    {
        public static void CopyToLimit(this Stream @this, Stream destination) => CopyToLimit(@this, destination, destination.Length - destination.Position);

        public static void CopyToLimit(this Stream @this, Stream destination, long limit)
        {
            byte[] array = new byte[81920];
            int count;
            while (limit > 0 && (count = @this.Read(array, 0, (int)Math.Min(limit, array.Length))) != 0)
            {
                limit -= count;
                destination.Write(array, 0, count);
            }
        }
    }
}
