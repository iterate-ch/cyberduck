using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Caching;
using System.Threading;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    public class IconCache
    {
        private static readonly CacheItemPolicy AutoEvictUnused = new()
        {
            SlidingExpiration = TimeSpan.FromSeconds(30),
            RemovedCallback = OnCacheEvict,
        };

        // DE0006: Non-generic collections shouldn't be used
        // Don't care here, as this basically comes down to maintaining two different platforms
        // (WPF/WinForms) within a single cache.
        private readonly Dictionary<(object Key, string Classifier, bool Default, int Size), Hashtable> cache = [];
        private readonly ReaderWriterLockSlim writer = new(LockRecursionPolicy.SupportsRecursion);

        public void CacheIcon<T>(object key, int size, string classifier = default)
        {
            var e = (key, classifier, true, 0);
            Hashtable list = GetCache(e);
            list[typeof(T)] = size;
        }

        public void CacheIcon<T>(object key, int size, T image, string classifier = default)
        {
            var e = (key, classifier, false, size);
            Hashtable list = GetCache(e);
            list[typeof(T)] = image;
        }

        public IEnumerable<T> Filter<T>(Predicate<(object Key, string Classifier, int Size)> filter)
        {
            using (ReadLock())
            {
                return cache.Where(kv => !kv.Key.Default)
                    .Where(kv => filter((kv.Key.Key, kv.Key.Classifier, kv.Key.Size)))
                    .Select(x => x.Value)
                    .Select(x => (T)x[typeof(T)])
                    .Where(x => x is not null).ToList();
            }
        }

        public Hashtable GetCache(in (object, string, bool, int) key)
        {
            using (UpgradeableReadLock())
            {
                if (!cache.TryGetValue(key, out var list))
                {
                    list = GetCacheExclusive(key);
                }
                return list;
            }
        }

        public Hashtable GetCacheExclusive(in (object, string, bool, int) key)
        {
            using (WriteLock())
            {
                if (!cache.TryGetValue(key, out var list))
                {
                    cache[key] = list = new();
                }
                return list;
            }
        }

        public ReaderWriterLockSlimExtensions.ReadLock ReadLock() => writer.UseReadLock();

        public void Temporary(string key, string classifier = default)
        {
            var cacheKey = $"{key}{(classifier is { } v ? $"{{{v}}}" : "")}";
            if (MemoryCache.Default.Get(cacheKey) is not EvictCacheItem)
            {
                MemoryCache.Default.Add(cacheKey, new EvictCacheItem(this, key, classifier), AutoEvictUnused);
            }
        }

        /// <summary>
        /// Returns image with default registered size.
        /// </summary>
        public bool TryGetIcon<T>(object Key, out T image, string Classifier = default)
        {
            image = default;
            Hashtable list;
            using (ReadLock())
            {
                if (!cache.TryGetValue((Key, Classifier, true, 0), out list))
                {
                    return false;
                }
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
            Hashtable list;
            using (ReadLock())
            {
                if (!cache.TryGetValue((Key, classifier, false, size), out list))
                {
                    return false;
                }
            }
            image = (T)list[typeof(T)];
            return image is not null;
        }

        public ReaderWriterLockSlimExtensions.UpgradeableReadLock UpgradeableReadLock() => writer.UseUpgradeableReadLock();

        public ReaderWriterLockSlimExtensions.WriteLock WriteLock() => writer.UseWriteLock();

        private static void OnCacheEvict(CacheEntryRemovedArguments arguments)
        {
            EvictCacheItem item = (EvictCacheItem)arguments.CacheItem.Value;
            using (item.Cache.WriteLock())
            {
                var cacheKeys = item.Cache.cache.Keys
                    .Where(x => Equals(item.Key, x.Key) && string.Equals(item.Classifier, x.Classifier))
                    .ToArray();
                foreach (var key in cacheKeys)
                {
                    var local = item.Cache.cache[key];
                    item.Cache.cache.Remove(key);
                    local.Clear();
                }
            }
        }

        private record class EvictCacheItem(IconCache Cache, string Key, string Classifier = null);
    }
}
