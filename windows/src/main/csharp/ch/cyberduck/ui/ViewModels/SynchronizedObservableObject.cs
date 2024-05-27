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

using CommunityToolkit.Mvvm.ComponentModel;
using System.ComponentModel;
using System.Threading;

namespace ch.cyberduck.ui.ViewModels;

public abstract class SynchronizedObservableObject : ObservableObject
{
    private readonly SynchronizationContext sync = SynchronizationContext.Current;

    protected override void OnPropertyChanged(PropertyChangedEventArgs e)
    {
        sync.Send(d => base.OnPropertyChanged((PropertyChangedEventArgs)d), e);
    }

    protected override void OnPropertyChanging(PropertyChangingEventArgs e)
    {
        sync.Send(d => base.OnPropertyChanging((PropertyChangingEventArgs)d), e);
    }
}
