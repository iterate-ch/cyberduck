// Copyright(c) 2002 - 2024 iterate GmbH. All rights reserved.
// https://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

using ch.cyberduck.core;
using ch.cyberduck.core.local;
using ch.cyberduck.core.local.features;
using ch.cyberduck.core.transfer;
using CommunityToolkit.Mvvm.ComponentModel;
using org.apache.logging.log4j;
using System;

namespace ch.cyberduck.ui.Model
{
    public partial class TransferItemModel(TransferItem transferItem) : ObservableObject
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(TransferItemModel).FullName);
        private static readonly WeakReference<Trash> trash = new(null);
        [ObservableProperty]
        private string title;

        public Local Local => TransferItem.local;

        public TransferItem TransferItem => transferItem;

        private static Trash Trash
        {
            get
            {
                Trash instance;
                lock (trash)
                {
                    if (!trash.TryGetTarget(out instance))
                    {
                        trash.SetTarget(instance = LocalTrashFactory.get());
                    }
                }

                return instance;
            }
        }

        public void Delete()
        {
            var local = TransferItem.local;
            if (local.exists())
            {
                try
                {
                    Trash.trash(local);
                }
                catch (Exception e)
                {
                    Log.warn(string.Format("Failure trashing file {0} {1}", local, e.Message));
                }
            }
        }

        public void Refresh(int? count)
        {
            Title = count is { } ? $"{TransferItem.remote.getName()} ({count} more)" : TransferItem.remote.getName();
        }

        public override string ToString() => Title;
    }
}
