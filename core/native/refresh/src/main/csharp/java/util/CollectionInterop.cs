using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace java.util
{
    public class CollectionEnumerable<T> : IEnumerable<T>
    {
        private readonly Collection collection;

        public struct Enumerator : IEnumerator<T>
        {
            private readonly Collection collection;
            private T current;
            private Iterator iterator;

            public T Current => current;

            object IEnumerator.Current => current;

            public Enumerator(Collection collection)
            {
                this.collection = collection;
            }

            public void Dispose()
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

        public CollectionEnumerable(Collection collection)
        {
            this.collection = collection;
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
