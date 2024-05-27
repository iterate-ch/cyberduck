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

using ch.cyberduck.ui.Model;
using CommunityToolkit.Mvvm.ComponentModel;
using DynamicData.Binding;
using System;

namespace ch.cyberduck.ui.ViewModels;

public partial class TransferItemViewModel : SynchronizedObservableObject
{
    [ObservableProperty]
    private string title;

    public TransferItemModel TransferItem { get; }

    public TransferItemViewModel(TransferItemModel transferItem)
    {
        TransferItem = transferItem;

        this.WhenValueChanged(m => m.TransferItem.Title)
            .Subscribe(v => Title = v);
    }

    public override string ToString() => Title;
}
