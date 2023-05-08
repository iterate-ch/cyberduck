using System;

namespace java.lang
{
    public static partial class IterableExtensions
    {
        public static IterableEnumerator GetEnumerator(this Iterable iterable) => new(iterable);

        public static IterableEnumerator<T> GetEnumerator<T>(this Iterable iterable) => new(iterable);
    }
}
