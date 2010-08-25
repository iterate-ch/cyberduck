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
using System.Xml;
using ch.cyberduck.core;
using ch.cyberduck.core.serializer;
using Ch.Cyberduck.Ui.Winforms.Serializer;

namespace Ch.Cyberduck.Ui.Controller
{
    public class HostPlistReader : PlistReader<Host>
    {
        public static void Register()
        {
            HostReaderFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        public override Host deserialize(XmlNode dictNode)
        {
            return new Host(dictNode);
        }

        private class Factory : HostReaderFactory
        {
            protected override object create()
            {
                return new HostPlistReader();
            }
        }
    }
}