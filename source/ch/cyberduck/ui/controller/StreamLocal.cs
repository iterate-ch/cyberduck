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
using ch.cyberduck.core;

namespace Ch.Cyberduck.Ui.Controller
{
    /// <summary>
    /// Stream-based Local implementation which is used for drag'n'drop operations to Windows Explorer
    /// </summary>
    public class StreamLocal : Local
    {
        private readonly java.io.OutputStream _os;

        public StreamLocal(String path, java.io.OutputStream os)
            : base(path)
        {
            _os = os;
        }

        public override bool exists()
        {
            return false;
        }

        public override void setIcon(int i)
        {
            ;
        }

        public override void trash()
        {
            ;
        }

        public override bool open()
        {
            return false;
        }

        public override void bounce()
        {
            ;
        }

        public override void setQuarantine(string str1, string str2)
        {
            ;
        }

        public override void setWhereFrom(string str)
        {
            ;
        }

        public override java.io.OutputStream getOutputStream(bool resume)
        {
            return _os;
        }

        public override void writeUnixPermission(Permission p, bool b)
        {
            ;
        }
    }
}