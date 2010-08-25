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
using ch.cyberduck.core.serializer;
using java.util;

namespace Ch.Cyberduck.Ui.Winforms.Serializer
{
    public class PlistDeserializer : Deserializer
    {
        private readonly XmlNode _plistEntry;

        public PlistDeserializer(XmlNode plistEntry)
        {
            _plistEntry = plistEntry;
        }

        public string stringForKey(string key)
        {
            XmlNode foundNode = _plistEntry.SelectSingleNode("key[.='" + key + "']");
            if (null == foundNode)
            {
                return null;
            }
            return foundNode.NextSibling.InnerText;
        }

        public List listForKey(string key)
        {
            XmlNode foundNode = _plistEntry.SelectSingleNode("key[.='" + key + "']");
            if (null == foundNode)
            {
                return null;
            }
            XmlNode arrayNode = foundNode.NextSibling;
            XmlNodeList dictNodes = arrayNode.ChildNodes;
            List dictNodeList = new ArrayList();
            foreach (XmlNode dictNode in dictNodes)
            {
                dictNodeList.add(dictNode);
            }
            return dictNodeList;
        }

        public object objectForKey(string key)
        {
            XmlNode foundNode = _plistEntry.SelectSingleNode("key[.='" + key + "']");
            if (null == foundNode)
            {
                return null;
            }
            return foundNode.NextSibling;
        }

        public static void Register()
        {
            DeserializerFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : DeserializerFactory
        {
            protected override object create()
            {
                return new PlistDeserializer(new XmlDocument());
            }

            protected override Deserializer create(object obj)
            {
                return new PlistDeserializer((XmlNode) obj);
            }
        }
    }
}