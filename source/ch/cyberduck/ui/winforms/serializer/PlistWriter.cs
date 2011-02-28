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
using System.Xml;
using ch.cyberduck.core;
using ch.cyberduck.core.serializer;
using java.util;
using Collection = java.util.Collection;

namespace Ch.Cyberduck.Ui.Winforms.Serializer
{
    public class PlistWriter : Writer
    {
        public void write(Collection collection, Local file)
        {
            XmlDocument plistDocument = GetPlistDocument();
            XmlNode plistElement = plistDocument.LastChild;
            XmlNode arrayElement = plistDocument.CreateElement("array");
            plistElement.AppendChild(arrayElement);

            for (Iterator i = collection.iterator(); i.hasNext();)
            {
                Serializable s = (Serializable) i.next();
                XmlNode impNode = plistDocument.ImportNode((XmlNode) s.getAsDictionary(), true);
                arrayElement.AppendChild(impNode);
            }
            plistDocument.Save(file.getAbsolute());
        }

        public void write(Serializable bookmark, Local file)
        {
            XmlDocument plistDocument = GetPlistDocument();
            XmlNode plistElement = plistDocument.LastChild;

            XmlNode impNode = plistDocument.ImportNode((XmlNode) bookmark.getAsDictionary(), true);
            plistElement.AppendChild(impNode);

            plistDocument.Save(file.getAbsolute());
        }

        private static XmlDocument GetPlistDocument()
        {
            XmlDocument xmlDoc = new XmlDocument();
            XmlDeclaration declaration = xmlDoc.CreateXmlDeclaration("1.0", "UTF-8", string.Empty);
            // todo takes a very long time...goes to the internet probably
            // 20100623: try with XmlResolver = null
            // http://social.msdn.microsoft.com/Forums/en-US/xmlandnetfx/thread/45ed2191-2613-42e3-91d7-9a9a5f941309
            //XmlDocumentType type = xmlDoc.CreateDocumentType("plist", "-//Apple//DTD PLIST 1.0//EN",
            //                                                 "http://www.apple.com/DTDs/PropertyList-1.0.dtd", null);
            XmlNode plist = xmlDoc.CreateElement("plist");
            xmlDoc.AppendChild(declaration);
            //xmlDoc.AppendChild(type);
            xmlDoc.AppendChild(plist);

            return xmlDoc;
        }

        public static void Register()
        {
            HostWriterFactory.addFactory(Factory.NATIVE_PLATFORM, new HostFactory());
            TransferWriterFactory.addFactory(Factory.NATIVE_PLATFORM, new TransferFactory());
            ProtocolWriterFactory.addFactory(Factory.NATIVE_PLATFORM, new ProtocolFactory());
        }

        private class HostFactory : HostWriterFactory
        {
            protected override object create()
            {
                return new PlistWriter();
            }
        }

        private class TransferFactory : TransferWriterFactory
        {
            protected override object create()
            {
                return new PlistWriter();
            }
        }

        private class ProtocolFactory : ProtocolWriterFactory
        {
            protected override object create()
            {
                return new PlistWriter();
            }
        }
    }
}