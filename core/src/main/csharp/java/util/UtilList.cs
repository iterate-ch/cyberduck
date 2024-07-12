using System.Collections.Generic;

namespace java.util;

public class UtilList<T>(List list) : UtilCollection<T>(list), IList<T>
{
    public T this[int index]
    {
        get => (T)list.get(index);
        set => list.set(index, value);
    }

    public int IndexOf(T item) => list.indexOf(item);

    public void Insert(int index, T item) => list.set(index, item);

    public void RemoveAt(int index) => list.remove(index);
}
