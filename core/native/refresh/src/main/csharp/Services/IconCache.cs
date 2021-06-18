using System;
using System.Collections.Generic;
using System.Linq;

namespace Cyberduck.Core.Refresh.Services
{
    using System.Collections;

    public class IconCache
    {
        // DE0006: Non-generic collections shouldn't be used
        // Don't care here, as this basically comes down to maintaining two different platforms
        // (WPF/WinForms) within a single cache.
        private readonly Dictionary<(object Key, string Classifier, bool Default, int Size), Hashtable> cache = new();

        public void CacheIcon<T>(object key, int size, string classifier = default)
        {
            var e = (key, classifier, true, 0);
            if (!cache.TryGetValue(e, out var list))
            {
                cache[e] = list = new Hashtable();
            }
            list[typeof(T)] = size;
        }

        public void CacheIcon<T>(object key, int size, T image, string classifier = default)
        {
            var e = (key, classifier, false, size);
            if (!cache.TryGetValue(e, out var list))
            {
                cache[e] = list = new Hashtable();
            }
            list[typeof(T)] = image;
        }

        public IEnumerable<T> Filter<T>(Predicate<(object Key, string Classifier, int Size)> filter)
            => cache.Where(kv => !kv.Key.Default).Where(kv => filter((kv.Key.Key, kv.Key.Classifier, kv.Key.Size))).Select(kv => (T)kv.Value[typeof(T)]).Where(x => x is not null);

        /// <summary>
        /// Returns image with default registered size.
        /// </summary>
        public bool TryGetIcon<T>(object Key, out T image, string Classifier = default)
        {
            image = default;
            if (!cache.TryGetValue((Key, Classifier, true, 0), out var list))
            {
                return false;
            }
            if (list[typeof(T)] is not int size)
            {
                return false;
            }
            return TryGetIcon(Key, size, out image, Classifier);
        }

        public bool TryGetIcon<T>(object Key, int size, out T image, string classifier = default)
        {
            image = default;
            if (!cache.TryGetValue((Key, classifier, false, size), out var list))
            {
                return false;
            }
            image = (T)list[typeof(T)];
            return image is not null;
        }
    }
}
