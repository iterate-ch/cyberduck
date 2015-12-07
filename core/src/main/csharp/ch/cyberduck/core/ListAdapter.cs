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
// yves@langisch.ch
// 
using System.Collections;
using System.Collections.Generic;

namespace Ch.Cyberduck.Core
{
    /// <summary>
    /// The ListAdapter implements the generic IEnumerable interface in order to allow a 
    /// generic list instantiation of a List that implements the IList interface
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public class ListAdapter<T> : IEnumerable<T>
    {
        private readonly IList _nativeList;

        /// <summary>
        /// Initializes a new instance of the ListAdapter class.
        /// </summary>
        /// <param name="nativeListParameter">The native list parameter.</param>
        public ListAdapter(IList nativeListParameter)
        {
            _nativeList = nativeListParameter;
        }

        ///<summary>
        ///Returns an enumerator that iterates through the collection.
        ///</summary>
        ///
        ///<returns>
        ///A <see cref="T:System.Collections.Generic.IEnumerator`1"></see> that can be used to iterate through the collection.
        ///</returns>
        ///<filterpriority>1</filterpriority>
        IEnumerator<T> IEnumerable<T>.GetEnumerator()
        {
            return new NativeListEnumerator<T>(GetEnumerator());
        }

        ///<summary>
        ///Returns an enumerator that iterates through a collection.
        ///</summary>
        ///
        ///<returns>
        ///An <see cref="T:System.Collections.IEnumerator"></see> object that can be used to iterate through the collection.
        ///</returns>
        ///<filterpriority>2</filterpriority>
        public IEnumerator GetEnumerator()
        {
            return _nativeList.GetEnumerator();
        }
    }
}