using java.lang;

namespace java.util;

public static class UtilExtensions
{
    public static IterableEnumerator GetEnumerator(this Collection collection) => ((Iterable)collection).GetEnumerator();

    public static IterableEnumerator<T> GetEnumerator<T>(this Collection collection) => ((Iterable)collection).GetEnumerator<T>();

    public static UtilCollection<T> ToCollection<T>(this Collection collection) => new(collection);

    public static UtilList<T> ToList<T>(this List list) => new(list);
}
