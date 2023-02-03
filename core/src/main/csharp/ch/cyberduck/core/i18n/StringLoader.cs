//
// Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
// http://cyberduck.io/
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
// feedback@cyberduck.io
//

using System;
using System.Runtime.InteropServices;
using Windows.Win32;
using static Windows.Win32.CorePInvoke;

namespace Ch.Cyberduck.Core.I18n
{
    public class StringLoader : IDisposable
    {
        private readonly FreeLibrarySafeHandle shell32;
        private bool disposedValue;

        public StringLoader()
        {
            shell32 = LoadLibrary("shell32.dll");
        }

        ~StringLoader()
        {
            Dispose(disposing: false);
        }

        public void Dispose()
        {
            Dispose(disposing: true);
            GC.SuppressFinalize(this);
        }

        public string GetString(uint uiStringId)
        {
            int length = LoadString(shell32, uiStringId, out var lpBuffer);
            if (length == 0)
            {
                Marshal.ThrowExceptionForHR(Marshal.GetHRForLastWin32Error());
            }
            return lpBuffer.ToString(length);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (!disposedValue)
            {
                shell32.Dispose();
                disposedValue = true;
            }
        }
    }
}
