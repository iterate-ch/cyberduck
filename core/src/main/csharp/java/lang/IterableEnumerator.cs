using java.util;
using System.Collections;
using System.Collections.Generic;

namespace java.lang
{
    public struct IterableEnumerator(Iterable iterable) : IEnumerator
    {
        private object current;
        private Iterator iterator;

        public readonly object Current => current;

        public bool MoveNext()
        {
            iterator ??= iterable.iterator();
            if (iterator.hasNext())
            {
                current = iterator.next();
                return true;
            }

            return false;
        }

        public void Reset()
        {
            iterator = null;
        }
    }

    public struct IterableEnumerator<T>(Iterable iterable) : IEnumerator<T>
    {
        private T current;
        private Iterator iterator;

        public readonly T Current => current;

        readonly object IEnumerator.Current => current;

        public readonly void Dispose()
        { }

        public bool MoveNext()
        {
            iterator ??= iterable.iterator();
            if (iterator.hasNext())
            {
                current = (T)iterator.next();
                return true;
            }

            return false;
        }

        public void Reset()
        {
            iterator = null;
        }
    }
}
