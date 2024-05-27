using System.Collections;
using System.Collections.Generic;

namespace java.util
{
    public class CollectionEnumerable<T>(Collection collection) : IEnumerable<T>
    {
        public struct Enumerator(Collection collection) : IEnumerator<T>
        {
            private T current;
            private Iterator iterator;

            public readonly T Current => current;

            readonly object IEnumerator.Current => current;

            public readonly void Dispose()
            {
            }

            public bool MoveNext()
            {
                if (iterator is null)
                {
                    Reset();
                }

                bool next = iterator.hasNext();
                if (next)
                {
                    current = (T)iterator.next();
                }

                return next;
            }

            public void Reset()
            {
                iterator = collection.iterator();
            }
        }

        public Enumerator GetEnumerator() => new(collection);

        IEnumerator<T> IEnumerable<T>.GetEnumerator() => GetEnumerator();

        IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();
    }


    public static class CollectionInterop
    {
        public static CollectionEnumerable<T> AsEnumerable<T>(this Collection collection) => new(collection);
    }
}
