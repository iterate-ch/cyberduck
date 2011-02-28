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

namespace Ch.Cyberduck.Ui.Winforms.Serializer
{
    public class ProtocolPlistReader : PlistReader<Profile>
    {
        public static void Register()
        {
            ProtocolReaderFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        public override Profile deserialize(XmlNode dictNode)
        {
            return new Profile(dictNode);
        }

        private class Factory : ProtocolReaderFactory
        {
            protected override object create()
            {
                return new ProtocolPlistReader();
            }
        }
    }
}

