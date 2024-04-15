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
using ch.cyberduck.core.transfer;
using CommunityToolkit.Mvvm.ComponentModel;
using DynamicData;
using DynamicData.Binding;
using DynamicData.Kernel;
using java.util;
using System;

namespace ch.cyberduck.ui.Model
{
    public partial class TransferModel : ObservableObject
    {
        private readonly SourceList<TransferItem> rootsSource = new();
        private readonly IObservableList<TransferItemModel> roots;

        public float BandwidthRate => Model.getBandwidth().getRate();

        public bool Completed => Model.isComplete();

        public string Local => Model.getLocal();

        public Transfer Model { get; }

        public string Name => Model.getName();

        public IObservableList<TransferItemModel> Roots => roots;

        public long Size => Model.getSize().longValue();

        public Date Timestamp => Model.getTimestamp();

        public long Transferred => Model.getTransferred().longValue();

        public Transfer.Type Type => Model.getType();

        public TransferModel(Transfer transfer)
        {
            Model = transfer;
            rootsSource.Connect()
                .Transform((TransferItem m, Optional<TransferItemModel> old, int i) =>
                {
                    TransferItemModel model = old.HasValue
                        ? old.Value
                        : new(m);
                    model.Refresh(i == 0 && rootsSource.Count is { } count and > 1 ? count : null);
                    return model;
                }, true)
                .BindToObservableList(out roots).Subscribe();
        }

        public void Refresh()
        {
            OnPropertyChanged(nameof(BandwidthRate));
            OnPropertyChanged(nameof(Completed));
            OnPropertyChanged(nameof(Local));
            OnPropertyChanged(nameof(Name));
            OnPropertyChanged(nameof(Size));
            OnPropertyChanged(nameof(Timestamp));
            OnPropertyChanged(nameof(Transferred));
            rootsSource.EditDiff(Model.getRoots().AsEnumerable<TransferItem>());
        }
    }
}
