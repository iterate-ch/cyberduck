using java.util;
using System.Collections;
using System.Collections.Generic;

namespace java.lang
{
    public struct IterableEnumerator : IEnumerator
    {
        private readonly Iterable iterable;
        private object current;
        private Iterator iterator;

        public IterableEnumerator(Iterable iterable)
        {
            this.iterable = iterable;
        }

        public object Current => current;

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

    public struct IterableEnumerator<T> : IEnumerator<T>
    {
        private readonly Iterable iterable;
        private T current;
        private Iterator iterator;

        public IterableEnumerator(Iterable iterable)
        {
            this.iterable = iterable;
        }

        public T Current => current;

        object IEnumerator.Current => current;

        public void Dispose()
        {
        }

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
