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
using System.IO;
using java.io;

namespace Ch.Cyberduck.Core
{
    /// <summary>
    /// Java OutputStream wrapper for a .NET stream
    /// </summary>
    public class DelegateOutputStream : OutputStream
    {
        private readonly Stream _stream;

        public DelegateOutputStream(Stream stream)
        {
            _stream = stream;
        }

        public override void write(byte[] b, int off, int len)
        {
            _stream.Write(b, off, len);
        }

        public override void write(int i)
        {
            _stream.WriteByte((byte) i);
        }
    }
}