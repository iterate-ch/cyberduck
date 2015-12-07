// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 
using System;
using System.Collections;
using System.Collections.Generic;

namespace Ch.Cyberduck.Core.Collections
{
    public class LRUCache<TKey, TValue> : IDictionary<TKey, TValue>
    {
        private readonly int capacity;
        private readonly Dictionary<TKey, TValue> data;
        private readonly ICollection<KeyValuePair<TKey, TValue>> dataAsCollection;
        private readonly IndexedLinkedList<TKey> lruList = new IndexedLinkedList<TKey>();

        public LRUCache(int capacity)
        {
            if (capacity <= 0)
            {
                throw new ArgumentException("capacity should always be bigger than 0");
            }

            data = new Dictionary<TKey, TValue>(capacity);
            dataAsCollection = data;
            this.capacity = capacity;
        }

        public void Add(TKey key, TValue value)
        {
            if (!ContainsKey(key))
            {
                this[key] = value;
            }
            else
            {
                throw new ArgumentException("An attempt was made to insert a duplicate key in the cache.");
            }
        }

        public bool ContainsKey(TKey key)
        {
            return data.ContainsKey(key);
        }

        public ICollection<TKey> Keys
        {
            get { return data.Keys; }
        }

        public bool Remove(TKey key)
        {
            bool existed = data.Remove(key);
            lruList.Remove(key);
            return existed;
        }

        public bool TryGetValue(TKey key, out TValue value)
        {
            return data.TryGetValue(key, out value);
        }

        public ICollection<TValue> Values
        {
            get { return data.Values; }
        }

        public TValue this[TKey key]
        {
            get
            {
                var value = data[key];
                lruList.Remove(key);
                lruList.Add(key);
                return value;
            }
            set
            {
                data[key] = value;
                lruList.Remove(key);
                lruList.Add(key);

                if (data.Count > capacity)
                {
                    Remove(lruList.First);
                }
            }
        }

        public void Add(KeyValuePair<TKey, TValue> item)
        {
            Add(item.Key, item.Value);
        }

        public void Clear()
        {
            data.Clear();
            lruList.Clear();
        }

        public bool Contains(KeyValuePair<TKey, TValue> item)
        {
            return dataAsCollection.Contains(item);
        }

        public void CopyTo(KeyValuePair<TKey, TValue>[] array, int arrayIndex)
        {
            dataAsCollection.CopyTo(array, arrayIndex);
        }

        public int Count
        {
            get { return data.Count; }
        }

        public bool IsReadOnly
        {
            get { return false; }
        }

        public bool Remove(KeyValuePair<TKey, TValue> item)
        {
            bool removed = dataAsCollection.Remove(item);
            if (removed)
            {
                lruList.Remove(item.Key);
            }
            return removed;
        }

        public IEnumerator<KeyValuePair<TKey, TValue>> GetEnumerator()
        {
            return dataAsCollection.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return ((IEnumerable) data).GetEnumerator();
        }
    }
}