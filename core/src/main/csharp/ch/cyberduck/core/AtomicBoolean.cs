// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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

using System.Threading;

namespace Ch.Cyberduck.Core
{
    /// <summary>
    /// Provides non-blocking, thread-safe access to a boolean value.
    /// </summary>
    public class AtomicBoolean
    {
        private const int VALUE_FALSE = 0;
        private const int VALUE_TRUE = 1;

        private int _currentValue;

        public AtomicBoolean(bool initialValue)
        {
            _currentValue = BoolToInt(initialValue);
        }

        public bool Value
        {
            get
            {
                return IntToBool(Interlocked.Add(
                    ref _currentValue, 0));
            }
        }

        private int BoolToInt(bool value)
        {
            return value ? VALUE_TRUE : VALUE_FALSE;
        }

        private bool IntToBool(int value)
        {
            return value == VALUE_TRUE;
        }

        /// <summary>
        /// Sets the boolean value.
        /// </summary>
        /// <param name="newValue"></param>
        /// <returns>The original value.</returns>
        public bool SetValue(bool newValue)
        {
            return IntToBool(
                Interlocked.Exchange(ref _currentValue,
                                     BoolToInt(newValue)));
        }

        /// <summary>
        /// Compares with expected value and if same, assigns the new value.
        /// </summary>
        /// <param name="expectedValue"></param>
        /// <param name="newValue"></param>
        /// <returns>True if able to compare and set, otherwise false.</returns>
        public bool CompareAndSet(bool expectedValue,
                                  bool newValue)
        {
            int expectedVal = BoolToInt(expectedValue);
            int newVal = BoolToInt(newValue);
            return Interlocked.CompareExchange(
                ref _currentValue, newVal, expectedVal) == expectedVal;
        }
    }
}