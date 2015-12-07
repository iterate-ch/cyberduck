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
using System.Collections.Generic;

namespace Ch.Cyberduck.Core.Collections
{
    public class IndexedLinkedList<T>
    {
        private readonly LinkedList<T> data = new LinkedList<T>();
        private readonly Dictionary<T, LinkedListNode<T>> index = new Dictionary<T, LinkedListNode<T>>();

        public int Count
        {
            get { return data.Count; }
        }

        public T First
        {
            get { return data.First.Value; }
        }

        public void Add(T value)
        {
            index[value] = data.AddLast(value);
        }

        public void RemoveFirst()
        {
            index.Remove(data.First.Value);
            data.RemoveFirst();
        }

        public void Remove(T value)
        {
            LinkedListNode<T> node;
            if (index.TryGetValue(value, out node))
            {
                data.Remove(node);
                index.Remove(value);
            }
        }

        public void Clear()
        {
            data.Clear();
            index.Clear();
        }
    }
}