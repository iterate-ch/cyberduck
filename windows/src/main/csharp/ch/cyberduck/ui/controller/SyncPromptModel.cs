// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
// http://cyberduck.io/
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
// feedback@cyberduck.io
// 

using ch.cyberduck.core;
using ch.cyberduck.core.synchronization;
using ch.cyberduck.core.transfer;
using Ch.Cyberduck.Core.Resources;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class SyncPromptModel : TransferPromptModel
    {
        public SyncPromptModel(TransferPromptController controller, Session session, Transfer transfer)
            : base(controller, session, transfer)
        {
        }

        public virtual object GetCreateImage(TransferItem item)
        {
            if (!GetStatus(item).isExists())
            {
                return IconCache.Instance.IconForName("plus");
            }
            return null;
        }

        public override object GetSyncGetter(TransferItem item)
        {
            Comparison compare = ((SyncTransfer) Transfer).compare(item);
            if (compare.equals(Comparison.remote))
            {
                return IconCache.Instance.IconForName("transfer-download", 16);
            }
            if (compare.equals(Comparison.local))
            {
                return IconCache.Instance.IconForName("transfer-upload", 16);
            }
            return null;
        }
    }
}