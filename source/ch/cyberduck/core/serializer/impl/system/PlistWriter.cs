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

using System.Xml;
using ch.cyberduck.core;
using ch.cyberduck.core.serializer;
using java.util;
using Collection = java.util.Collection;

namespace Ch.Cyberduck.Core.Serializer.Impl
{
    public class PlistWriter : Writer
    {
        public void write(Collection collection, ch.cyberduck.core.Local file)
        {
            XmlDocument plistDocument = GetPlistDocument();
            XmlNode plistElement = plistDocument.LastChild;
            XmlNode arrayElement = plistDocument.CreateElement("array");
            plistElement.AppendChild(arrayElement);

            for (Iterator i = collection.iterator(); i.hasNext();)
            {
                Serializable s = (Serializable) i.next();
                XmlNode impNode = plistDocument.ImportNode((XmlNode) s.serialize(SerializerFactory.get()), true);
                arrayElement.AppendChild(impNode);
            }
            plistDocument.Save(file.getAbsolute());
        }

        public void write(Serializable bookmark, ch.cyberduck.core.Local file)
        {
            XmlDocument plistDocument = GetPlistDocument();
            XmlNode plistElement = plistDocument.LastChild;

            XmlNode impNode = plistDocument.ImportNode((XmlNode) bookmark.serialize(SerializerFactory.get()), true);
            plistElement.AppendChild(impNode);

            plistDocument.Save(file.getAbsolute());
        }

        private static XmlDocument GetPlistDocument()
        {
            XmlDocument xmlDoc = new XmlDocument();
            XmlDeclaration declaration = xmlDoc.CreateXmlDeclaration("1.0", "UTF-8", string.Empty);
            XmlNode plist = xmlDoc.CreateElement("plist");
            xmlDoc.AppendChild(declaration);
            //xmlDoc.AppendChild(type);
            xmlDoc.AppendChild(plist);

            return xmlDoc;
        }
    }
}