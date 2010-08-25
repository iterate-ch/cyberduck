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
using System.Xml;
using ch.cyberduck.core;
using ch.cyberduck.core.serializer;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Winforms.Serializer
{
    public class TransferPlistReader : PlistReader<Transfer>
    {
        private static readonly Logger Log = Logger.getLogger(typeof (TransferPlistReader).Name);

        public static void Register()
        {
            TransferReaderFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        public override Transfer deserialize(XmlNode dictNode)
        {
            XmlNode transferDict = dictNode.SelectSingleNode("dict");
            if (null != transferDict)
            {
                Host host = new Host(transferDict);
                Session s = SessionFactory.createSession(host);
                return deserialize(dictNode, s);
            }
            throw new ArgumentException("Unknown transfer");
        }

        private Transfer deserialize(XmlNode dictNode, Session session)
        {
            XmlNode kindKeyNode = dictNode.SelectSingleNode("key[.='Kind']");
            if (null != kindKeyNode)
            {
                int kind = int.Parse(kindKeyNode.NextSibling.InnerText);
                switch (kind)
                {
                    case Transfer.KIND_DOWNLOAD:
                        return new DownloadTransfer(dictNode, session);
                    case Transfer.KIND_UPLOAD:
                        return new UploadTransfer(dictNode, session);
                    case Transfer.KIND_SYNC:
                        return new SyncTransfer(dictNode, session);
                }
                Log.error("Unknown transfer:" + kind);
            }
            return null;
        }

        private class Factory : TransferReaderFactory
        {
            protected override object create()
            {
                return new TransferPlistReader();
            }
        }
    }
}