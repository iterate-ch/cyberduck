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
using ch.cyberduck.core;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class SyncPromptModel : TransferPromptModel
    {
        private readonly PathFilter _filter = new SyncPathFilter();

        public SyncPromptModel(TransferPromptController controller, Transfer transfer) : base(controller, transfer)
        {
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

        public override object GetSize(object reference)
        {
            //todo
            return Convert.ToInt64(100);

            Path p = ((TreePathReference)reference).Unique;
            SyncTransfer.Comparison compare = ((SyncTransfer) Transfer).compare(p);
            return compare.equals(SyncTransfer.COMPARISON_REMOTE_NEWER)
                       ? p.attributes().getSize()
                       : p.getLocal().attributes().getSize();            
        }

        public override object GetWarningImage(object reference)
        {
            //todo
            return null;

            Path p = ((TreePathReference)reference).Unique;
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

        public override object GetCreateImage(object reference)
        {
            return null;

            //todo
            Path p = ((TreePathReference)reference).Unique;
            //todo p.exists() gibt immer false zurück wegen Path.getParent().childs().contains(this) -> equals -> TreePathReference nimmt noch Attribute rein
            //todo bei GetSyncGetter wohl dasselbe
            if (!(p.exists() && p.getLocal().exists()))
            {
                return IconCache.Instance.IconForName("plus");
            }
            return null;
        }

        public override object GetSyncGetter(object reference)
        {
            return null;

            //todo
            Path p = ((TreePathReference)reference).Unique;
            SyncTransfer.Comparison compare = ((SyncTransfer)Transfer).compare(p);
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
            protected static Logger Log = Logger.getLogger(typeof (SyncPathFilter));

            public override bool accept(AbstractPath ap)
            {
                Path child = (Path) ap;
                Log.debug("accept:" + child);
                return base.accept(child);
            }
        }
    }
}