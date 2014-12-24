// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using org.apache.log4j;

namespace Ch.Cyberduck.Core.Serializer.Impl
{
    public abstract class PlistReader<T> : Reader where T : Serializable
    {
        private static readonly Logger Log = Logger.getLogger(typeof (PlistReader<>).Name);

        public Collection readCollection(ch.cyberduck.core.Local file)
        {
            XmlDocument plistDocument = new XmlDocument {XmlResolver = null};
            plistDocument.Load(file.getAbsolute());

            Collection resultCollection = new Collection();

            XmlNodeList dictNodes = plistDocument.SelectNodes("/plist/array/dict");
            foreach (XmlNode node in dictNodes)
            {
                object r = deserialize(node);
                if (null == r)
                {
                    continue;
                }
                resultCollection.add(r);
            }
            return resultCollection;
        }

        public Serializable read(ch.cyberduck.core.Local file)
        {
            XmlDocument plistDocument = new XmlDocument {XmlResolver = null};
            try
            {
                plistDocument.Load(file.getAbsolute());
            }
            catch (XmlException e)
            {
                Log.warn("Error while loading " + file, e);
                return null;
            }

            XmlNode dictNode = plistDocument.SelectSingleNode("/plist/dict");
            return deserialize(dictNode);
        }

        public abstract T deserialize(XmlNode dictNode);
    }
}