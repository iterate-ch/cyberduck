using java.lang;
using System.Collections;
using System.Collections.Generic;

namespace java.util;

public class UtilCollection<T>(Collection collection) : ICollection<T>
{
    public int Count => collection.size();

    public bool IsReadOnly => false;

    public void Add(T item) => collection.add(item);

    public void Clear() => collection.clear();

    public bool Contains(T item) => collection.contains(item);

    public void CopyTo(T[] array, int arrayIndex)
    {
        foreach (T item in collection)
        {
            array[arrayIndex++] = item;
        }
    }

    public IterableEnumerator<T> GetEnumerator() => IterableExtensions.GetEnumerator<T>(collection);

    public bool Remove(T item) => collection.remove(item);

    IEnumerator<T> IEnumerable<T>.GetEnumerator() => GetEnumerator();

    IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();
}
