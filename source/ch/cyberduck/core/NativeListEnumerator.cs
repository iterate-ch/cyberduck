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
    /// The Native List Enumerator offers an enumerator for a class that implements the IEnumerator interface
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public class NativeListEnumerator<T> : IEnumerator<T>
    {
        private IEnumerator _nativeEnumerator;

        /// <summary>
        /// Initializes a new instance of the NativeListEnumerator class.
        /// </summary>
        /// <param name="nativeEnumeratorParameter">The native enumerator parameter.</param>
        public NativeListEnumerator(IEnumerator nativeEnumeratorParameter)
        {
            _nativeEnumerator = nativeEnumeratorParameter;
        }

        ///<summary>
        ///Gets the element in the collection at the current position of the enumerator.
        ///</summary>
        ///
        ///<returns>
        ///The element in the collection at the current position of the enumerator.
        ///</returns>
        ///
        T IEnumerator<T>.Current
        {
            get { return (T) _nativeEnumerator.Current; }
        }

        ///<summary>
        ///Performs application-defined tasks associated with freeing, releasing, or resetting unmanaged resources.
        ///</summary>
        ///<filterpriority>2</filterpriority>
        public void Dispose()
        {
            _nativeEnumerator = null;
        }

        ///<summary>
        ///Advances the enumerator to the next element of the collection.
        ///</summary>
        ///
        ///<returns>
        ///true if the enumerator was successfully advanced to the next element; false if the enumerator has passed the end of the collection.
        ///</returns>
        ///
        ///<exception cref="T:System.InvalidOperationException">The collection was modified after the enumerator was created. </exception><filterpriority>2</filterpriority>
        public bool MoveNext()
        {
            return _nativeEnumerator.MoveNext();
        }

        ///<summary>
        ///Sets the enumerator to its initial position, which is before the first element in the collection.
        ///</summary>
        ///
        ///<exception cref="T:System.InvalidOperationException">The collection was modified after the enumerator was created. </exception><filterpriority>2</filterpriority>
        public void Reset()
        {
            _nativeEnumerator.Reset();
        }

        /// <summary>
        /// Gets the current element in the collection.
        /// </summary>
        /// <value></value>
        /// <returns>
        /// The current element in the collection.
        /// </returns>
        /// <exception cref="T:System.InvalidOperationException">The enumerator is positioned before the first element of the collection or after the last element. </exception>
        public object Current
        {
            get { return _nativeEnumerator.Current; }
        }
    }
}