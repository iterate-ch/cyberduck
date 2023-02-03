using java.lang;

namespace java.util
{
    public static class CollectionExtensions
    {
        public static IterableExtensions.IterableEnumerator GetEnumerator(this Collection collection) => ((Iterable)collection).GetEnumerator();
    }
}
