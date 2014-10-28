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

using System;
using System.Xml;
using ch.cyberduck.core;
using ch.cyberduck.core.serializer;
using java.util;

namespace Ch.Cyberduck.Core.Serializer.Impl
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

        public bool booleanForKey(string key)
        {
            XmlNode foundNode = _plistEntry.SelectSingleNode("key[.='" + key + "']");
            if (null == foundNode)
            {
                return false;
            }
            if ("1".Equals(foundNode.NextSibling.InnerText, StringComparison.OrdinalIgnoreCase))
            {
                return true;
            }
            return false;
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
    }
}