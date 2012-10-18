// 
// Copyright (c) 2010-2012 Yves Langisch. All rights reserved.
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

using ch.cyberduck.core;
using ch.cyberduck.core.synchronization;
using ch.cyberduck.core.transfer;
using ch.cyberduck.core.transfer.synchronisation;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class SyncPromptModel : TransferPromptModel
    {
        private readonly PathFilter _filter;

        public SyncPromptModel(TransferPromptController controller, Transfer transfer) : base(controller, transfer)
        {
            _filter = new SyncPathFilter(transfer);
        }

        /// <summary>
        /// Filtering what files are displayed. Used to
        /// decide which files to include in the prompt dialog
        /// </summary>
        /// <returns></returns>
        public override PathFilter Filter()
        {
            return _filter;
        }

        public override void Add(Path path)
        {
            foreach (Path child in Transfer.children(path))
            {
                base.Add(child);
            }
        }

        public override object GetSize(Path path)
        {
            Comparison compare = ((SyncTransfer) Transfer).compare(path);
            return compare.equals(Comparison.REMOTE_NEWER)
                       ? path.attributes().getSize()
                       : path.getLocal().attributes().getSize();
        }

        public override object GetWarningImage(Path path)
        {
            if (path.attributes().isFile())
            {
                if (path.exists())
                {
                    if (path.attributes().getSize() == 0)
                    {
                        return AlertIcon;
                    }
                }
                if (path.getLocal().exists())
                {
                    if (path.getLocal().attributes().getSize() == 0)
                    {
                        return AlertIcon;
                    }
                }
            }
            return null;
        }

        public override object GetCreateImage(Path path)
        {
            if (!(path.exists() && path.getLocal().exists()))
            {
                return IconCache.Instance.IconForName("plus");
            }
            return null;
        }

        public override object GetSyncGetter(Path path)
        {
            if (path.attributes().isDirectory())
            {
                if (path.exists() && path.getLocal().exists())
                {
                    return null;
                }
            }
            Comparison compare = ((SyncTransfer) Transfer).compare(path);
            if (compare.equals(Comparison.REMOTE_NEWER))
            {
                return IconCache.Instance.IconForName("transfer-download", 16);
            }
            if (compare.equals(Comparison.LOCAL_NEWER))
            {
                return IconCache.Instance.IconForName("transfer-upload", 16);
            }
            return null;
        }

        internal class SyncPathFilter : PromptFilter
        {
            protected static Logger Log = Logger.getLogger(typeof (SyncPathFilter).FullName);
            private readonly Transfer _transfer;

            public SyncPathFilter(Transfer transfer)
            {
                _transfer = transfer;
            }

            public override bool accept(AbstractPath ap)
            {
                Path child = (Path) ap;
                Log.debug("accept:" + child);
                return base.accept(child);
            }
        }
    }
}