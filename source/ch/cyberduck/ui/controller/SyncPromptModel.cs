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
using ch.cyberduck.core;
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

        public override object GetSize(TreePathReference reference)
        {
            Path p = GetPath(reference);
            SyncTransfer.Comparison compare = ((SyncTransfer) Transfer).compare(p);
            return compare.equals(SyncTransfer.COMPARISON_REMOTE_NEWER)
                       ? p.attributes().getSize()
                       : p.getLocal().attributes().getSize();
        }

        public override object GetWarningImage(TreePathReference reference)
        {
            Path p = GetPath(reference);
            if (p.attributes().isFile())
            {
                if (p.exists())
                {
                    if (p.attributes().getSize() == 0)
                    {
                        return AlertIcon;
                    }
                }
                if (p.getLocal().exists())
                {
                    if (p.getLocal().attributes().getSize() == 0)
                    {
                        return AlertIcon;
                    }
                }
            }
            return null;
        }

        public override object GetCreateImage(TreePathReference reference)
        {
            Path p = GetPath(reference);
            if (!(p.exists() && p.getLocal().exists()))
            {
                return IconCache.Instance.IconForName("plus");
            }
            return null;
        }

        public override object GetSyncGetter(TreePathReference reference)
        {
            Path p = (reference).Unique;
            if(p.attributes().isDirectory()) {
                if(p.exists() && p.getLocal().exists()) {
                    return null;
                }
            }
            SyncTransfer.Comparison compare = ((SyncTransfer) Transfer).compare(p);
            if (compare.equals(SyncTransfer.COMPARISON_REMOTE_NEWER))
            {
                return IconCache.Instance.IconForName("arrowDown", 16);
            }
            if (compare.equals(SyncTransfer.COMPARISON_LOCAL_NEWER))
            {
                return IconCache.Instance.IconForName("arrowUp", 16);
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