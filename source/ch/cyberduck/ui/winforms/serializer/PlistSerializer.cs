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

namespace Ch.Cyberduck.Ui.Winforms.Serializer
{
    public class PlistSerializer : ch.cyberduck.core.serializer.Serializer
    {
        private readonly XmlNode _lastNode;
        private readonly XmlNode _rootNode;
        private readonly XmlDocument _xmlDoc = new XmlDocument();

        public PlistSerializer()
        {
            _rootNode = _xmlDoc.CreateElement("dict");
            _lastNode = _xmlDoc.AppendChild(_rootNode);
        }

        public void setObjectForKey(Serializable value, string key)
        {
            // key element
            XmlElement keyElement = _xmlDoc.CreateElement("key");
            XmlText keyTextNode = _xmlDoc.CreateTextNode(key);
            keyElement.AppendChild(keyTextNode);
            _lastNode.AppendChild(keyElement);

            XmlNode node = (XmlNode) value.getAsDictionary();
            XmlNode dict = node.SelectSingleNode("//dict");
            //XmlNode dict = node.LastChild.FirstChild;
            XmlNode importNode = _xmlDoc.ImportNode(dict, true);
            _lastNode.AppendChild(importNode);
        }

        public void setListForKey(List value, string key)
        {
            //key
            XmlElement keyElement = _xmlDoc.CreateElement("key");
            XmlText keyTextNode = _xmlDoc.CreateTextNode(key);
            keyElement.AppendChild(keyTextNode);
            _lastNode.AppendChild(keyElement);

            //value
            XmlElement arrayElement = _xmlDoc.CreateElement("array");
            _lastNode.AppendChild(arrayElement);
            for (int i = 0; i < value.size(); i++)
            {
                Serializable serNode = (Serializable) value.get(i);
                XmlNode node = (XmlNode) serNode.getAsDictionary();
                //XmlNode dict = node.LastChild.FirstChild;
                //XmlNode importNode = _xmlDoc.ImportNode(dict, true);
                XmlNode importNode = _xmlDoc.ImportNode(node, true);
                arrayElement.AppendChild(importNode);
            }
        }

        public void setStringForKey(string value, string key)
        {
            // key element
            XmlElement keyElement = _xmlDoc.CreateElement("key");
            XmlText keyTextNode = _xmlDoc.CreateTextNode(key);
            keyElement.AppendChild(keyTextNode);
            _lastNode.AppendChild(keyElement);

            keyElement = _xmlDoc.CreateElement("string");
            keyTextNode = _xmlDoc.CreateTextNode(value);
            keyElement.AppendChild(keyTextNode);
            _lastNode.AppendChild(keyElement);
        }

        public object getSerialized()
        {
            return _rootNode;
        }

        public static void Register()
        {
            SerializerFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : SerializerFactory
        {
            protected override object create()
            {
                return new PlistSerializer();
            }
        }
    }
}