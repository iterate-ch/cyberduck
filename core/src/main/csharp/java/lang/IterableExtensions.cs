using java.util;
using System;
using System.Collections;

namespace java.lang
{
    public static class IterableExtensions
    {
        public static IterableEnumerator GetEnumerator(this Iterable iterable) => new(iterable);

        public struct IterableEnumerator : IEnumerator
        {
            private object current;
            private Iterator iterator;

            public IterableEnumerator(Iterable list)
            {
                iterator = list.iterator();
                current = default;
            }

            public object Current => current;

            public bool MoveNext()
            {
                if (iterator.hasNext())
                {
                    current = iterator.next();
                    return true;
                }
                return false;
            }

            public void Reset() => throw new NotImplementedException();
        }
    }
}
