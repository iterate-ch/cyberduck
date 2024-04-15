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
using ch.cyberduck.core.threading;
using ch.cyberduck.core.transfer;
using CommunityToolkit.Mvvm.ComponentModel;

namespace ch.cyberduck.ui.Model
{
    public partial class TransferProgressModel(Transfer transfer) : ObservableObject, ProgressListener
    {
        [ObservableProperty]
        private bool completed;
        [ObservableProperty]
        private string message;
        [ObservableProperty]
        private double? progress;
        [ObservableProperty]
        private string text;

        public TransferBackgroundAction Action { get; set; }

        public Transfer Transfer => transfer;

        public void Cancel()
        {
            Action?.cancel();
        }

        public void Refresh(TransferProgress progress)
        {
            if (progress is null)
            {
                Completed = false;
                Message = null;
                Progress = null;
            }
            else
            {
                Completed = progress.isComplete();
                Progress = progress.getSize().longValue() is long size and > 0
                    ? progress.getTransferred().longValue() * 100.0 / size
                    : null;
                Text = progress.getProgress();
            }
        }

        void ProgressListener.message(string str)
        {
            Message = str;
        }
    }
}
