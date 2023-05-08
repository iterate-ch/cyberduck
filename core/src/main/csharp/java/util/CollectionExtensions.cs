using java.lang;

namespace java.util
{
    public static class CollectionExtensions
    {
        public static IterableEnumerator GetEnumerator(this Collection collection) => ((Iterable)collection).GetEnumerator();

        public static IterableEnumerator<T> GetEnumerator<T> (this Collection collection) => ((Iterable)collection).GetEnumerator<T>();
    }
}
