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
using ch.cyberduck.core.transfer;
using org.apache.log4j;

namespace Ch.Cyberduck.Core.Serializer.Impl
{
    public class TransferPlistReader : PlistReader<Transfer>
    {
        private static readonly Logger Log = Logger.getLogger(typeof (TransferPlistReader).FullName);

        public static void Register()
        {
            TransferReaderFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        public override Transfer deserialize(XmlNode dictNode)
        {
            XmlNode transferDict = dictNode.SelectSingleNode("dict");
            if (null != transferDict)
            {
                XmlNode kindKeyNode = dictNode.SelectSingleNode("key[.='Kind']");
                if (null != kindKeyNode)
                {
                    int kind = int.Parse(kindKeyNode.NextSibling.InnerText);
                    Transfer.Type type = Transfer.Type.values()[kind];
                    if (type == Transfer.Type.download)
                    {
                        return new DownloadTransfer(dictNode);
                    }
                    if (type == Transfer.Type.upload)
                    {
                        return new UploadTransfer(dictNode);
                    }
                    if (type == Transfer.Type.sync)
                    {
                        return new SyncTransfer(dictNode);
                    }
                    if (type == Transfer.Type.copy)
                    {
                        return new CopyTransfer(dictNode);
                    }
                    Log.error("Unknown transfer:" + kind);
                    throw new ArgumentException("Unknown transfer");
                }
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