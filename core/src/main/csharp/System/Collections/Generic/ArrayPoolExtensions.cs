// Copyright(c) 2002 - 2024 iterate GmbH. All rights reserved.
// https://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

using System.Buffers;

namespace System.Collections.Generic;

public static class ArrayPoolExtensions
{
    public static PooledCollectionCopy<T> CopyToPool<T>(this ICollection<T> collection, bool clearArray = false)
    {
        var count = collection.Count;
        var buffer = ArrayPool<T>.Shared.Rent(count);
        collection.CopyTo(buffer, 0);
        return new(buffer, count, clearArray);
    }

    public ref struct PooledCollectionCopy<T>
    {
        private readonly bool clearArray;
        private readonly int length;
        private T[] values;

        public readonly Memory<T> Memory => new(values, 0, length);

        public readonly Span<T> Span => new(values, 0, length);

        internal PooledCollectionCopy(T[] values, int length, bool clearArray)
        {
            this.values = values;
            this.length = length;
            this.clearArray = clearArray;
        }

        public void Dispose()
        {
            ArrayPool<T>.Shared.Return(values, clearArray);
            values = null;
        }
    }
}
