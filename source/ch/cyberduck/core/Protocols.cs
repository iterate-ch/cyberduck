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

using ch.cyberduck.core;
using ch.cyberduck.core.ftp;
using ch.cyberduck.core.s3;
using ch.cyberduck.core.sftp;

namespace Ch.Cyberduck.Core
{
    internal sealed class Protocols
    {
        public static readonly Protocol FTP = new FTPProtocol();
        public static readonly Protocol S3_SSL = new S3Protocol();
        public static readonly Protocol SFTP = new SFTPProtocol();

        private Protocols()
        {
        }
    }
}